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

package v1r7.controllers

import api.models.errors._
import cats.data.EitherT
import cats.implicits._
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, ControllerComponents}
import play.mvc.Http.MimeTypes
import utils.{IdGenerator, Logging}
import api.connectors.DownstreamUri.Release6Uri
import api.controllers.{AuthorisedController, BaseController, EndpointLogContext}
import api.hateoas.HateoasFactory
import api.models.domain.MtdSourceEnum
import api.models.domain.MtdSourceEnum.latest
import api.services.{DeleteRetrieveService, EnrolmentsAuthService, MtdIdLookupService}
import v1r7.models.request.retrieveFinancialDetails.RetrieveFinancialDetailsRawData
import v1r7.models.response.retrieveFinancialDetails.{RetrieveFinancialDetailsHateoasData, RetrieveFinancialDetailsResponse}
import v1r7.requestParsers.RetrieveFinancialDetailsRequestParser

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class RetrieveFinancialDetailsController @Inject()(val authService: EnrolmentsAuthService,
                                                   val lookupService: MtdIdLookupService,
                                                   requestParser: RetrieveFinancialDetailsRequestParser,
                                                   service: DeleteRetrieveService,
                                                   hateoasFactory: HateoasFactory,
                                                   cc: ControllerComponents,
                                                   val idGenerator: IdGenerator)(implicit ec: ExecutionContext)
    extends AuthorisedController(cc)
    with BaseController
    with Logging {

  implicit val endpointLogContext: EndpointLogContext =
    EndpointLogContext(
      controllerName = "RetrieveFinancialDetailsController",
      endpointName = "retrieve"
    )

  def retrieve(nino: String, taxYear: String, employmentId: String, source: Option[String]): Action[AnyContent] =
    authorisedAction(nino).async { implicit request =>
      implicit val correlationId: String = idGenerator.generateCorrelationId
      logger.info(
        s"[${endpointLogContext.controllerName}][${endpointLogContext.endpointName}] " +
          s"with CorrelationId: $correlationId")

      val rawData: RetrieveFinancialDetailsRawData = RetrieveFinancialDetailsRawData(
        nino = nino,
        taxYear = taxYear,
        employmentId = employmentId,
        source = source
      )

      implicit val desUri: Release6Uri[RetrieveFinancialDetailsResponse] = Release6Uri[RetrieveFinancialDetailsResponse](
        s"income-tax/income/employments/$nino/$taxYear/$employmentId?view" +
          s"=${source.flatMap(MtdSourceEnum.parser.lift).getOrElse(latest).toDesViewString}"
      )

      val result =
        for {
          _               <- EitherT.fromEither[Future](requestParser.parseRequest(rawData))
          serviceResponse <- EitherT(service.retrieve[RetrieveFinancialDetailsResponse](ifsErrorMap))
          vendorResponse <- EitherT.fromEither[Future](
            hateoasFactory
              .wrap(serviceResponse.responseData, RetrieveFinancialDetailsHateoasData(nino, taxYear, employmentId))
              .asRight[ErrorWrapper])
        } yield {
          logger.info(
            s"[${endpointLogContext.controllerName}][${endpointLogContext.endpointName}] - " +
              s"Success response received with CorrelationId: ${serviceResponse.correlationId}")

          Ok(Json.toJson(vendorResponse))
            .withApiHeaders(serviceResponse.correlationId)
            .as(MimeTypes.JSON)
        }

      result.leftMap { errorWrapper =>
        val resCorrelationId = errorWrapper.correlationId
        val result           = errorResult(errorWrapper).withApiHeaders(resCorrelationId)
        logger.warn(
          s"[${endpointLogContext.controllerName}][${endpointLogContext.endpointName}] - " +
            s"Error response received with CorrelationId: $resCorrelationId")

        result
      }.merge
    }

  private def errorResult(errorWrapper: ErrorWrapper) =
    errorWrapper.error match {
      case BadRequestError | NinoFormatError | TaxYearFormatError | EmploymentIdFormatError | RuleTaxYearRangeInvalidError | SourceFormatError |
          RuleTaxYearNotSupportedError =>
        BadRequest(Json.toJson(errorWrapper))
      case NotFoundError           => NotFound(Json.toJson(errorWrapper))
      case StandardDownstreamError => InternalServerError(Json.toJson(errorWrapper))
      case _                       => unhandledError(errorWrapper)
    }

  private def ifsErrorMap: Map[String, MtdError] =
    Map(
      "INVALID_TAXABLE_ENTITY_ID" -> NinoFormatError,
      "INVALID_TAX_YEAR"          -> TaxYearFormatError,
      "INVALID_EMPLOYMENT_ID"     -> EmploymentIdFormatError,
      "INVALID_VIEW"              -> SourceFormatError,
      "TAX_YEAR_NOT_SUPPORTED"    -> RuleTaxYearNotSupportedError,
      "INVALID_CORRELATIONID"     -> StandardDownstreamError,
      "NO_DATA_FOUND"             -> NotFoundError,
      "SERVER_ERROR"              -> StandardDownstreamError,
      "SERVICE_UNAVAILABLE"       -> StandardDownstreamError
    )
}
