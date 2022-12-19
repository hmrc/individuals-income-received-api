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

import api.controllers.{AuthorisedController, BaseController, EndpointLogContext}
import api.hateoas.AmendHateoasBody
import api.models.audit.{AuditEvent, AuditResponse, GenericAuditDetail}
import api.models.errors._
import api.services.{AuditService, EnrolmentsAuthService, MtdIdLookupService}
import cats.data.EitherT
import cats.implicits._
import config.AppConfig
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.{Action, AnyContentAsJson, ControllerComponents}
import play.mvc.Http.MimeTypes
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.audit.http.connector.AuditResult
import utils.{IdGenerator, Logging}
import v1.models.request.createAmendOther.CreateAmendOtherRawData
import v1.requestParsers.CreateAmendOtherRequestParser
import v1.services.CreateAmendOtherService

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class CreateAmendOtherController @Inject()(val authService: EnrolmentsAuthService,
                                           val lookupService: MtdIdLookupService,
                                           appConfig: AppConfig,
                                           requestParser: CreateAmendOtherRequestParser,
                                           service: CreateAmendOtherService,
                                           auditService: AuditService,
                                           cc: ControllerComponents,
                                           val idGenerator: IdGenerator)(implicit ec: ExecutionContext)
    extends AuthorisedController(cc)
    with BaseController
    with Logging
    with AmendHateoasBody {

  implicit val endpointLogContext: EndpointLogContext =
    EndpointLogContext(
      controllerName = "AmendOtherController",
      endpointName = "amendOther"
    )

  def createAmendOther(nino: String, taxYear: String): Action[JsValue] =
    authorisedAction(nino).async(parse.json) { implicit request =>
      implicit val correlationId: String = idGenerator.generateCorrelationId
      logger.info(
        s"[${endpointLogContext.controllerName}][${endpointLogContext.endpointName}] " +
          s"with CorrelationId: $correlationId")
      val rawData: CreateAmendOtherRawData = CreateAmendOtherRawData(
        nino = nino,
        taxYear = taxYear,
        body = AnyContentAsJson(request.body)
      )
      val result =
        for {
          parsedRequest   <- EitherT.fromEither[Future](requestParser.parseRequest(rawData))
          serviceResponse <- EitherT(service.createAmend(parsedRequest))
        } yield {
          logger.info(
            s"[${endpointLogContext.controllerName}][${endpointLogContext.endpointName}] - " +
              s"Success response received with CorrelationId: ${serviceResponse.correlationId}")

          auditSubmission(
            GenericAuditDetail(
              request.userDetails,
              Map("nino" -> nino, "taxYear" -> taxYear),
              Some(request.body),
              serviceResponse.correlationId,
              AuditResponse(httpStatus = OK, response = Right(Some(amendOtherHateoasBody(appConfig, nino, taxYear))))
            )
          )
          Ok(amendOtherHateoasBody(appConfig, nino, taxYear))
            .withApiHeaders(serviceResponse.correlationId)
            .as(MimeTypes.JSON)
        }

      result.leftMap { errorWrapper =>
        val resCorrelationId = errorWrapper.correlationId
        val result           = errorResult(errorWrapper).withApiHeaders(resCorrelationId)
        logger.warn(
          s"[${endpointLogContext.controllerName}][${endpointLogContext.endpointName}] - " +
            s"Error response received with CorrelationId: $resCorrelationId")

        auditSubmission(
          GenericAuditDetail(
            request.userDetails,
            Map("nino" -> nino, "taxYear" -> taxYear),
            Some(request.body),
            resCorrelationId,
            AuditResponse(httpStatus = result.header.status, response = Left(errorWrapper.auditErrors))
          )
        )
        result
      }.merge
    }

  private def errorResult(errorWrapper: ErrorWrapper) =
    errorWrapper.error match {
      case BadRequestError | NinoFormatError | TaxYearFormatError | RuleTaxYearRangeInvalidError | RuleTaxYearNotSupportedError | CustomMtdError(
            TaxYearFormatError.code) | CustomMtdError(RuleTaxYearRangeInvalidError.code) | CustomMtdError(RuleIncorrectOrEmptyBodyError.code) |
          CustomMtdError(ValueFormatError.code) | CustomMtdError(CountryCodeFormatError.code) | CustomMtdError(CountryCodeRuleError.code) =>
        BadRequest(Json.toJson(errorWrapper))
      case StandardDownstreamError => InternalServerError(Json.toJson(errorWrapper))
      case _                       => unhandledError(errorWrapper)
    }

  private def auditSubmission(details: GenericAuditDetail)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[AuditResult] = {
    val event = AuditEvent("CreateAmendOtherIncome", "create-amend-other-income", details)
    auditService.auditEvent(event)
  }

}