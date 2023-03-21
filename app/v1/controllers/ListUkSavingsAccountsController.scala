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

import api.controllers.{AuthorisedController, EndpointLogContext, RequestContext, RequestHandler}
import api.hateoas.HateoasFactory
import api.services.{EnrolmentsAuthService, MtdIdLookupService}
import config.AppConfig
import play.api.mvc.{Action, AnyContent, ControllerComponents}
import utils.{IdGenerator, Logging}
import v1.controllers.requestParsers.ListUkSavingsAccountsRequestParser
import v1.models.request.listUkSavingsAccounts.ListUkSavingsAccountsRawData
import v1.models.response.listUkSavingsAccounts.ListUkSavingsAccountsHateoasData
import v1.services.ListUkSavingsAccountsService

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext

@Singleton
class ListUkSavingsAccountsController @Inject() (val authService: EnrolmentsAuthService,
                                                 val lookupService: MtdIdLookupService,
                                                 requestParser: ListUkSavingsAccountsRequestParser,
                                                 service: ListUkSavingsAccountsService,
                                                 appConfig: AppConfig,
                                                 hateoasFactory: HateoasFactory,
                                                 cc: ControllerComponents,
                                                 val idGenerator: IdGenerator)(implicit ec: ExecutionContext)
    extends AuthorisedController(cc)
    with Logging {

  implicit val endpointLogContext: EndpointLogContext =
    EndpointLogContext(
      controllerName = "ListUkSavingsAccountsController",
      endpointName = "listUkSavingsAccounts"
    )

  def listUkSavingsAccounts(nino: String, savingsAccountId: Option[String]): Action[AnyContent] =
    authorisedAction(nino).async { implicit request =>
      implicit val ctx: RequestContext = RequestContext.from(idGenerator, endpointLogContext)

      val rawData: ListUkSavingsAccountsRawData = ListUkSavingsAccountsRawData(
        nino = nino,
        savingsAccountId = savingsAccountId
      )

      val requestHandler =
        RequestHandler
          .withParser(requestParser)
          .withService(service.listUkSavingsAccounts)
          .withHateoasResult(hateoasFactory)(ListUkSavingsAccountsHateoasData(nino))

      requestHandler.handleRequest(rawData)
    }

}
