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
import utils.Logging
import v1.connectors.DesUri
import v1.controllers.requestParsers.DeleteCustomEmploymentRequestParser
import v1.models.errors._
import v1.models.request.deleteCustomEmployment.DeleteCustomEmploymentRawData
import v1.services.{DeleteRetrieveService, EnrolmentsAuthService, MtdIdLookupService}

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class DeleteEmploymentFinancialDetailsController @Inject()(val authService: EnrolmentsAuthService,
                                                           val lookupService: MtdIdLookupService,
                                                           requestParser: DeleteCustomEmploymentRequestParser,
                                                           service: DeleteRetrieveService,
                                                           cc: ControllerComponents)(implicit ec: ExecutionContext)
  extends AuthorisedController(cc) with BaseController with Logging {

  implicit val endpointLogContext: EndpointLogContext =
    EndpointLogContext(
      controllerName = "DeleteEmploymentFinancialDetailsController",
      endpointName = "deleteEmploymentFinancialDetails"
    )

  def deleteEmploymentFinancialDetails(nino: String, taxYear: String, employmentId: String): Action[AnyContent] =
    authorisedAction(nino).async { implicit request =>

      val rawData: DeleteCustomEmploymentRawData = DeleteCustomEmploymentRawData(
        nino = nino,
        taxYear = taxYear,
        employmentId = employmentId
      )

      implicit val desUri: DesUri[Unit] = DesUri[Unit](
        s"income-tax/income/employments/$nino/$taxYear/$employmentId"
      )

      val result =
        for {
          _ <- EitherT.fromEither[Future](requestParser.parseRequest(rawData))
          serviceResponse <- EitherT(service.delete(desErrorMap))
        } yield {
          logger.info(
            s"[${endpointLogContext.controllerName}][${endpointLogContext.endpointName}] - " +
              s"Success response received with CorrelationId: ${serviceResponse.correlationId}")

          NoContent
            .withApiHeaders(serviceResponse.correlationId)
            .as(MimeTypes.JSON)
        }

      result.leftMap { errorWrapper =>
        val correlationId = getCorrelationId(errorWrapper)
        val result = errorResult(errorWrapper).withApiHeaders(correlationId)

        result
      }.merge
    }

  private def errorResult(errorWrapper: ErrorWrapper) = {
    (errorWrapper.error: @unchecked) match {
      case BadRequestError | NinoFormatError | TaxYearFormatError | EmploymentIdFormatError |
           RuleTaxYearRangeInvalidError | RuleTaxYearNotSupportedError => BadRequest(Json.toJson(errorWrapper))
      case NotFoundError => NotFound(Json.toJson(errorWrapper))
      case DownstreamError => InternalServerError(Json.toJson(errorWrapper))
    }
  }

  private def desErrorMap: Map[String, MtdError] =
    Map(
      "INVALID_TAXABLE_ENTITY_ID" -> NinoFormatError,
      "INVALID_TAX_YEAR" -> TaxYearFormatError,
      "INVALID_EMPLOYMENT_ID" -> EmploymentIdFormatError,
      "INVALID_CORRELATIONID" -> DownstreamError,
      "NO_DATA_FOUND" -> NotFoundError,
      "SERVER_ERROR" -> DownstreamError,
      "SERVICE_UNAVAILABLE" -> DownstreamError
    )
}
