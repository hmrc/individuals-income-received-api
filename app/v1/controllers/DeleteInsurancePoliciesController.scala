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
import api.services.{AuditService, EnrolmentsAuthService, MtdIdLookupService}
import play.api.mvc.{Action, AnyContent, ControllerComponents}
import utils.IdGenerator
import v1.controllers.requestParsers.DeleteInsurancePoliciesRequestParser
import v1.models.request.deleteInsurancePolicies.DeleteInsurancePoliciesRawData
import v1.services.DeleteInsurancePoliciesService

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext

@Singleton
class DeleteInsurancePoliciesController @Inject() (val authService: EnrolmentsAuthService,
                                                   val lookupService: MtdIdLookupService,
                                                   parser: DeleteInsurancePoliciesRequestParser,
                                                   service: DeleteInsurancePoliciesService,
                                                   auditService: AuditService,
                                                   cc: ControllerComponents,
                                                   val idGenerator: IdGenerator)(implicit ec: ExecutionContext)
    extends AuthorisedController(cc) {

  implicit val endpointLogContext: EndpointLogContext =
    EndpointLogContext(
      controllerName = "DeleteInsurancePoliciesController",
      endpointName = "delete"
    )

  def delete(nino: String, taxYear: String): Action[AnyContent] = authorisedAction(nino).async { implicit request =>
    implicit val ctx: RequestContext = RequestContext.from(idGenerator, endpointLogContext)

    val rawData: DeleteInsurancePoliciesRawData = DeleteInsurancePoliciesRawData(nino = nino, taxYear = taxYear)

    val requestHandler = RequestHandler
      .withParser(parser)
      .withService(service.delete)
      .withAuditing(AuditHandler(
        auditService = auditService,
        auditType = "DeleteInsurancePolicies",
        transactionName = "delete-insurance-policies",
        params = Map("nino" -> nino, "taxYear" -> taxYear),
        requestBody = None
      ))

    requestHandler.handleRequest(rawData)
  }

}
