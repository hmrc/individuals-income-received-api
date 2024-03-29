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
import play.api.libs.json.JsValue
import play.api.mvc.{Action, AnyContentAsJson, ControllerComponents}
import utils.IdGenerator
import v1.controllers.requestParsers.AddUkSavingsAccountRequestParser
import v1.models.request.addUkSavingsAccount.AddUkSavingsAccountRawData
import v1.models.response.addUkSavingsAccount.AddUkSavingsAccountHateoasData
import v1.services.AddUkSavingsAccountService

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext

@Singleton
class AddUkSavingsAccountController @Inject() (val authService: EnrolmentsAuthService,
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
          .withAuditing(AuditHandler.flattenedAuditing(
            auditService = auditService,
            auditType = "AddUkSavingsAccount",
            transactionName = "add-uk-savings-account",
            params = Map("versionNumber" -> "1.0", "nino" -> nino),
            requestBody = Some(request.body),
            includeResponse = true
          ))
          .withHateoasResultFrom(hateoasFactory)((_, resp) => AddUkSavingsAccountHateoasData(nino, resp.savingsAccountId))

      requestHandler.handleRequest(rawData)
    }

}
