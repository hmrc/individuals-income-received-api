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
import api.models.hateoas.RelType.CREATE_AND_AMEND_UK_SAVINGS
import api.services.{AuditService, EnrolmentsAuthService, MtdIdLookupService}
import play.api.libs.json.JsValue
import play.api.mvc.{Action, AnyContentAsJson, ControllerComponents}
import utils.IdGenerator
import v1.controllers.requestParsers.CreateAmendUkSavingsAccountAnnualSummaryRequestParser
import v1.models.request.createAmendUkSavingsAnnualSummary.CreateAmendUkSavingsAnnualSummaryRawData
import v1.models.response.createAmendUkSavingsIncomeAnnualSummary.CreateAndAmendUkSavingsAnnualSummaryHateoasData
import v1.services.CreateAmendUkSavingsAnnualSummaryService

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext

@Singleton
class CreateAmendUkSavingsAnnualSummaryController @Inject()(val authService: EnrolmentsAuthService,
                                                            val lookupService: MtdIdLookupService,
                                                            parser: CreateAmendUkSavingsAccountAnnualSummaryRequestParser,
                                                            service: CreateAmendUkSavingsAnnualSummaryService,
                                                            auditService: AuditService,
                                                            hateoasFactory: HateoasFactory,
                                                            cc: ControllerComponents,
                                                            val idGenerator: IdGenerator)(implicit ec: ExecutionContext)
    extends AuthorisedController(cc) {

  implicit val endpointLogContext: EndpointLogContext =
    EndpointLogContext(
      controllerName = "CreateAmendUkSavingsAnnualSummaryController",
      endpointName = "createAmendUkSavingsAnnualSummary"
    )

  def createAmendUkSavingsAnnualSummary(nino: String, taxYear: String, savingsAccountId: String): Action[JsValue] =
    authorisedAction(nino).async(parse.json) { implicit request =>
      implicit val ctx: RequestContext = RequestContext.from(idGenerator, endpointLogContext)

      val rawData = CreateAmendUkSavingsAnnualSummaryRawData(
        nino = nino,
        taxYear = taxYear,
        savingsAccountId = savingsAccountId,
        body = AnyContentAsJson(request.body)
      )

      val requestHandler = RequestHandler
        .withParser(parser)
        .withService(service.createAmend)
        .withAuditing(AuditHandler.flattenedAuditing(
          auditService = auditService,
          auditType = "createAmendUkSavingsAnnualSummary",
          transactionName = CREATE_AND_AMEND_UK_SAVINGS,
          params = Map("versionNumber" -> "2.0", "nino" -> nino, "taxYear" -> taxYear, "savingsAccountId" -> savingsAccountId),
          requestBody = Some(request.body),
          includeResponse = true
        ))        .withHateoasResult(hateoasFactory)(CreateAndAmendUkSavingsAnnualSummaryHateoasData(nino, taxYear, savingsAccountId))

      requestHandler.handleRequest(rawData)
    }

}
