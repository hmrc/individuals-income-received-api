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
import api.services.{AuditService, EnrolmentsAuthService, MtdIdLookupService}
import config.{AppConfig, FeatureSwitches}
import play.api.libs.json.JsValue
import play.api.mvc.{Action, AnyContentAsJson, ControllerComponents}
import utils.IdGenerator
import v2.controllers.requestParsers.CreateAmendOtherRequestParser
import v2.models.request.createAmendOther.CreateAmendOtherRawData
import v2.models.response.createAmendOther.CreateAmendOtherHateoasData
import v2.models.response.createAmendOther.CreateAmendOtherResponse.CreateAmendOtherLinksFactory
import v2.services.CreateAmendOtherService

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext

@Singleton
class CreateAmendOtherController @Inject() (val authService: EnrolmentsAuthService,
                                            val lookupService: MtdIdLookupService,
                                            parser: CreateAmendOtherRequestParser,
                                            service: CreateAmendOtherService,
                                            auditService: AuditService,
                                            hateoasFactory: HateoasFactory,
                                            appConfig: AppConfig,
                                            cc: ControllerComponents,
                                            val idGenerator: IdGenerator)(implicit ec: ExecutionContext)
    extends AuthorisedController(cc) {

  implicit val endpointLogContext: EndpointLogContext =
    EndpointLogContext(
      controllerName = "AmendOtherController",
      endpointName = "amendOther"
    )

  def createAmendOther(nino: String, taxYear: String): Action[JsValue] =
    authorisedAction(nino).async(parse.json) { implicit request =>
      implicit val ctx: RequestContext              = RequestContext.from(idGenerator, endpointLogContext)
      implicit val featureSwitches: FeatureSwitches = FeatureSwitches()(appConfig)

      val rawData: CreateAmendOtherRawData = CreateAmendOtherRawData(
        nino = nino,
        taxYear = taxYear,
        body = AnyContentAsJson(request.body)
      )

      val requestHandler = RequestHandler
        .withParser(parser)
        .withService(service.createAmend)
        .withAuditing(AuditHandler(
          auditService = auditService,
          auditType = "CreateAmendOtherIncome",
          transactionName = "create-amend-other-income",
          params = Map("nino" -> nino, "taxYear" -> taxYear),
          requestBody = Some(request.body),
          includeResponse = true
        ))
        .withHateoasResult(hateoasFactory)(CreateAmendOtherHateoasData(nino, taxYear))

      requestHandler.handleRequest(rawData)
    }

}
