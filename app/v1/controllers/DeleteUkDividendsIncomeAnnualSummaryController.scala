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
import api.models.audit.{AuditEvent, AuditResponse, FlattenedGenericAuditDetail}
import api.models.errors._
import api.services.{AuditService, EnrolmentsAuthService, MtdIdLookupService}
import cats.data.EitherT
import cats.implicits._
import javax.inject.{Inject, Singleton}
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, ControllerComponents}
import play.mvc.Http.MimeTypes
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.audit.http.connector.AuditResult
import utils.{IdGenerator, Logging}
import v1.models.request.deleteUkDividendsIncomeAnnualSummary.DeleteUkDividendsIncomeAnnualSummaryRawData
import v1.requestParsers.DeleteUkDividendsIncomeAnnualSummaryRequestParser
import v1.services.DeleteUkDividendsIncomeAnnualSummaryService

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class DeleteUkDividendsIncomeAnnualSummaryController @Inject() (val authService: EnrolmentsAuthService,
                                                                val lookupService: MtdIdLookupService,
                                                                requestParser: DeleteUkDividendsIncomeAnnualSummaryRequestParser,
                                                                service: DeleteUkDividendsIncomeAnnualSummaryService,
                                                                auditService: AuditService,
                                                                cc: ControllerComponents,
                                                                val idGenerator: IdGenerator)(implicit ec: ExecutionContext)
    extends AuthorisedController(cc)
    with BaseController
    with Logging {

  implicit val endpointLogContext: EndpointLogContext =
    EndpointLogContext(
      controllerName = "DeleteUkDividendsIncomeAnnualSummaryController",
      endpointName = "deleteUkDividends"
    )

  def deleteUkDividends(nino: String, taxYear: String): Action[AnyContent] =
    authorisedAction(nino).async { implicit request =>
      implicit val correlationId: String = idGenerator.generateCorrelationId
      logger.info(
        s"[${endpointLogContext.controllerName}][${endpointLogContext.endpointName}] " +
          s"with CorrelationId: $correlationId")

      val rawData: DeleteUkDividendsIncomeAnnualSummaryRawData = DeleteUkDividendsIncomeAnnualSummaryRawData(
        nino = nino,
        taxYear = taxYear
      )

      val result =
        for {
          parsedRequest   <- EitherT.fromEither[Future](requestParser.parseRequest(rawData))
          serviceResponse <- EitherT(service.delete(parsedRequest))
        } yield {
          logger.info(
            s"[${endpointLogContext.controllerName}][${endpointLogContext.endpointName}] - " +
              s"Success response received with CorrelationId: ${serviceResponse.correlationId}")

        auditSubmission(
          FlattenedGenericAuditDetail(
            versionNumber = Some("1.0"),
            request.userDetails,
            Map("nino" -> nino, "taxYear" -> taxYear),
            None,
            serviceResponse.correlationId,
            AuditResponse(httpStatus = NO_CONTENT, response = Right(None))
          )
        )

          NoContent
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
            FlattenedGenericAuditDetail(
              Some("1.0"),
              request.userDetails,
              Map("nino" -> nino, "taxYear" -> taxYear),
              None,
              resCorrelationId,
              AuditResponse(httpStatus = result.header.status, response = Left(errorWrapper.auditErrors))
            )
          )

        result
      }.merge
    }

  private def errorResult(errorWrapper: ErrorWrapper) =
    errorWrapper.error match {
      case BadRequestError | NinoFormatError | TaxYearFormatError | RuleTaxYearRangeInvalidError | RuleTaxYearNotSupportedError =>
        BadRequest(Json.toJson(errorWrapper))
      case NotFoundError           => NotFound(Json.toJson(errorWrapper))
      case StandardDownstreamError => InternalServerError(Json.toJson(errorWrapper))
      case _                       => unhandledError(errorWrapper)
    }

  private def auditSubmission(details: FlattenedGenericAuditDetail)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[AuditResult] = {
    val event = AuditEvent("DeleteUkDividendsIncome", "delete-uk-dividends-income", details)
    auditService.auditEvent(event)
  }
}
