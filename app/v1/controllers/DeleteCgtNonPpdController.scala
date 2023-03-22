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

import api.controllers._
import api.models.audit.{AuditEvent, AuditResponse}
import api.models.auth.UserDetails
import api.models.errors._
import api.services.{AuditService, EnrolmentsAuthService, MtdIdLookupService}
import play.api.libs.json.JsValue
import play.api.mvc.{Action, AnyContent, ControllerComponents}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.audit.http.connector.AuditResult
import utils.{IdGenerator, Logging}
import v1.controllers.requestParsers.DeleteCgtNonPpdRequestParser
import v1.models.audit.DeleteCgtNonPpdAuditDetail
import v1.models.request.deleteCgtNonPpd.DeleteCgtNonPpdRawData
import v1.services.DeleteCgtNonPpdService

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class DeleteCgtNonPpdController @Inject() (val authService: EnrolmentsAuthService,
                                           val lookupService: MtdIdLookupService,
                                           parser: DeleteCgtNonPpdRequestParser,
                                           service: DeleteCgtNonPpdService,
                                           auditService: AuditService,
                                           cc: ControllerComponents,
                                           val idGenerator: IdGenerator)(implicit ec: ExecutionContext)
    extends AuthorisedController(cc)
    with BaseController
    with Logging {

  implicit val endpointLogContext: EndpointLogContext =
    EndpointLogContext(
      controllerName = "DeleteCgtNonPpdController",
      endpointName = "deleteCgtResidentialPropertyDisposals"
    )

  def deleteCgtNonPpd(nino: String, taxYear: String): Action[AnyContent] =
    authorisedAction(nino).async { implicit request =>
      implicit val ctx: RequestContext = RequestContext.from(idGenerator, endpointLogContext)

      val rawData: DeleteCgtNonPpdRawData = DeleteCgtNonPpdRawData(nino, taxYear)

      val requestHandler = RequestHandler
        .withParser(parser)
        .withService(service.deleteCgtNonPpd)
        .withAuditing(auditHandler(nino, taxYear, request))

      requestHandler.handleRequest(rawData)
    }

  private def auditHandler(nino: String, taxYear: String, request: UserRequest[AnyContent]): AuditHandler = {
    new AuditHandler() {
      override def performAudit(userDetails: UserDetails, httpStatus: Int, response: Either[ErrorWrapper, Option[JsValue]])(implicit
          ctx: RequestContext,
          ec: ExecutionContext): Unit = {

        response match {
          case Left(err: ErrorWrapper) =>
            auditSubmission(
              DeleteCgtNonPpdAuditDetail(
                request.userDetails,
                nino,
                taxYear,
                ctx.correlationId,
                AuditResponse(httpStatus = httpStatus, response = Left(err.auditErrors))))

          case Right(_: Option[JsValue]) =>
            auditSubmission(
              DeleteCgtNonPpdAuditDetail(
                request.userDetails,
                nino,
                taxYear,
                ctx.correlationId,
                AuditResponse(NO_CONTENT, Right(None))
              ))
        }
      }
    }
  }

  private def auditSubmission(details: DeleteCgtNonPpdAuditDetail)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[AuditResult] = {
    val event = AuditEvent("DeleteCgtNonPpd", "Delete-Cgt-Non-Ppd", details)
    auditService.auditEvent(event)
  }

}
