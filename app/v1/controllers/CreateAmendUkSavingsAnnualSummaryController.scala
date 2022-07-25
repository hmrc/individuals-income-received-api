/*
 * Copyright 2022 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package v1.controllers

import api.controllers.{AuthorisedController, BaseController, EndpointLogContext, UserRequest}
import api.hateoas.{AmendHateoasBody, HateoasFactory}
import api.models.audit.{AuditEvent, AuditResponse, FlattenedGenericAuditDetail}
import api.models.errors._
import api.models.hateoas.RelType.CREATE_AND_AMEND_UK_SAVINGS
import api.services.{AuditService, EnrolmentsAuthService, MtdIdLookupService}
import cats.data.EitherT
import cats.implicits._
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.{Action, AnyContentAsJson, ControllerComponents}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.audit.http.connector.AuditResult
import utils.{IdGenerator, Logging}
import v1.models.request.createAmendUkSavingsAnnualSummary.CreateAmendUkSavingsAnnualSummaryRawData
import v1.models.response.createAmendUkSavingsIncomeAnnualSummary.CreateAndAmendUkSavingsAnnualSummaryHateoasData
import v1.requestParsers.CreateAmendUkSavingsAccountAnnualSummaryRequestParser
import v1.services.CreateAmendUkSavingsAnnualSummaryService

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class CreateAmendUkSavingsAnnualSummaryController @Inject() (val authService: EnrolmentsAuthService,
                                                             val lookupService: MtdIdLookupService,
                                                             requestParser: CreateAmendUkSavingsAccountAnnualSummaryRequestParser,
                                                             service: CreateAmendUkSavingsAnnualSummaryService,
                                                             auditService: AuditService,
                                                             hateoasFactory: HateoasFactory,
                                                             cc: ControllerComponents,
                                                             val idGenerator: IdGenerator)(implicit ec: ExecutionContext)
    extends AuthorisedController(cc)
    with BaseController
    with Logging
    with AmendHateoasBody {

  implicit val endpointLogContext: EndpointLogContext =
    EndpointLogContext(
      controllerName = "CreateAmendUkSavingsAnnualSummaryController",
      endpointName = "createAmendUkSavingsAnnualSummary"
    )

  def createAmendUkSavingsAnnualSummary(nino: String, taxYear: String, savingsAccountId: String): Action[JsValue] =
    authorisedAction(nino).async(parse.json) { implicit request =>
      implicit val correlationId: String = idGenerator.generateCorrelationId
      logger.info(
        s"[${endpointLogContext.controllerName}][${endpointLogContext.endpointName}]" +
          s"with CorrelationId: $correlationId")

      val rawData = CreateAmendUkSavingsAnnualSummaryRawData(
        nino = nino,
        taxYear = taxYear,
        savingsAccountId = savingsAccountId,
        body = AnyContentAsJson(request.body)
      )

      val auditMapping: Map[String, String] = Map("nino" -> nino, "taxYear" -> taxYear, "savingsAccountId" -> savingsAccountId)
      val result = for {
        parsedRequest   <- EitherT.fromEither[Future](requestParser.parseRequest(rawData))
        serviceResponse <- EitherT(service.createAmend(parsedRequest))
        vendorResponse <- EitherT.fromEither[Future](
          hateoasFactory
            .wrap((), CreateAndAmendUkSavingsAnnualSummaryHateoasData(nino, taxYear, savingsAccountId))
            .asRight[ErrorWrapper])
      } yield {
        logger.info(
          s"[${endpointLogContext.controllerName}][${endpointLogContext.endpointName}] - " +
            s"Success response received with CorrelationId: ${serviceResponse.correlationId}"
        )
        saveAudit(request, auditMapping, serviceResponse.correlationId, AuditResponse(httpStatus = OK, response = Right(None)))

        Ok(Json.toJson(vendorResponse))
          .withApiHeaders(serviceResponse.correlationId)
      }

      result.leftMap { errorWrapper =>
        val resCorrelationId = errorWrapper.correlationId
        val errResult        = errorResult(errorWrapper).withApiHeaders(resCorrelationId)
        logger.warn(
          s"[${endpointLogContext.controllerName}][${endpointLogContext.endpointName}] " +
            s"Error response received with CorrelationId: $resCorrelationId")
        saveAudit(
          request,
          auditMapping,
          resCorrelationId,
          AuditResponse(httpStatus = errResult.header.status, response = Left(errorWrapper.auditErrors)))

        errorResult(errorWrapper).withApiHeaders(resCorrelationId)
      }.merge
    }

  private def errorResult(errorWrapper: ErrorWrapper) =
    errorWrapper.error match {
      case BadRequestError | NinoFormatError | SavingsAccountIdFormatError | TaxYearFormatError | RuleTaxYearRangeInvalidError |
          RuleTaxYearNotSupportedError | CustomMtdError(ValueFormatError.code) | CustomMtdError(RuleIncorrectOrEmptyBodyError.code) =>
        BadRequest(Json.toJson(errorWrapper))
      case NotFoundError           => NotFound(Json.toJson(errorWrapper))
      case StandardDownstreamError => InternalServerError(Json.toJson(errorWrapper))
      case _                       => unhandledError(errorWrapper)
    }

  private def auditSubmission(details: FlattenedGenericAuditDetail)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[AuditResult] = {
    val event: AuditEvent[FlattenedGenericAuditDetail] = AuditEvent("createAmendUkSavingsAnnualSummary", CREATE_AND_AMEND_UK_SAVINGS, details)
    auditService.auditEvent(event)
  }

  private def saveAudit(request: UserRequest[JsValue], auditMapping: Map[String, String], correlationId: String, auditResponse: AuditResponse)(
      implicit hc: HeaderCarrier) = {

    auditSubmission(
      FlattenedGenericAuditDetail(
        versionNumber = Some("1.0"),
        request.userDetails,
        auditMapping,
        Some(request.body),
        correlationId,
        auditResponse
      )
    )
  }

}
