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

package v1r7.controllers

import cats.data.EitherT
import cats.implicits._
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, ControllerComponents}
import play.mvc.Http.MimeTypes
import utils.{IdGenerator, Logging}
import v1r7.connectors.DownstreamUri.IfsUri
import v1r7.controllers.requestParsers.DeleteRetrieveRequestParser
import v1r7.hateoas.HateoasFactory
import v1r7.models.errors._
import v1r7.models.request.DeleteRetrieveRawData
import v1r7.models.response.retrieveOtherCgt.{RetrieveOtherCgtHateoasData, RetrieveOtherCgtResponse}
import v1r7.services.{DeleteRetrieveService, EnrolmentsAuthService, MtdIdLookupService}

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class RetrieveOtherCgtController @Inject()(val authService: EnrolmentsAuthService,
                                           val lookupService: MtdIdLookupService,
                                           requestParser: DeleteRetrieveRequestParser,
                                           service: DeleteRetrieveService,
                                           hateoasFactory: HateoasFactory,
                                           cc: ControllerComponents,
                                           val idGenerator: IdGenerator)(implicit ec: ExecutionContext)
  extends AuthorisedController(cc) with BaseController with Logging {

  implicit val endpointLogContext: EndpointLogContext =
    EndpointLogContext(
      controllerName = "RetrieveOtherCgtController",
      endpointName = "retrieveOtherCgt"
    )

  def retrieveOtherCgt(nino: String, taxYear: String): Action[AnyContent] =
    authorisedAction(nino).async { implicit request =>

      implicit val correlationId: String = idGenerator.generateCorrelationId
      logger.info(
        s"[${endpointLogContext.controllerName}][${endpointLogContext.endpointName}] " +
          s"with CorrelationId: $correlationId")

      val rawData: DeleteRetrieveRawData = DeleteRetrieveRawData(
        nino = nino,
        taxYear = taxYear
      )

      implicit val ifsUri: IfsUri[RetrieveOtherCgtResponse] = IfsUri[RetrieveOtherCgtResponse](
        s"income-tax/income/disposals/other-gains/$nino/$taxYear"
      )

      val result =
        for {
          _ <- EitherT.fromEither[Future](requestParser.parseRequest(rawData))
          serviceResponse <- EitherT(service.retrieve[RetrieveOtherCgtResponse]())
          vendorResponse <- EitherT.fromEither[Future](
            hateoasFactory
              .wrap(serviceResponse.responseData, RetrieveOtherCgtHateoasData(nino, taxYear))
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
           RuleTaxYearRangeInvalidError | RuleTaxYearNotSupportedError => BadRequest(Json.toJson(errorWrapper))
      case NotFoundError => NotFound(Json.toJson(errorWrapper))
      case DownstreamError => InternalServerError(Json.toJson(errorWrapper))
    }
  }
}