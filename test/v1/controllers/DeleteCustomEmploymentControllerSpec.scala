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
import api.models.domain.Nino
import api.models.errors._
import api.models.outcomes.ResponseWrapper
import play.api.libs.json.JsValue
import play.api.mvc.Result
import v1.mocks.requestParsers.MockDeleteCustomEmploymentRequestParser
import v1.mocks.services.MockDeleteCustomEmploymentService
import v1.models.request.deleteCustomEmployment.{DeleteCustomEmploymentRawData, DeleteCustomEmploymentRequest}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class DeleteCustomEmploymentControllerSpec
    extends ControllerBaseSpec
    with ControllerTestRunner
    with MockDeleteCustomEmploymentService
    with MockAuditService
    with MockDeleteCustomEmploymentRequestParser {

  val taxYear: String      = "2019-20"
  val employmentId: String = "4557ecb5-fd32-48cc-81f5-e6acd1099f3c"

  val rawData: DeleteCustomEmploymentRawData = DeleteCustomEmploymentRawData(
    nino = nino,
    taxYear = taxYear,
    employmentId = employmentId
  )

  val requestData: DeleteCustomEmploymentRequest = DeleteCustomEmploymentRequest(
    nino = Nino(nino),
    taxYear = taxYear,
    employmentId = employmentId
  )

  "DeleteCustomEmploymentController" should {
    "return NO_content" when {
      "happy path" in new Test {
        MockDeleteCustomEmploymentRequestParser
          .parse(rawData)
          .returns(Right(requestData))

        MockDeleteCustomEmploymentService
          .delete(requestData)
          .returns(Future.successful(Right(ResponseWrapper(correlationId, ()))))

        runOkTestWithAudit(expectedStatus = NO_CONTENT)
      }
    }

    "return the error as per spec" when {
      "the parser validation fails" in new Test {
        MockDeleteCustomEmploymentRequestParser
          .parse(rawData)
          .returns(Left(ErrorWrapper(correlationId, NinoFormatError)))

        runErrorTestWithAudit(NinoFormatError)
      }

      "service returns an error" in new Test {
        MockDeleteCustomEmploymentRequestParser
          .parse(rawData)
          .returns(Right(requestData))

        MockDeleteCustomEmploymentService
          .delete(requestData)
          .returns(Future.successful(Left(ErrorWrapper(correlationId, NotFoundError))))

        runErrorTestWithAudit(NotFoundError)
      }
    }
  }

  trait Test extends ControllerTest with AuditEventChecking[GenericAuditDetail] {

    val controller = new DeleteCustomEmploymentController(
      authService = mockEnrolmentsAuthService,
      lookupService = mockMtdIdLookupService,
      parser = mockDeleteCustomEmploymentRequestParser,
      service = mockDeleteCustomEmploymentService,
      auditService = mockAuditService,
      cc = cc,
      idGenerator = mockIdGenerator
    )

    protected def callController(): Future[Result] = controller.deleteCustomEmployment(nino, taxYear, employmentId)(fakeDeleteRequest)

    def event(auditResponse: AuditResponse, requestBody: Option[JsValue]): AuditEvent[GenericAuditDetail] =
      AuditEvent(
        auditType = "DeleteACustomEmployment",
        transactionName = "delete-a-custom-employment",
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
