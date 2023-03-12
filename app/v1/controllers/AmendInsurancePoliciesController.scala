/*
 * Copyright 2023 HM Revenue & Customs
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
import v1.controllers.requestParsers.AmendInsurancePoliciesRequestParser
import v1.models.request.amendInsurancePolicies.AmendInsurancePoliciesRawData
import v1.services.AmendInsurancePoliciesService

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class AmendInsurancePoliciesController @Inject() (val authService: EnrolmentsAuthService,
                                                  val lookupService: MtdIdLookupService,
                                                  appConfig: AppConfig,
                                                  requestParser: AmendInsurancePoliciesRequestParser,
                                                  service: AmendInsurancePoliciesService,
                                                  auditService: AuditService,
                                                  cc: ControllerComponents,
                                                  val idGenerator: IdGenerator)(implicit ec: ExecutionContext)
    extends AuthorisedController(cc)
    with BaseController
    with Logging
    with AmendHateoasBody {

  implicit val endpointLogContext: EndpointLogContext =
    EndpointLogContext(
      controllerName = "AmendInsurancePoliciesController",
      endpointName = "amendInsurancePolicies"
    )

  def amendInsurancePolicies(nino: String, taxYear: String): Action[JsValue] =
    authorisedAction(nino).async(parse.json) { implicit request =>
      implicit val correlationId: String = idGenerator.generateCorrelationId
      logger.info(
        s"[${endpointLogContext.controllerName}][${endpointLogContext.endpointName}] " +
          s"with CorrelationId: $correlationId")

      val rawData: AmendInsurancePoliciesRawData = AmendInsurancePoliciesRawData(
        nino = nino,
        taxYear = taxYear,
        body = AnyContentAsJson(request.body)
      )

      val result =
        for {
          parsedRequest   <- EitherT.fromEither[Future](requestParser.parseRequest(rawData))
          serviceResponse <- EitherT(service.amendInsurancePolicies(parsedRequest))
        } yield {
          logger.info(
            s"[${endpointLogContext.controllerName}][${endpointLogContext.endpointName}] - " +
              s"Success response received with CorrelationId: ${serviceResponse.correlationId}")

          auditSubmission(
            GenericAuditDetail(
              userDetails = request.userDetails,
              params = Map("nino" -> nino, "taxYear" -> taxYear),
              request = Some(request.body),
              `X-CorrelationId` = serviceResponse.correlationId,
              auditResponse = AuditResponse(httpStatus = OK, response = Right(Some(amendInsurancePoliciesHateoasBody(appConfig, nino, taxYear))))
            )
          )

          Ok(amendInsurancePoliciesHateoasBody(appConfig, nino, taxYear))
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
            userDetails = request.userDetails,
            params = Map("nino" -> nino, "taxYear" -> taxYear),
            request = Some(request.body),
            `X-CorrelationId` = resCorrelationId,
            auditResponse = AuditResponse(httpStatus = result.header.status, response = Left(errorWrapper.auditErrors))
          )
        )

        result
      }.merge
    }

  private def errorResult(errorWrapper: ErrorWrapper) =
    errorWrapper.error match {
      case InternalError => InternalServerError(Json.toJson(errorWrapper))
      case _
          if errorWrapper.containsAnyOf(
            BadRequestError,
            NinoFormatError,
            TaxYearFormatError,
            RuleTaxYearNotSupportedError,
            RuleTaxYearRangeInvalidError,
            RuleIncorrectOrEmptyBodyError,
            ValueFormatError,
            CustomerRefFormatError,
            EventFormatError
          ) =>
        BadRequest(Json.toJson(errorWrapper))
      case _ => unhandledError(errorWrapper)
    }

  private def auditSubmission(details: GenericAuditDetail)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[AuditResult] = {

    val event = AuditEvent(
      auditType = "CreateAmendInsurancePolicies",
      transactionName = "create-amend-insurance-policies",
      detail = details
    )
    auditService.auditEvent(event)
  }

}
