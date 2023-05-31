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

package v1andv2.controllers

import api.controllers._
import api.hateoas.HateoasFactory
import api.models.audit.{AuditEvent, AuditResponse}
import api.models.auth.UserDetails
import api.models.errors._
import api.services.{AuditService, EnrolmentsAuthService, MtdIdLookupService, NrsProxyService}
import config.AppConfig
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.{Action, AnyContentAsJson, ControllerComponents}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.audit.http.connector.AuditResult
import utils.IdGenerator
import v1andv2.controllers.requestParsers.CreateAmendOtherCgtRequestParser
import v1andv2.models.audit.CreateAmendOtherCgtAuditDetail
import v1andv2.models.request.createAmendOtherCgt.CreateAmendOtherCgtRawData
import v1andv2.models.response.createAmendOtherCgt.CreateAmendOtherCgtHateoasData
import v1andv2.models.response.createAmendOtherCgt.CreateAmendOtherCgtResponse.CreateAmendOtherCgtLinksFactory
import v1andv2.services._

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class CreateAmendOtherCgtController @Inject() (val authService: EnrolmentsAuthService,
                                               val lookupService: MtdIdLookupService,
                                               appConfig: AppConfig,
                                               parser: CreateAmendOtherCgtRequestParser,
                                               service: CreateAmendOtherCgtService,
                                               nrsProxyService: NrsProxyService,
                                               hateoasFactory: HateoasFactory,
                                               auditService: AuditService,
                                               cc: ControllerComponents,
                                               val idGenerator: IdGenerator)(implicit ec: ExecutionContext)
    extends AuthorisedController(cc) {

  implicit val endpointLogContext: EndpointLogContext =
    EndpointLogContext(
      controllerName = "CreateAmendOtherCgtController",
      endpointName = "createAmendOtherCgt"
    )

  def createAmendOtherCgt(nino: String, taxYear: String): Action[JsValue] =
    authorisedAction(nino).async(parse.json) { implicit request =>
      implicit val ctx: RequestContext = RequestContext.from(idGenerator, endpointLogContext)

      val rawData: CreateAmendOtherCgtRawData = CreateAmendOtherCgtRawData(
        nino = nino,
        taxYear = taxYear,
        body = AnyContentAsJson(request.body)
      )

      val requestHandler = RequestHandler
        .withParser(parser)
        .withService { req =>
          nrsProxyService.submitAsync(nino, "itsa-cgt-disposal-other", request.body)
          service.createAmend(req)
        }
        .withAuditing(auditHandler(nino, taxYear, request))
        .withHateoasResult(hateoasFactory)(CreateAmendOtherCgtHateoasData(nino, taxYear))

      requestHandler.handleRequest(rawData)
    }

  private def auditHandler(nino: String, taxYear: String, request: UserRequest[JsValue]): AuditHandler = {
    new AuditHandler() {
      override def performAudit(userDetails: UserDetails, httpStatus: Int, response: Either[ErrorWrapper, Option[JsValue]])(implicit
          ctx: RequestContext,
          ec: ExecutionContext): Unit = {

        response match {
          case Left(err: ErrorWrapper) =>
            auditSubmission(
              CreateAmendOtherCgtAuditDetail(
                request.userDetails,
                nino,
                taxYear,
                request.body,
                ctx.correlationId,
                AuditResponse(httpStatus = httpStatus, Left(err.auditErrors))))

          case Right(_: Option[JsValue]) =>
            auditSubmission(
              CreateAmendOtherCgtAuditDetail(
                request.userDetails,
                nino,
                taxYear,
                request.body,
                ctx.correlationId,
                AuditResponse(OK, Right(Some(Json.toJson(CreateAmendOtherCgtHateoasData(nino, taxYear)))))
              ))
        }
      }
    }
  }

  private def auditSubmission(details: CreateAmendOtherCgtAuditDetail)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[AuditResult] = {
    val event = AuditEvent("CreateAmendOtherCgtDisposalsAndGains", "Create-Amend-Other-Cgt-Disposals-And-Gains", details)
    auditService.auditEvent(event)
  }

}
