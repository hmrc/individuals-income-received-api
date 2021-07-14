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

package v1.controllers

import cats.data.EitherT
import config.AppConfig
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.{Action, AnyContentAsJson, ControllerComponents}
import play.mvc.Http.MimeTypes
import utils.{IdGenerator, Logging}
import v1.controllers.requestParsers.CreateAmendOtherCgtRequestParser
import v1.hateoas.AmendHateoasBody
import v1.models.errors._
import v1.models.request.createAmendOtherCgt.CreateAmendOtherCgtRawData
import v1.services.{ CreateAmendOtherCgtService, EnrolmentsAuthService, MtdIdLookupService}

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class CreateAmendOtherCgtController @Inject()(val authService: EnrolmentsAuthService,
                                              val lookupService: MtdIdLookupService,
                                              appConfig: AppConfig,
                                              requestParser: CreateAmendOtherCgtRequestParser,
                                              service: CreateAmendOtherCgtService,
                                              cc: ControllerComponents,
                                              val idGenerator: IdGenerator)(implicit ec: ExecutionContext)
  extends AuthorisedController(cc) with BaseController with Logging with AmendHateoasBody {

  implicit val endpointLogContext: EndpointLogContext =
    EndpointLogContext(
      controllerName = "CreateAmendOtherCgtController",
      endpointName = "createAmendOtherCgt"
    )

  def createAmendOtherCgt(nino: String, taxYear: String): Action[JsValue] =
    authorisedAction(nino).async(parse.json) { implicit request =>

      implicit val correlationId: String = idGenerator.generateCorrelationId
      logger.info(
        s"[${endpointLogContext.controllerName}][${endpointLogContext.endpointName}] " +
          s"with CorrelationId: $correlationId")

      val rawData: CreateAmendOtherCgtRawData = CreateAmendOtherCgtRawData(
        nino = nino,
        taxYear = taxYear,
        body = AnyContentAsJson(request.body)
      )

      val result =
        for {
          parsedRequest <- EitherT.fromEither[Future](requestParser.parseRequest(rawData))
          serviceResponse <- EitherT(service.createAmend(parsedRequest))
        } yield {
          logger.info(
            s"[${endpointLogContext.controllerName}][${endpointLogContext.endpointName}] - " +
              s"Success response received with CorrelationId: ${serviceResponse.correlationId}")

          Ok(amendOtherCgtHateoasBody(appConfig, nino, taxYear))
            .withApiHeaders(serviceResponse.correlationId)
            .as(MimeTypes.JSON)
        }

      result.leftMap { errorWrapper =>
        val resCorrelationId = errorWrapper.correlationId
        val result = errorResult(errorWrapper).withApiHeaders(resCorrelationId)
        logger.warn(
          s"[${endpointLogContext.controllerName}][${endpointLogContext.endpointName}] - " +
            s"Error response received with CorrelationId: $resCorrelationId")

        result
      }.merge
    }

  private def errorResult(errorWrapper: ErrorWrapper) = {
    (errorWrapper.error: @unchecked) match {
      case BadRequestError | NinoFormatError | TaxYearFormatError |
           RuleTaxYearNotSupportedError | RuleTaxYearRangeInvalidError |
           CustomMtdError(RuleIncorrectOrEmptyBodyError.code) |
           CustomMtdError(RuleGainLossError.code) |
           CustomMtdError(ValueFormatError.code) |
           CustomMtdError(DateFormatError.code) |
           CustomMtdError(AssetDescriptionFormatError.code) |
           CustomMtdError(AssetTypeFormatError.code) |
           CustomMtdError(ClaimOrElectionCodesFormatError.code) |
           CustomMtdError(RuleDisposalDateError.code) |
           CustomMtdError(RuleAcquisitionDateError.code) |
           CustomMtdError(RuleGainAfterReliefLossAfterReliefError.code)
      => BadRequest(Json.toJson(errorWrapper))
      case DownstreamError => InternalServerError(Json.toJson(errorWrapper))
    }
  }
}
