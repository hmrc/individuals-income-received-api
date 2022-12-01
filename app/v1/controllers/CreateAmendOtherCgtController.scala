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
import v1.models.audit.CreateAmendOtherCgtAuditDetail
import v1.models.request.createAmendOtherCgt.CreateAmendOtherCgtRawData
import v1.requestParsers.CreateAmendOtherCgtRequestParser
import v1.services._

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class CreateAmendOtherCgtController @Inject() (val authService: EnrolmentsAuthService,
                                               val lookupService: MtdIdLookupService,
                                               appConfig: AppConfig,
                                               requestParser: CreateAmendOtherCgtRequestParser,
                                               service: CreateAmendOtherCgtService,
                                               nrsProxyService: NrsProxyService,
                                               auditService: AuditService,
                                               cc: ControllerComponents,
                                               val idGenerator: IdGenerator)(implicit ec: ExecutionContext)
    extends AuthorisedController(cc)
    with BaseController
    with Logging
    with AmendHateoasBody {

  implicit val endpointLogContext: EndpointLogContext =
    EndpointLogContext(
      controllerName = "CreateAmendOtherCgtController",
      endpointName = "createAmendOtherCgt"
    )

  def createAmendOtherCgt(nino: String, taxYear: String): Action[JsValue] =
    authorisedAction(nino).async(parse.json) { implicit request =>
      implicit val correlationId: String = idGenerator.generateCorrelationId
      logger.info(
        s"[${endpointLogContext.controllerName}][${endpointLogContext.endpointName}] " +
          s"with CorrelationId: $correlationId")

      val rawData: CreateAmendOtherCgtRawData = CreateAmendOtherCgtRawData(
        nino = nino,
        taxYear = taxYear,
        body = AnyContentAsJson(request.body)
      )

      val result =
        for {
          parsedRequest <- EitherT.fromEither[Future](requestParser.parseRequest(rawData))
          serviceResponse <- {
            nrsProxyService.submitAsync(nino, "itsa-cgt-disposal-other", request.body)
            EitherT(service.createAmend(parsedRequest))
          }
        } yield {
          logger.info(
            s"[${endpointLogContext.controllerName}][${endpointLogContext.endpointName}] - " +
              s"Success response received with CorrelationId: ${serviceResponse.correlationId}")

          auditSubmission(
            CreateAmendOtherCgtAuditDetail(
              request.userDetails,
              nino,
              taxYear,
              request.body,
              serviceResponse.correlationId,
              AuditResponse(OK, Right(Some(amendOtherCgtHateoasBody(appConfig, nino, taxYear))))
            ))

          Ok(amendOtherCgtHateoasBody(appConfig, nino, taxYear))
            .withApiHeaders(serviceResponse.correlationId)
            .as(MimeTypes.JSON)
        }

      result.leftMap { errorWrapper =>
        val resCorrelationId = errorWrapper.correlationId
        val result = errorResult(errorWrapper).withApiHeaders(resCorrelationId)
        logger.warn(
          s"[${endpointLogContext.controllerName}][${endpointLogContext.endpointName}] - " +
            s"Error response received with CorrelationId: $resCorrelationId")

        auditSubmission(
          CreateAmendOtherCgtAuditDetail(
            request.userDetails,
            nino,
            taxYear,
            request.body,
            correlationId,
            AuditResponse(result.header.status, Left(errorWrapper.auditErrors))))

        result
      }.merge
    }

  private def errorResult(errorWrapper: ErrorWrapper) =
    errorWrapper.error match {
      case _
        if errorWrapper.containsAnyOf(
          BadRequestError,
          NinoFormatError,
          TaxYearFormatError,
          RuleTaxYearNotSupportedError,
          RuleTaxYearRangeInvalidError,
        ) =>
        BadRequest(Json.toJson(errorWrapper))
      case CustomMtdError(RuleIncorrectOrEmptyBodyError.code) |
           CustomMtdError(RuleGainLossError.code) |
           CustomMtdError(ValueFormatError.code) |
           CustomMtdError(DateFormatError.code) |
           CustomMtdError(AssetDescriptionFormatError.code) |
           CustomMtdError(AssetTypeFormatError.code) |
           CustomMtdError(ClaimOrElectionCodesFormatError.code) |
           CustomMtdError(RuleDisposalDateError.code) |
           CustomMtdError(RuleAcquisitionDateError.code) |
           CustomMtdError(RuleGainAfterReliefLossAfterReliefError.code) =>
        BadRequest(Json.toJson(errorWrapper))
      case RuleDisposalDateError |
           RuleAcquisitionDateError =>
        BadRequest(Json.toJson(errorWrapper))
      case StandardDownstreamError => InternalServerError(Json.toJson(errorWrapper))
      case _ => unhandledError(errorWrapper)
    }

  private def auditSubmission(details: CreateAmendOtherCgtAuditDetail)(implicit hc: HeaderCarrier, ec: ExecutionContext) = {
    val event = AuditEvent("CreateAmendOtherCgtDisposalsAndGains", "Create-Amend-Other-Cgt-Disposals-And-Gains", details)
    auditService.auditEvent(event)
  }

}
