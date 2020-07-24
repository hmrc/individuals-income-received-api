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
import config.AppConfig
import javax.inject.Inject
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.{Action, AnyContentAsJson, ControllerComponents}
import play.mvc.Http.MimeTypes
import utils.Logging
import v1.controllers.requestParsers.IgnoreEmploymentRequestParser
import v1.hateoas.IgnoreHateoasBody
import v1.models.errors._
import v1.models.request.ignoreEmployment.IgnoreEmploymentRawData
import v1.services.{EnrolmentsAuthService, IgnoreEmploymentService, MtdIdLookupService}

import scala.concurrent.{ExecutionContext, Future}

class IgnoreEmploymentController @Inject()(val authService: EnrolmentsAuthService,
                                           val lookupService: MtdIdLookupService,
                                           appConfig: AppConfig,
                                           requestParser: IgnoreEmploymentRequestParser,
                                           service: IgnoreEmploymentService,
                                           cc: ControllerComponents)(implicit ec: ExecutionContext)
  extends AuthorisedController(cc) with BaseController with Logging with IgnoreHateoasBody {

  implicit val endpointLogContext: EndpointLogContext =
    EndpointLogContext(
      controllerName = "IgnoreEmploymentController",
      endpointName = "ignoreEmployment"
    )

  def ignoreEmployment(nino: String, taxYear: String, employmentId: String): Action[JsValue] =
    authorisedAction(nino).async(parse.json) { implicit request =>

      val rawData: IgnoreEmploymentRawData = IgnoreEmploymentRawData(
        nino = nino,
        taxYear = taxYear,
        employmentId = employmentId,
        body = AnyContentAsJson(request.body)
      )

      val result =
        for {
          parsedRequest <- EitherT.fromEither[Future](requestParser.parseRequest(rawData))
          serviceResponse <- EitherT(service.ignoreEmployment(parsedRequest))
        } yield {
          logger.info(
            s"[${endpointLogContext.controllerName}][${endpointLogContext.endpointName}] - " +
              s"Success response received with CorrelationId: ${serviceResponse.correlationId}")

          Ok(ignoreEmploymentHateoasBody(appConfig, nino, taxYear, employmentId))
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
      case BadRequestError | NinoFormatError | TaxYearFormatError |
           EmploymentIdFormatError | RuleIncorrectOrEmptyBodyError |
           RuleTaxYearNotSupportedError | RuleTaxYearRangeInvalidError | RuleTaxYearNotEndedError
      => BadRequest(Json.toJson(errorWrapper))
      case RuleCustomEmploymentError => Forbidden(Json.toJson(errorWrapper))
      case NotFoundError => NotFound(Json.toJson(errorWrapper))
      case DownstreamError => InternalServerError(Json.toJson(errorWrapper))
    }
  }
}