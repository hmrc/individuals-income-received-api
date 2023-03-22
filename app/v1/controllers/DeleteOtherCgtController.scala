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
import utils.IdGenerator
import v1.controllers.requestParsers.DeleteOtherCgtRequestParser
import v1.models.audit.DeleteOtherCgtAuditDetail
import v1.models.request.deleteOtherCgt.DeleteOtherCgtRawData
import v1.services.DeleteOtherCgtService

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class DeleteOtherCgtController @Inject() (val authService: EnrolmentsAuthService,
                                          val lookupService: MtdIdLookupService,
                                          parser: DeleteOtherCgtRequestParser,
                                          service: DeleteOtherCgtService,
                                          auditService: AuditService,
                                          cc: ControllerComponents,
                                          val idGenerator: IdGenerator)(implicit ec: ExecutionContext)
    extends AuthorisedController(cc) {

  implicit val endpointLogContext: EndpointLogContext =
    EndpointLogContext(
      controllerName = "DeleteOtherCgtController",
      endpointName = "deleteOtherCgt"
    )

  def deleteOtherCgt(nino: String, taxYear: String): Action[AnyContent] =
    authorisedAction(nino).async { implicit request =>
      implicit val ctx: RequestContext = RequestContext.from(idGenerator, endpointLogContext)

      val rawData: DeleteOtherCgtRawData = DeleteOtherCgtRawData(
        nino = nino,
        taxYear = taxYear
      )

      val requestHandler =
        RequestHandler
          .withParser(parser)
          .withService(service.delete)
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
              DeleteOtherCgtAuditDetail(
                request.userDetails,
                nino,
                taxYear,
                ctx.correlationId,
                AuditResponse(httpStatus = httpStatus, response = Left(err.auditErrors))))

          case Right(_: Option[JsValue]) =>
            auditSubmission(DeleteOtherCgtAuditDetail(request.userDetails, nino, taxYear, ctx.correlationId, AuditResponse(NO_CONTENT, Right(None))))
        }
      }
    }
  }

  private def auditSubmission(details: DeleteOtherCgtAuditDetail)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[AuditResult] = {
    val event = AuditEvent("DeleteOtherCgtDisposalsAndGains", "Delete-Other-Cgt-Disposals-And-Gains", details)
    auditService.auditEvent(event)
  }

}
