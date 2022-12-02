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

import api.connectors.DownstreamUri
import api.connectors.DownstreamUri.{Api1661Uri, TaxYearSpecificIfsUri}
import api.controllers.{AuthorisedController, BaseController, EndpointLogContext}
import api.hateoas.HateoasFactory
import api.models.errors._
import api.services.{DeleteRetrieveService, EnrolmentsAuthService, MtdIdLookupService}
import cats.data.EitherT
import cats.implicits._
import config.{AppConfig, FeatureSwitches}
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, ControllerComponents}
import play.mvc.Http.MimeTypes
import utils.{IdGenerator, Logging}
import v1.models.request.retrieveNonPayeEmploymentIncome.{RetrieveNonPayeEmploymentIncomeRawData, RetrieveNonPayeEmploymentIncomeRequest}
import v1.models.response.retrieveNonPayeEmploymentIncome.{RetrieveNonPayeEmploymentIncomeHateoasData, RetrieveNonPayeEmploymentIncomeResponse}
import v1.requestParsers.RetrieveNonPayeEmploymentRequestParser

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class RetrieveNonPayeEmploymentController @Inject() (val authService: EnrolmentsAuthService,
                                                     val lookupService: MtdIdLookupService,
                                                     requestParser: RetrieveNonPayeEmploymentRequestParser,
                                                     service: DeleteRetrieveService,
                                                     hateoasFactory: HateoasFactory,
                                                     cc: ControllerComponents,
                                                     appConfig: AppConfig,
                                                     val idGenerator: IdGenerator)(implicit ec: ExecutionContext)
    extends AuthorisedController(cc)
    with BaseController
    with Logging {

  implicit val endpointLogContext: EndpointLogContext =
    EndpointLogContext(
      controllerName = "RetrieveNonPayeEmploymentController",
      endpointName = "retrieveNonPayeEmployment"
    )

  def retrieveNonPayeEmployment(nino: String, taxYear: String, source: Option[String]): Action[AnyContent] =
    authorisedAction(nino).async { implicit request =>
      implicit val correlationId: String = idGenerator.generateCorrelationId
      logger.info(
        s"[${endpointLogContext.controllerName}][${endpointLogContext.endpointName}] " +
          s"with CorrelationId: $correlationId")

      val rawData = RetrieveNonPayeEmploymentIncomeRawData(
        nino = nino,
        taxYear = taxYear,
        source = source
      )

      val result =
        for {
          parsedRequest <- EitherT.fromEither[Future](requestParser.parseRequest(rawData))
          uri = downstreamUri(parsedRequest)
          serviceResponse <- EitherT(service.retrieve[RetrieveNonPayeEmploymentIncomeResponse](uri, desErrorMap))
        } yield {
          val vendorResponse = hateoasFactory.wrap(serviceResponse.responseData, RetrieveNonPayeEmploymentIncomeHateoasData(nino, taxYear))

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
      case BadRequestError | NinoFormatError | TaxYearFormatError | SourceFormatError | RuleTaxYearRangeInvalidError | RuleTaxYearNotSupportedError =>
        BadRequest(Json.toJson(errorWrapper))
      case NotFoundError           => NotFound(Json.toJson(errorWrapper))
      case StandardDownstreamError => InternalServerError(Json.toJson(errorWrapper))
      case _                       => InternalServerError(Json.toJson(errorWrapper))
    }

  private def downstreamUri(parsedRequest: RetrieveNonPayeEmploymentIncomeRequest): DownstreamUri[RetrieveNonPayeEmploymentIncomeResponse] = {
    import parsedRequest._

    implicit val featureSwitches: FeatureSwitches = FeatureSwitches(appConfig.featureSwitches)

    if (taxYear.useTaxYearSpecificApi) {
      TaxYearSpecificIfsUri[RetrieveNonPayeEmploymentIncomeResponse](
        s"income-tax/income/employments/non-paye/${taxYear.asTysDownstream}/${nino.value}?view=${source.toDesViewString}"
      )
    } else {
      Api1661Uri[RetrieveNonPayeEmploymentIncomeResponse](
        // Note tax year is in MTD format
        s"income-tax/income/employments/non-paye/${nino.value}/${taxYear.asMtd}?view=${source.toDesViewString}"
      )
    }
  }

  private def desErrorMap: Map[String, MtdError] =
    Map(
      "INVALID_TAXABLE_ENTITY_ID" -> NinoFormatError,
      "INVALID_TAX_YEAR"          -> TaxYearFormatError,
      "INVALID_VIEW"              -> StandardDownstreamError,
      "INVALID_CORRELATIONID"     -> StandardDownstreamError,
      "NO_DATA_FOUND"             -> NotFoundError,
      "NOT_FOUND"                 -> NotFoundError,
      "TAX_YEAR_NOT_SUPPORTED"    -> RuleTaxYearNotSupportedError,
      "SERVER_ERROR"              -> StandardDownstreamError,
      "SERVICE_UNAVAILABLE"       -> StandardDownstreamError
    )

}
