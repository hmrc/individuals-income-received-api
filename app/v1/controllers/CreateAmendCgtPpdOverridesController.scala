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
import api.models.audit.{AuditEvent, AuditResponse}
import api.models.errors._
import api.services.{AuditService, EnrolmentsAuthService, MtdIdLookupService, NrsProxyService}
import cats.data.EitherT
import config.AppConfig
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.{Action, AnyContentAsJson, ControllerComponents}
import play.mvc.Http.MimeTypes
import uk.gov.hmrc.http.HeaderCarrier
import utils.{IdGenerator, Logging}
import v1.models.audit.CreateAmendCgtPpdOverridesAuditDetail
import v1.models.request.createAmendCgtPpdOverrides.CreateAmendCgtPpdOverridesRawData
import v1.requestParsers.CreateAmendCgtPpdOverridesRequestParser
import v1.services._

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class CreateAmendCgtPpdOverridesController @Inject() (val authService: EnrolmentsAuthService,
                                                      val lookupService: MtdIdLookupService,
                                                      appConfig: AppConfig,
                                                      requestParser: CreateAmendCgtPpdOverridesRequestParser,
                                                      service: CreateAmendCgtPpdOverridesService,
                                                      auditService: AuditService,
                                                      nrsProxyService: NrsProxyService,
                                                      cc: ControllerComponents,
                                                      val idGenerator: IdGenerator)(implicit ec: ExecutionContext)
    extends AuthorisedController(cc)
    with BaseController
    with Logging
    with AmendHateoasBody {

  implicit val endpointLogContext: EndpointLogContext =
    EndpointLogContext(
      controllerName = "CreateAmendCgtPpdOverridesController",
      endpointName = "createAmendCgtPpdOverrides"
    )

  def createAmendCgtPpdOverrides(nino: String, taxYear: String): Action[JsValue] =
    authorisedAction(nino).async(parse.json) { implicit request =>
      implicit val correlationId: String = idGenerator.generateCorrelationId
      logger.info(
        s"[${endpointLogContext.controllerName}][${endpointLogContext.endpointName}]" +
          s"with CorrelationId: $correlationId")

      val rawData: CreateAmendCgtPpdOverridesRawData = CreateAmendCgtPpdOverridesRawData(
        nino = nino,
        taxYear = taxYear,
        body = AnyContentAsJson(request.body)
      )

      val result =
        for {
          parsedRequest <- EitherT.fromEither[Future](requestParser.parseRequest(rawData))
          serviceResponse <- {
            nrsProxyService.submitAsync(nino, "itsa-cgt-disposal-ppd", request.body)
            EitherT(service.createAmend(parsedRequest))
          }
        } yield {
          logger.info(
            s"[${endpointLogContext.controllerName}][${endpointLogContext.endpointName}] - " +
              s"Success response received with CorrelationId: ${serviceResponse.correlationId}"
          )

          auditSubmission(
            CreateAmendCgtPpdOverridesAuditDetail(
              request.userDetails,
              nino,
              taxYear,
              request.body,
              serviceResponse.correlationId,
              AuditResponse(OK, Right(Some(amendCgtPpdOverridesHateoasBody(appConfig, nino, taxYear))))
            ))

          Ok(amendCgtPpdOverridesHateoasBody(appConfig, nino, taxYear))
            .withApiHeaders(serviceResponse.correlationId)
            .as(MimeTypes.JSON)

        }

      result.leftMap { errorWrapper =>
        val resCorrelationId = errorWrapper.correlationId
        val result           = errorResult(errorWrapper).withApiHeaders(resCorrelationId)
        logger.warn(
          s"[${endpointLogContext.controllerName}][${endpointLogContext.endpointName}] " +
            s"Error response received with CorrelationId: $resCorrelationId")

        auditSubmission(
          CreateAmendCgtPpdOverridesAuditDetail(
            request.userDetails,
            nino,
            taxYear,
            request.body,
            correlationId,
            AuditResponse(result.header.status, Left(errorWrapper.auditErrors))))

        result
      }.merge
    }

  private val badRequestErrors: Seq[MtdError] = Seq(
    BadRequestError,
    NinoFormatError,
    TaxYearFormatError,
    RuleTaxYearRangeInvalidError,
    RuleTaxYearNotSupportedError,
    RuleAmountGainLossError,
    ValueFormatError,
    DateFormatError,
    PpdSubmissionIdFormatError,
    RuleLossesGreaterThanGainError,
    RuleTaxYearNotEndedError,
    RuleIncorrectOrEmptyBodyError,
    RuleDuplicatedPpdSubmissionIdError
  )

  private def errorResult(errorWrapper: ErrorWrapper) =
    errorWrapper.error match {
      case NotFoundError | PpdSubmissionIdNotFoundError          => NotFound(Json.toJson(errorWrapper))
      case RuleIncorrectDisposalTypeError                        => Forbidden(Json.toJson(errorWrapper))
      case StandardDownstreamError                               => InternalServerError(Json.toJson(errorWrapper))
      case _ if errorWrapper.containsAnyOf(badRequestErrors: _*) => BadRequest(Json.toJson(errorWrapper))
      case _                                                     => unhandledError(errorWrapper)
    }

  private def auditSubmission(details: CreateAmendCgtPpdOverridesAuditDetail)(implicit hc: HeaderCarrier, ec: ExecutionContext) = {
    val event = AuditEvent("CreateAmendCgtPpdOverrides", "Create-Amend-Cgt-Ppd-Overrides", details)
    auditService.auditEvent(event)
  }

}
