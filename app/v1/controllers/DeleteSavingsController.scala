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

import play.api.mvc.{Action, AnyContent, ControllerComponents}
import utils.IdGenerator
import api.controllers.{AuditHandler, AuthorisedController, EndpointLogContext, RequestContext, RequestHandler}
import api.services.{AuditService, EnrolmentsAuthService, MtdIdLookupService}
import v1.controllers.requestParsers.DeleteSavingsRequestParser
import v1.models.request.deleteSavings.DeleteSavingsRawData
import v1.services.DeleteSavingsService

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext

@Singleton
class DeleteSavingsController @Inject() (val authService: EnrolmentsAuthService,
                                         val lookupService: MtdIdLookupService,
                                         parser: DeleteSavingsRequestParser,
                                         service: DeleteSavingsService,
                                         auditService: AuditService,
                                         cc: ControllerComponents,
                                         val idGenerator: IdGenerator)(implicit ec: ExecutionContext)
    extends AuthorisedController(cc) {

  implicit val endpointLogContext: EndpointLogContext =
    EndpointLogContext(
      controllerName = "DeleteSavingsController",
      endpointName = "deleteSaving"
    )

  def deleteSaving(nino: String, taxYear: String): Action[AnyContent] =
    authorisedAction(nino).async { implicit request =>
      implicit val ctx: RequestContext = RequestContext.from(idGenerator, endpointLogContext)

      val rawData: DeleteSavingsRawData = DeleteSavingsRawData(nino = nino, taxYear = taxYear)

      val requestHandler = RequestHandler
        .withParser(parser)
        .withService(service.deleteSavings)
        .withNoContentResult()
        .withAuditing(
          AuditHandler(
            auditService,
            auditType = "DeleteSavingsIncome",
            transactionName = "delete-savings-income",
            params = Map("nino" -> nino, "taxYear" -> taxYear)
          )
        )
      requestHandler.handleRequest(rawData)
    }
//    implicit val correlationId: String = idGenerator.generateCorrelationId
//    logger.info(
//      s"[${endpointLogContext.controllerName}][${endpointLogContext.endpointName}] " +
//        s"with CorrelationId: $correlationId"
//    )
//
//    val rawData: DeleteSavingsRawData = DeleteSavingsRawData(nino = nino, taxYear = taxYear)
//
//    val result =
//      for {
//        parsedRequest   <- EitherT.fromEither[Future](parser.parseRequest(rawData))
//        serviceResponse <- EitherT(service.deleteSavings(parsedRequest))
//      } yield {
//        logger.info(
//          s"[${endpointLogContext.controllerName}][${endpointLogContext.endpointName}] - " +
//            s"Success response received with CorrelationId: ${serviceResponse.correlationId}")
//
//        auditSubmission(
//          GenericAuditDetail(
//            request.userDetails,
//            Map("nino" -> nino, "taxYear" -> taxYear),
//            None,
//            serviceResponse.correlationId,
//            AuditResponse(httpStatus = NO_CONTENT, response = Right(None))
//          ))
//
//        NoContent
//          .withApiHeaders(serviceResponse.correlationId)
//          .as(MimeTypes.JSON)
//      }
//
//    result.leftMap { errorWrapper =>
//      val resCorrelationId = errorWrapper.correlationId
//      val result           = errorResult(errorWrapper).withApiHeaders(resCorrelationId)
//      logger.warn(
//        s"[${endpointLogContext.controllerName}][${endpointLogContext.endpointName}] - " +
//          s"Error response received with CorrelationId: $resCorrelationId")
//
//      auditSubmission(
//        GenericAuditDetail(
//          request.userDetails,
//          Map("nino" -> nino, "taxYear" -> taxYear),
//          None,
//          resCorrelationId,
//          AuditResponse(httpStatus = result.header.status, response = Left(errorWrapper.auditErrors))
//        ))
//
//      result
//    }.merge
//  }
//
//  private def errorResult(errorWrapper: ErrorWrapper) =
//    errorWrapper.error match {
//      case _
//          if errorWrapper.containsAnyOf(
//            BadRequestError,
//            NinoFormatError,
//            TaxYearFormatError,
//            RuleTaxYearRangeInvalidError,
//            RuleTaxYearNotSupportedError) =>
//        BadRequest(Json.toJson(errorWrapper))
//      case NotFoundError => NotFound(Json.toJson(errorWrapper))
//      case InternalError => InternalServerError(Json.toJson(errorWrapper))
//      case _             => unhandledError(errorWrapper)
//    }
//
//  private def auditSubmission(details: GenericAuditDetail)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[AuditResult] = {
//    val event = AuditEvent("DeleteSavingsIncome", "delete-savings-income", details)
//    auditService.auditEvent(event)
//  }

}
