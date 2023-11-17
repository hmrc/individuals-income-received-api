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
import v1.controllers.requestParsers.AmendCustomEmploymentRequestParser
import v1.models.request.amendCustomEmployment.AmendCustomEmploymentRawData
import v1.models.response.amendCustomEmployment.AmendCustomEmploymentHateoasData
import v1.models.response.amendCustomEmployment.AmendCustomEmploymentResponse.AmendCustomEmploymentLinksFactory
import v1.services.AmendCustomEmploymentService

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext

@Singleton
class AmendCustomEmploymentController @Inject()(val authService: EnrolmentsAuthService,
                                                val lookupService: MtdIdLookupService,
                                                appConfig: AppConfig,
                                                parser: AmendCustomEmploymentRequestParser,
                                                service: AmendCustomEmploymentService,
                                                auditService: AuditService,
                                                hateoasFactory: HateoasFactory,
                                                cc: ControllerComponents,
                                                val idGenerator: IdGenerator)(implicit ec: ExecutionContext)
  extends AuthorisedController(cc) {

  implicit val endpointLogContext: EndpointLogContext =
    EndpointLogContext(
      controllerName = "AmendCustomEmploymentController",
      endpointName = "amendCustomEmployment"
    )

  def amendEmployment(nino: String, taxYear: String, employmentId: String): Action[JsValue] =
    authorisedAction(nino).async(parse.json) { implicit request =>
      implicit val ctx: RequestContext = RequestContext.from(idGenerator, endpointLogContext)

      val rawData: AmendCustomEmploymentRawData = AmendCustomEmploymentRawData(
        nino = nino,
        taxYear = taxYear,
        employmentId = employmentId,
        body = AnyContentAsJson(request.body),
        temporalValidationEnabled = FeatureSwitches(appConfig.featureSwitches).isTemporalValidationEnabled
      )

      val requestHandler = RequestHandler
        .withParser(parser)
        .withService(service.amendEmployment)
        .withAuditing(AuditHandler(
          auditService = auditService,
          auditType = "AmendACustomEmployment",
          transactionName = "amend-a-custom-employment",
          params = Map("nino" -> nino, "taxYear" -> taxYear, "employmentId" -> employmentId),
          requestBody = Some(request.body),
          includeResponse = true
        ))
        .withHateoasResult(hateoasFactory)(AmendCustomEmploymentHateoasData(nino, taxYear, employmentId))

      requestHandler.handleRequest(rawData)
    }

}
