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

package v2.controllers

import api.controllers._
import api.hateoas.HateoasFactory
import api.models.audit.{AuditEvent, AuditResponse, FlattenedGenericAuditDetail}
import api.models.auth.UserDetails
import api.models.errors._
import api.services.{AuditService, EnrolmentsAuthService, MtdIdLookupService}
import play.api.libs.json.{JsObject, JsValue}
import play.api.mvc.{Action, AnyContentAsJson, ControllerComponents}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.audit.http.connector.AuditResult
import utils.IdGenerator
import v1andv2.controllers.requestParsers.AddUkSavingsAccountRequestParser
import v1andv2.models.request.addUkSavingsAccount.AddUkSavingsAccountRawData
import v1andv2.models.response.addUkSavingsAccount.AddUkSavingsAccountHateoasData
import v1andv2.services.AddUkSavingsAccountService

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class AddUkSavingsAccountController @Inject()(val authService: EnrolmentsAuthService,
                                              val lookupService: MtdIdLookupService,
                                              parser: AddUkSavingsAccountRequestParser,
                                              service: AddUkSavingsAccountService,
                                              auditService: AuditService,
                                              hateoasFactory: HateoasFactory,
                                              cc: ControllerComponents,
                                              val idGenerator: IdGenerator)(implicit ec: ExecutionContext)
    extends AuthorisedController(cc) {

  implicit val endpointLogContext: EndpointLogContext =
    EndpointLogContext(
      controllerName = "AddUkSavingsAccountController",
      endpointName = "addUkSavingsAccount"
    )

  def addUkSavingsAccount(nino: String): Action[JsValue] =
    authorisedAction(nino).async(parse.json) { implicit request: UserRequest[JsValue] =>
      implicit val ctx: RequestContext = RequestContext.from(idGenerator, endpointLogContext)

      val rawData: AddUkSavingsAccountRawData = AddUkSavingsAccountRawData(nino = nino, body = AnyContentAsJson(request.body))

      val requestHandler =
        RequestHandler
          .withParser(parser)
          .withService(service.addSavings)
          .withAuditing(auditHandler(nino, request))
          .withHateoasResultFrom(hateoasFactory)((_, resp) => AddUkSavingsAccountHateoasData(nino, resp.savingsAccountId))

      requestHandler.handleRequest(rawData)
    }

  private def auditHandler(nino: String, request: UserRequest[JsValue]): AuditHandler = {
    new AuditHandler() {
      override def performAudit(userDetails: UserDetails, httpStatus: Int, response: Either[ErrorWrapper, Option[JsValue]])(implicit
          ctx: RequestContext,
          ec: ExecutionContext): Unit = {

        response match {
          case Left(err: ErrorWrapper) =>
            auditSubmission(
              FlattenedGenericAuditDetail(
                Some("2.0"),
                request.userDetails,
                Map("nino" -> nino),
                Some(request.body),
                ctx.correlationId,
                AuditResponse(httpStatus = httpStatus, response = Left(err.auditErrors))
              )
            )

          case Right(resp: Option[JsValue]) =>
            val respNoHateoas = resp.map {
              case js: JsObject => js - "links"
              case js: JsValue  => js
            }

            auditSubmission(
              FlattenedGenericAuditDetail(
                versionNumber = Some("2.0"),
                request.userDetails,
                Map("nino" -> nino),
                Some(request.body),
                ctx.correlationId,
                AuditResponse(httpStatus = OK, response = Right(respNoHateoas))
              )
            )
        }
      }
    }
  }

  private def auditSubmission(details: FlattenedGenericAuditDetail)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[AuditResult] = {
    val event = AuditEvent("AddUkSavingsAccount", "add-uk-savings-account", details)
    auditService.auditEvent(event)
  }

}
