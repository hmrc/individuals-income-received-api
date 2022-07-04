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
import api.hateoas.HateoasFactory
import api.models.errors.{BadRequestError, ErrorWrapper, NinoFormatError, NotFoundError,
  RuleTaxYearNotSupportedError, RuleTaxYearRangeInvalidError, SavingsAccountIdFormatError, StandardDownstreamError, TaxYearFormatError}
import api.services.{EnrolmentsAuthService, MtdIdLookupService}
import cats.data.EitherT
import cats.implicits.catsSyntaxEitherId
import play.api.libs.json.Json
import javax.inject.{Inject, Singleton}
import play.api.mvc.{Action, AnyContent, ControllerComponents}
import play.mvc.Http.MimeTypes
import utils.{IdGenerator, Logging}
import v1.models.request.retrieveUkSavingsAnnualSummary.RetrieveUkSavingsAnnualSummaryRawData
import v1.models.response.retrieveUkSavingsAnnualSummary.RetrieveUkSavingsAnnualSummaryResponseHateoasData
import v1.requestParsers.RetrieveUkSavingsAccountRequestParser
import v1.services.RetrieveUkSavingsAccountAnnualSummaryService

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class RetrieveUkSavingsAccountAnnualSummaryController @Inject() (val authService: EnrolmentsAuthService,
                                                        val lookupService: MtdIdLookupService,
                                                        requestParser: RetrieveUkSavingsAccountRequestParser,
                                                        service: RetrieveUkSavingsAccountAnnualSummaryService,
                                                        hateoasFactory: HateoasFactory,
                                                        cc: ControllerComponents,
                                                        val idGenerator: IdGenerator)(implicit ec: ExecutionContext)
              extends AuthorisedController(cc)
              with BaseController
              with Logging {

  implicit val endpointLogContext: EndpointLogContext =
    EndpointLogContext(
      controllerName = "RetrieveUkSavingsAccountAnnualSummaryController",
      endpointName = "retrieveUkSavingAccountSummary"
    )

  def retrieveUkSavingAccount(nino: String, taxYear: String, savingsAccountId: String): Action[AnyContent] =
    authorisedAction(nino).async { implicit request =>
      implicit val correlationId: String = idGenerator.generateCorrelationId
      logger.info(
        s"[${endpointLogContext.controllerName}][${endpointLogContext.endpointName}] " +
          s"with CorrelationId: $correlationId")

      val rawData: RetrieveUkSavingsAnnualSummaryRawData = RetrieveUkSavingsAnnualSummaryRawData(nino, taxYear, savingsAccountId)

      val result =
        for {
           parsedRequest   <- EitherT.fromEither[Future](requestParser.parseRequest(rawData))
           serviceResponse <- EitherT(service.retrieveUkSavingsAccountAnnualSummary(parsedRequest))
           vendorResponse  <- EitherT.fromEither[Future](
              hateoasFactory
               .wrap(serviceResponse.responseData, RetrieveUkSavingsAnnualSummaryResponseHateoasData(nino, taxYear, savingsAccountId))
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
      case BadRequestError | NinoFormatError | TaxYearFormatError | RuleTaxYearRangeInvalidError | RuleTaxYearNotSupportedError | SavingsAccountIdFormatError =>
        BadRequest(Json.toJson(errorWrapper))
      case NotFoundError           => NotFound(Json.toJson(errorWrapper))
      case StandardDownstreamError => InternalServerError(Json.toJson(errorWrapper))
      case _                       => unhandledError(errorWrapper)
    }
}
