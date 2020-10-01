/*
 * Copyright 2020 HM Revenue & Customs
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

import cats.data.EitherT
import cats.implicits._
import javax.inject.{Inject, Singleton}
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, ControllerComponents}
import play.mvc.Http.MimeTypes
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.audit.http.connector.AuditResult
import utils.Logging
import v1.connectors.DesUri
import v1.controllers.requestParsers.DeleteRetrieveRequestParser
import v1.models.audit.{AuditEvent, AuditResponse, GenericAuditDetail}
import v1.models.domain.DesTaxYear
import v1.models.errors._
import v1.models.request.DeleteRetrieveRawData
import v1.services.{AuditService, DeleteRetrieveService, EnrolmentsAuthService, MtdIdLookupService}

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class DeleteForeignController @Inject()(val authService: EnrolmentsAuthService,
                                        val lookupService: MtdIdLookupService,
                                        requestParser: DeleteRetrieveRequestParser,
                                        service: DeleteRetrieveService,
                                        auditService: AuditService,
                                        cc: ControllerComponents)(implicit ec: ExecutionContext)
  extends AuthorisedController(cc) with BaseController with Logging {

  implicit val endpointLogContext: EndpointLogContext =
    EndpointLogContext(
      controllerName = "DeleteForeignController",
      endpointName = "deleteForeign"
    )

  def deleteForeign(nino: String, taxYear: String): Action[AnyContent] =
    authorisedAction(nino).async { implicit request =>

      val rawData: DeleteRetrieveRawData = DeleteRetrieveRawData(
        nino = nino,
        taxYear = taxYear
      )

      implicit val desUri: DesUri[Unit] = DesUri[Unit](
        s"some-placeholder/foreign/$nino/${DesTaxYear.fromMtd(taxYear)}"
      )

      val result =
        for {
          _ <- EitherT.fromEither[Future](requestParser.parseRequest(rawData))
          serviceResponse <- EitherT(service.delete())
        } yield {
          logger.info(
            s"[${endpointLogContext.controllerName}][${endpointLogContext.endpointName}] - " +
              s"Success response received with CorrelationId: ${serviceResponse.correlationId}")

          auditSubmission(
            GenericAuditDetail(
              request.userDetails, Map("nino" -> nino, "taxYear" -> taxYear), None,
              serviceResponse.correlationId, AuditResponse(httpStatus = NO_CONTENT, response = Right(None))
            )
          )

          NoContent
            .withApiHeaders(serviceResponse.correlationId)
            .as(MimeTypes.JSON)
        }

      result.leftMap { errorWrapper =>
        val correlationId = getCorrelationId(errorWrapper)
        val result = errorResult(errorWrapper).withApiHeaders(correlationId)

        auditSubmission(
          GenericAuditDetail(
            request.userDetails, Map("nino" -> nino, "taxYear" -> taxYear), None,
            correlationId, AuditResponse(httpStatus = result.header.status, response = Left(errorWrapper.auditErrors))
          )
        )

        result
      }.merge
    }

  private def errorResult(errorWrapper: ErrorWrapper) = {
    (errorWrapper.error: @unchecked) match {
      case BadRequestError | NinoFormatError | TaxYearFormatError |
           RuleTaxYearRangeInvalidError | RuleTaxYearNotSupportedError
      => BadRequest(Json.toJson(errorWrapper))
      case NotFoundError => NotFound(Json.toJson(errorWrapper))
      case DownstreamError => InternalServerError(Json.toJson(errorWrapper))
    }
  }

  private def auditSubmission(details: GenericAuditDetail)
                             (implicit hc: HeaderCarrier,
                              ec: ExecutionContext): Future[AuditResult] = {
    val event = AuditEvent("DeleteForeignIncome", "delete-foreign-income", details)
    auditService.auditEvent(event)
  }
}
