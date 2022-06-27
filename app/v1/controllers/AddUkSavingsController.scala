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
import api.models.errors._
import api.services.{AuditService, EnrolmentsAuthService, MtdIdLookupService}
import cats.data.EitherT
import javax.inject.{Inject, Singleton}
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.{Action, AnyContentAsJson, ControllerComponents}
import play.mvc.Http.MimeTypes
import utils.{IdGenerator, Logging}

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class AddUkSavingsController @Inject() (val authService: EnrolmentsAuthService,
                                        val lookupService: MtdIdLookupService,
                                        requestParser: AddUkSavingsRequestParser,
                                        service: AddUkSavingsService,
                                        auditService: AuditService,
                                        hateoasFactory: HateoasFactory,
                                        cc: ControllerComponents,
                                        val idGenerator: IdGenerator)(implicit ec: ExecutionContext)

  extends AuthorisedController(cc)
  with BaseController
  with Logging {

  implicit val endpointLogContext: EndpointLogContext =
    EndpointLogContext(
      controllerName = "AddUkSavingsController",
      endpointName = "addSavings"
    )

  def addUkSavings(nino: String, taxYear: String): Action[JsValue] =
    authorisedAction(nino).async(parse.json) { implicit request =>
      implicit val correlationId: String = idGenerator.generateCorrelationId
      logger.info(s"[${endpointLogContext.controllerName}][${endpointLogContext.endpointName}] with CorrelationId: $correlationId")

      val rawData: AddUkSavingsRawData = AddUkSavingsRawData(nino = nino, body = AnyContentAsJson(request.body))

      val result =
        for {
          parsedRequest   <- EitherT.fromEither[Future](requestParser.parseRequest(rawData))
          serviceResponse <- EitherT(service.addEmployment(parsedRequest))
          hateoasResponse <- EitherT.fromEither[Future](
            hateoasFactory
              .wrap(serviceResponse.responseData, AddUkSavingsHateoasData(nino, taxYear, serviceResponse.responseData.employmentId))
              .asRight[ErrorWrapper]
          )
        } yield {
          logger.info(
            s"[${endpointLogContext.controllerName}][${endpointLogContext.endpointName}] - Success response received with CorrelationId: ${serviceResponse.correlationId}"
          )

          Ok(Json.toJson(hateoasResponse))
            .withApiHeaders(serviceResponse.correlationId)
            .as(MimeTypes.JSON)
        }

      result.leftMap { errorWrapper =>
        val resCorrelationId = errorWrapper.correlationId
        val result           = errorResult(errorWrapper).withApiHeaders(resCorrelationId)
        logger.warn(
          s"[${endpointLogContext.controllerName}][${endpointLogContext.endpointName}] - Error response received with CorrelationId: $resCorrelationId")

        result
      }.merge
    }

  private def errorResult(errorWrapper: ErrorWrapper) =
    errorWrapper.error match {
      case BadRequestError | NinoFormatError | TaxYearFormatError | RuleTaxYearRangeInvalidError | RuleTaxYearNotSupportedError |
           RuleTaxYearNotEndedError | CustomMtdError(RuleIncorrectOrEmptyBodyError.code) =>
        BadRequest(Json.toJson(errorWrapper))
      case StandardDownstreamError => InternalServerError(Json.toJson(errorWrapper))
      case _                       => unhandledError(errorWrapper)
    }


}

