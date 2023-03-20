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

import api.controllers.{AuditHandler, AuthorisedController, EndpointLogContext, RequestContext, RequestHandler}
import api.hateoas.HateoasFactory
import api.services.{AuditService, EnrolmentsAuthService, MtdIdLookupService}
import config.AppConfig
import play.api.libs.json.JsValue
import play.api.mvc.{Action, AnyContentAsJson, ControllerComponents}
import utils.IdGenerator
import v1.controllers.requestParsers.CreateAmendPensionsRequestParser
import v1.models.request.createAmendPensions.CreateAmendPensionsRawData
import v1.models.response.createAmendPensions.CreateAndAmendPensionsIncomeHateoasData
import v1.models.response.createAmendPensions.CreateAndAmendPensionsIncomeResponse.CreateAndAmendPensionsIncomeLinksFactory
import v1.services.CreateAmendPensionsService

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext

@Singleton
class CreateAmendPensionsController @Inject() (val authService: EnrolmentsAuthService,
                                               val lookupService: MtdIdLookupService,
                                               appConfig: AppConfig,
                                               parser: CreateAmendPensionsRequestParser,
                                               service: CreateAmendPensionsService,
                                               auditService: AuditService,
                                               hateoasFactory: HateoasFactory,
                                               cc: ControllerComponents,
                                               val idGenerator: IdGenerator)(implicit ec: ExecutionContext)
    extends AuthorisedController(cc) {

  implicit val endpointLogContext: EndpointLogContext =
    EndpointLogContext(
      controllerName = "AmendPensionsController",
      endpointName = "amendPensions"
    )

  def createAmendPensions(nino: String, taxYear: String): Action[JsValue] =
    authorisedAction(nino).async(parse.json) { implicit request =>
      implicit val ctx: RequestContext = RequestContext.from(idGenerator, endpointLogContext)

      val rawData: CreateAmendPensionsRawData = CreateAmendPensionsRawData(
        nino = nino,
        taxYear = taxYear,
        body = AnyContentAsJson(request.body)
      )

      val requestHandler = RequestHandler
        .withParser(parser)
        .withService(service.createAmendPensions)
        .withAuditing(
          AuditHandler(
            auditService,
            auditType = "CreateAmendPensionsIncome",
            transactionName = "create-amend-pensions-income",
            params = Map("nino" -> nino, "taxYear" -> taxYear),
            requestBody = Some(request.body),
            includeResponse = true
          )
        )
        .withHateoasResult(hateoasFactory)(CreateAndAmendPensionsIncomeHateoasData(nino, taxYear))

      requestHandler.handleRequest(rawData)
    }

//      implicit val correlationId: String = idGenerator.generateCorrelationId
//      logger.info(
//        s"[${endpointLogContext.controllerName}][${endpointLogContext.endpointName}] " +
//          s"with CorrelationId: $correlationId")
//
//
//      val result =
//        for {
//          parsedRequest   <- EitherT.fromEither[Future](parser.parseRequest(rawData))
//          serviceResponse <- EitherT(service.createAmendPensions(parsedRequest))
//        } yield {
//          logger.info(
//            s"[${endpointLogContext.controllerName}][${endpointLogContext.endpointName}] - " +
//              s"Success response received with CorrelationId: ${serviceResponse.correlationId}")
//
//          auditSubmission(
//            GenericAuditDetail(
//              userDetails = request.userDetails,
//              params = Map("nino" -> nino, "taxYear" -> taxYear),
//              request = Some(request.body),
//              `X-CorrelationId` = serviceResponse.correlationId,
//              response = AuditResponse(
//                httpStatus = OK,
//                response = Right(Some(createAmendPensionsHateoasBody(appConfig, nino, taxYear)))
//              )
//            )
//          )
//
//          Ok(createAmendPensionsHateoasBody(appConfig, nino, taxYear))
//            .withApiHeaders(serviceResponse.correlationId)
//            .as(MimeTypes.JSON)
//        }
//
//      result.leftMap { errorWrapper =>
//        val resCorrelationId = errorWrapper.correlationId
//        val result           = errorResult(errorWrapper).withApiHeaders(resCorrelationId)
//        logger.warn(
//          s"[${endpointLogContext.controllerName}][${endpointLogContext.endpointName}] - " +
//            s"Error response received with CorrelationId: $resCorrelationId")
//
//        auditSubmission(
//          GenericAuditDetail(
//            userDetails = request.userDetails,
//            params = Map("nino" -> nino, "taxYear" -> taxYear),
//            request = Some(request.body),
//            `X-CorrelationId` = resCorrelationId,
//            response = AuditResponse(
//              httpStatus = result.header.status,
//              response = Left(errorWrapper.auditErrors)
//            )
//          )
//        )
//
//        result
//      }.merge

//  private def auditSubmission(details: GenericAuditDetail)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[AuditResult] = {
//    val event = AuditEvent(
//      auditType = "CreateAmendPensionsIncome",
//      transactionName = "create-amend-pensions-income",
//      detail = details
//    )
//
//    auditService.auditEvent(event)
//  }

}
