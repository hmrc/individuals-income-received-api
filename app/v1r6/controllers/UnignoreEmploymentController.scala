/*
 * Copyright 2021 HM Revenue & Customs
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

package v1r6.controllers

import cats.data.EitherT
import cats.implicits._
import config.AppConfig
import javax.inject.{Inject, Singleton}
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, ControllerComponents}
import play.mvc.Http.MimeTypes
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.audit.http.connector.AuditResult
import utils.{IdGenerator, Logging}
import v1r6.controllers.requestParsers.IgnoreEmploymentRequestParser
import v1r6.hateoas.IgnoreHateoasBody
import v1r6.models.audit.{AuditEvent, AuditResponse, GenericAuditDetail}
import v1r6.models.errors._
import v1r6.models.request.ignoreEmployment.IgnoreEmploymentRawData
import v1r6.services.{AuditService, EnrolmentsAuthService, MtdIdLookupService, UnignoreEmploymentService}

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class UnignoreEmploymentController @Inject()(val authService: EnrolmentsAuthService,
                                             val lookupService: MtdIdLookupService,
                                             appConfig: AppConfig,
                                             requestParser: IgnoreEmploymentRequestParser,
                                             service: UnignoreEmploymentService,
                                             auditService: AuditService,
                                             cc: ControllerComponents,
                                             val idGenerator: IdGenerator)(implicit ec: ExecutionContext)
  extends AuthorisedController(cc) with BaseController with Logging with IgnoreHateoasBody {

  implicit val endpointLogContext: EndpointLogContext =
    EndpointLogContext(
      controllerName = "UnignoreEmploymentController",
      endpointName = "unignoreEmployment"
    )

  def unignoreEmployment(nino: String, taxYear: String, employmentId: String): Action[AnyContent] =
    authorisedAction(nino).async { implicit request =>

      implicit val correlationId: String = idGenerator.generateCorrelationId
      logger.info(
        s"[${endpointLogContext.controllerName}][${endpointLogContext.endpointName}] " +
          s"with CorrelationId: $correlationId")

      val rawData: IgnoreEmploymentRawData = IgnoreEmploymentRawData(
        nino = nino,
        taxYear = taxYear,
        employmentId = employmentId
      )

      val result =
        for {
          parsedRequest <- EitherT.fromEither[Future](requestParser.parseRequest(rawData))
          serviceResponse <- EitherT(service.unignoreEmployment(parsedRequest))
        } yield {
          logger.info(
            s"[${endpointLogContext.controllerName}][${endpointLogContext.endpointName}] - " +
              s"Success response received with CorrelationId: ${serviceResponse.correlationId}")

          auditSubmission(
            GenericAuditDetail(
              request.userDetails, Map("nino" -> nino, "taxYear" -> taxYear, "employmentId" -> employmentId), None, serviceResponse.correlationId,
              AuditResponse(httpStatus = OK, response = Right(Some(ignoreEmploymentHateoasBody(appConfig, nino, taxYear, employmentId))))
            )
          )

          Ok(ignoreEmploymentHateoasBody(appConfig, nino, taxYear, employmentId))
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
          GenericAuditDetail(
            request.userDetails, Map("nino" -> nino, "taxYear" -> taxYear, "employmentId" -> employmentId), None,
            resCorrelationId, AuditResponse(httpStatus = result.header.status, response = Left(errorWrapper.auditErrors))
          )
        )

        result
      }.merge
    }

  private def errorResult(errorWrapper: ErrorWrapper) = {
    (errorWrapper.error: @unchecked) match {
      case BadRequestError | NinoFormatError | TaxYearFormatError | EmploymentIdFormatError |
           RuleTaxYearNotSupportedError | RuleTaxYearRangeInvalidError | RuleTaxYearNotEndedError
      => BadRequest(Json.toJson(errorWrapper))
      case RuleCustomEmploymentUnignoreError => Forbidden(Json.toJson(errorWrapper))
      case NotFoundError => NotFound(Json.toJson(errorWrapper))
      case DownstreamError => InternalServerError(Json.toJson(errorWrapper))
    }
  }

  private def auditSubmission(details: GenericAuditDetail)
                             (implicit hc: HeaderCarrier,
                              ec: ExecutionContext): Future[AuditResult] = {
    val event = AuditEvent("UnignoreEmployment", "unignore-employment", details)
    auditService.auditEvent(event)
  }
}