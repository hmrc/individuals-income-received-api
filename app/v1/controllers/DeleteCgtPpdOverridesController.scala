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
import utils.IdGenerator
import v1.controllers.requestParsers.DeleteCgtPpdOverridesRequestParser
import v1.models.audit.DeleteCgtPpdOverridesAuditDetail
import v1.models.request.deleteCgtPpdOverrides.DeleteCgtPpdOverridesRawData
import v1.services.DeleteCgtPpdOverridesService

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext

@Singleton
class DeleteCgtPpdOverridesController @Inject() (val authService: EnrolmentsAuthService,
                                                 val lookupService: MtdIdLookupService,
                                                 parser: DeleteCgtPpdOverridesRequestParser,
                                                 service: DeleteCgtPpdOverridesService,
                                                 auditService: AuditService,
                                                 cc: ControllerComponents,
                                                 val idGenerator: IdGenerator)(implicit ec: ExecutionContext)
    extends AuthorisedController(cc) {

  implicit val endpointLogContext: EndpointLogContext = EndpointLogContext(
    controllerName = "DeleteCgtPpdOverridesController",
    endpointName = "Delete 'Report and Pay Capital Gains Tax on Residential Property' Overrides (PPD)"
  )

  def deleteCgtPpdOverrides(nino: String, taxYear: String): Action[AnyContent] =
    authorisedAction(nino).async { implicit request =>
      implicit val ctx: RequestContext = RequestContext.from(idGenerator, endpointLogContext)

      val rawData: DeleteCgtPpdOverridesRawData = DeleteCgtPpdOverridesRawData(nino = nino, taxYear = taxYear)

      val requestHandler = RequestHandler
        .withParser(parser)
        .withService(service.deleteCgtPpdOverrides)
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
              DeleteCgtPpdOverridesAuditDetail(
                request.userDetails,
                nino,
                taxYear,
                ctx.correlationId,
                AuditResponse(httpStatus = httpStatus, response = Left(err.auditErrors))))

          case Right(_: Option[JsValue]) =>
            auditSubmission(
              DeleteCgtPpdOverridesAuditDetail(request.userDetails, nino, taxYear, ctx.correlationId, AuditResponse(NO_CONTENT, Right(None))))
        }
      }
    }
  }

  private def auditSubmission(details: DeleteCgtPpdOverridesAuditDetail)(implicit hc: HeaderCarrier, ec: ExecutionContext) = {
    val event = AuditEvent("DeleteCgtPpdOverrides", "Delete-Cgt-Ppd-Overrides", details)
    auditService.auditEvent(event)
  }

}
