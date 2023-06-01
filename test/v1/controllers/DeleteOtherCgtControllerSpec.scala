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
import api.mocks.MockIdGenerator
import api.mocks.services.{MockAuditService, MockEnrolmentsAuthService, MockMtdIdLookupService}
import api.models.audit.{AuditEvent, AuditResponse}
import api.models.domain.{Nino, TaxYear}
import api.models.errors._
import api.models.outcomes.ResponseWrapper
import play.api.libs.json.JsValue
import play.api.mvc.Result
import v1.mocks.requestParsers.MockDeleteOtherCgtRequestParser
import v1.mocks.services.MockDeleteOtherCgtService
import v1.models.audit.DeleteOtherCgtAuditDetail
import v1.models.request.deleteOtherCgt.{DeleteOtherCgtRawData, DeleteOtherCgtRequest}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class DeleteOtherCgtControllerSpec
    extends ControllerBaseSpec
    with ControllerTestRunner
    with MockEnrolmentsAuthService
    with MockMtdIdLookupService
    with MockDeleteOtherCgtService
    with MockAuditService
    with MockDeleteOtherCgtRequestParser
    with MockIdGenerator {

  val taxYear: String = "2019-20"

  val rawData: DeleteOtherCgtRawData = DeleteOtherCgtRawData(
    nino = nino,
    taxYear = taxYear
  )

  val requestData: DeleteOtherCgtRequest = DeleteOtherCgtRequest(
    nino = Nino(nino),
    taxYear = TaxYear.fromMtd(taxYear)
  )

  "DeleteOtherCgtController" should {
    "return a successful response with status 204 (No Content)" when {
      "a valid request is supplied" in new Test {
        MockDeleteOtherCgtRequestParser
          .parse(rawData)
          .returns(Right(requestData))

        MockDeleteOtherCgtService
          .delete(requestData)
          .returns(Future.successful(Right(ResponseWrapper(correlationId, ()))))

        runOkTestWithAudit(expectedStatus = NO_CONTENT)
      }
    }

    "return the error as per spec" when {
      "the parser validation fails" in new Test {
        MockDeleteOtherCgtRequestParser
          .parse(rawData)
          .returns(Left(ErrorWrapper(correlationId, NinoFormatError)))

        runErrorTestWithAudit(NinoFormatError)
      }

      "service returns an error" in new Test {
        MockDeleteOtherCgtRequestParser
          .parse(rawData)
          .returns(Right(requestData))

        MockDeleteOtherCgtService
          .delete(requestData)
          .returns(Future.successful(Left(ErrorWrapper(correlationId, RuleTaxYearNotSupportedError))))

        runErrorTestWithAudit(RuleTaxYearNotSupportedError)
      }
    }
  }

  trait Test extends ControllerTest with AuditEventChecking[DeleteOtherCgtAuditDetail] {

    val controller = new DeleteOtherCgtController(
      authService = mockEnrolmentsAuthService,
      lookupService = mockMtdIdLookupService,
      parser = mockDeleteOtherCgtRequestParser,
      service = mockDeleteOtherCgtService,
      auditService = mockAuditService,
      cc = cc,
      idGenerator = mockIdGenerator
    )

    protected def callController(): Future[Result] = controller.deleteOtherCgt(nino, taxYear)(fakeDeleteRequest)

    def event(auditResponse: AuditResponse, maybeRequestBody: Option[JsValue]): AuditEvent[DeleteOtherCgtAuditDetail] =
      AuditEvent(
        auditType = "DeleteOtherCgtDisposalsAndGains",
        transactionName = "Delete-Other-Cgt-Disposals-And-Gains",
        detail = DeleteOtherCgtAuditDetail(
          userType = "Individual",
          agentReferenceNumber = None,
          nino,
          taxYear,
          correlationId,
          response = auditResponse
        )
      )

  }

}
