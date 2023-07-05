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
import api.hateoas.HateoasFactory
import api.services.{AuditService, EnrolmentsAuthService, MtdIdLookupService}
import config.{AppConfig, FeatureSwitches}
import play.api.libs.json.JsValue
import play.api.mvc.{Action, AnyContentAsJson, ControllerComponents}
import utils.IdGenerator
import v1.controllers.requestParsers.CreateAmendNonPayeEmploymentRequestParser
import v1.models.request.createAmendNonPayeEmployment.CreateAmendNonPayeEmploymentRawData
import v1.models.response.createAmendNonPayeEmployment.CreateAmendNonPayeEmploymentHateoasData
import v1.models.response.createAmendNonPayeEmployment.CreateAmendNonPayeEmploymentResponse.CreateAmendNonPayeEmploymentLinksFactory
import v1.services.CreateAmendNonPayeEmploymentService

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext

@Singleton
class CreateAmendNonPayeEmploymentController @Inject() (val authService: EnrolmentsAuthService,
                                                        val lookupService: MtdIdLookupService,
                                                        appConfig: AppConfig,
                                                        parser: CreateAmendNonPayeEmploymentRequestParser,
                                                        service: CreateAmendNonPayeEmploymentService,
                                                        auditService: AuditService,
                                                        hateoasFactory: HateoasFactory,
                                                        cc: ControllerComponents,
                                                        val idGenerator: IdGenerator)(implicit ec: ExecutionContext)
    extends AuthorisedController(cc) {

  implicit val endpointLogContext: EndpointLogContext =
    EndpointLogContext(
      controllerName = "CreateAmendNonPayeEmploymentController",
      endpointName = "createAmendNonPayeEmployment"
    )

  def createAmendNonPayeEmployment(nino: String, taxYear: String): Action[JsValue] =
    authorisedAction(nino).async(parse.json) { implicit request =>
      implicit val ctx: RequestContext = RequestContext.from(idGenerator, endpointLogContext)

      val rawData: CreateAmendNonPayeEmploymentRawData = CreateAmendNonPayeEmploymentRawData(
        nino = nino,
        taxYear = taxYear,
        body = AnyContentAsJson(request.body),
        temporalValidationEnabled = FeatureSwitches(appConfig.featureSwitches).isTemporalValidationEnabled
      )

      val requestHandler = RequestHandler
        .withParser(parser)
        .withService(service.createAndAmend)
        .withAuditing(AuditHandler(
          auditService = auditService,
          auditType = "CreateAmendNonPayeEmployment",
          transactionName = "create-amend-non-paye-employment",
          params = Map("nino" -> nino, "taxYear" -> taxYear),
          requestBody = Some(request.body),
          includeResponse = true
        ))
        .withHateoasResult(hateoasFactory)(CreateAmendNonPayeEmploymentHateoasData(nino, taxYear))

      requestHandler.handleRequest(rawData)
    }

}
