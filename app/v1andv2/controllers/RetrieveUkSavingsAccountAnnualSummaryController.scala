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

import api.controllers.{AuthorisedController, EndpointLogContext, RequestContext, RequestHandler}
import api.hateoas.HateoasFactory
import api.services.{EnrolmentsAuthService, MtdIdLookupService}
import play.api.mvc.{Action, AnyContent, ControllerComponents}
import utils.IdGenerator
import v1andv2.controllers.requestParsers.RetrieveUkSavingsAccountRequestParser
import v1andv2.models.request.retrieveUkSavingsAnnualSummary.RetrieveUkSavingsAnnualSummaryRawData
import v1andv2.models.response.retrieveUkSavingsAnnualSummary.RetrieveUkSavingsAnnualSummaryResponseHateoasData
import v1andv2.services.RetrieveUkSavingsAccountAnnualSummaryService

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext

@Singleton
class RetrieveUkSavingsAccountAnnualSummaryController @Inject() (val authService: EnrolmentsAuthService,
                                                                 val lookupService: MtdIdLookupService,
                                                                 parser: RetrieveUkSavingsAccountRequestParser,
                                                                 service: RetrieveUkSavingsAccountAnnualSummaryService,
                                                                 hateoasFactory: HateoasFactory,
                                                                 cc: ControllerComponents,
                                                                 val idGenerator: IdGenerator)(implicit ec: ExecutionContext)
    extends AuthorisedController(cc) {

  implicit val endpointLogContext: EndpointLogContext =
    EndpointLogContext(
      controllerName = "RetrieveUkSavingsAccountAnnualSummaryController",
      endpointName = "retrieveUkSavingAccountSummary"
    )

  def retrieveUkSavingAccount(nino: String, taxYear: String, savingsAccountId: String): Action[AnyContent] =
    authorisedAction(nino).async { implicit request =>
      implicit val ctx: RequestContext = RequestContext.from(idGenerator, endpointLogContext)

      val rawData: RetrieveUkSavingsAnnualSummaryRawData = RetrieveUkSavingsAnnualSummaryRawData(nino, taxYear, savingsAccountId)

      val requestHandler =
        RequestHandler
          .withParser(parser)
          .withService(service.retrieveUkSavingsAccountAnnualSummary)
          .withHateoasResult(hateoasFactory)(RetrieveUkSavingsAnnualSummaryResponseHateoasData(nino, taxYear, savingsAccountId))

      requestHandler.handleRequest(rawData)
    }

}
