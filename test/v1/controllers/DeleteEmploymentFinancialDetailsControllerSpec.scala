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

import api.controllers.{ControllerBaseSpec, ControllerTestRunner}
import api.mocks.services.MockAuditService
import api.models.audit.{AuditEvent, AuditResponse, GenericAuditDetail}
import api.models.domain.{Nino, TaxYear}
import api.models.errors._
import api.models.outcomes.ResponseWrapper
import play.api.libs.json.JsValue
import play.api.mvc.Result
import v1.mocks.requestParsers.MockDeleteEmploymentFinancialDetailsRequestParser
import v1.mocks.services.MockDeleteEmploymentFinancialDetailsService
import v1.models.request.deleteEmploymentFinancialDetails.{DeleteEmploymentFinancialDetailsRawData, DeleteEmploymentFinancialDetailsRequest}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class DeleteEmploymentFinancialDetailsControllerSpec
    extends ControllerBaseSpec
    with ControllerTestRunner
    with MockDeleteEmploymentFinancialDetailsService
    with MockAuditService
    with MockDeleteEmploymentFinancialDetailsRequestParser {

  val taxYear: String      = "2019-20"
  val employmentId: String = "4557ecb5-fd32-48cc-81f5-e6acd1099f3c"

  val rawData: DeleteEmploymentFinancialDetailsRawData = DeleteEmploymentFinancialDetailsRawData(
    nino = nino,
    taxYear = taxYear,
    employmentId = employmentId
  )

  val requestData: DeleteEmploymentFinancialDetailsRequest = DeleteEmploymentFinancialDetailsRequest(
    nino = Nino(nino),
    taxYear = TaxYear.fromMtd(taxYear),
    employmentId = employmentId
  )

  "deleteEmploymentFinancialDetailsController" should {
    "return NO_content" when {
      "happy path" in new Test {
        MockDeleteEmploymentFinancialDetailsRequestParser
          .parse(rawData)
          .returns(Right(requestData))

        MockDeleteEmploymentFinancialDetailsService
          .delete(requestData)
          .returns(Future.successful(Right(ResponseWrapper(correlationId, ()))))

        runOkTestWithAudit(expectedStatus = NO_CONTENT)
      }
    }

    "return the error as per spec" when {
      "the parser validation fails" in new Test {
        MockDeleteEmploymentFinancialDetailsRequestParser
          .parse(rawData)
          .returns(Left(ErrorWrapper(correlationId, NinoFormatError, None)))

        runErrorTestWithAudit(NinoFormatError)
      }

      "service returns an error" in new Test {
        MockDeleteEmploymentFinancialDetailsRequestParser
          .parse(rawData)
          .returns(Right(requestData))

        MockDeleteEmploymentFinancialDetailsService
          .delete(requestData)
          .returns(Future.successful(Left(ErrorWrapper(correlationId, RuleTaxYearNotSupportedError))))

        runErrorTestWithAudit(RuleTaxYearNotSupportedError)
      }
    }
  }

  trait Test extends ControllerTest with AuditEventChecking[GenericAuditDetail] {

    val controller = new DeleteEmploymentFinancialDetailsController(
      authService = mockEnrolmentsAuthService,
      lookupService = mockMtdIdLookupService,
      parser = mockDeleteEmploymentFinancialDetailsRequestParser,
      service = mockDeleteEmploymentFinancialDetailsService,
      auditService = mockAuditService,
      cc = cc,
      idGenerator = mockIdGenerator
    )

    protected def callController(): Future[Result] = controller.deleteEmploymentFinancialDetails(nino, taxYear, employmentId)(fakeDeleteRequest)

    def event(auditResponse: AuditResponse, requestBody: Option[JsValue]): AuditEvent[GenericAuditDetail] =
      AuditEvent(
        auditType = "DeleteEmploymentFinancialDetails",
        transactionName = "delete-employment-financial-details",
        detail = GenericAuditDetail(
          userType = "Individual",
          agentReferenceNumber = None,
          params = Map("nino" -> nino, "taxYear" -> taxYear, "employmentId" -> employmentId),
          request = requestBody,
          `X-CorrelationId` = correlationId,
          response = auditResponse
        )
      )

  }

}
