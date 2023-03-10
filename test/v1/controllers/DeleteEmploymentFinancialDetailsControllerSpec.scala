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

import api.controllers.ControllerBaseSpec
import api.mocks.MockIdGenerator
import api.mocks.services.{MockAuditService, MockEnrolmentsAuthService, MockMtdIdLookupService}
import api.models.audit.{AuditError, AuditEvent, AuditResponse, GenericAuditDetail}
import api.models.domain.{Nino, TaxYear}
import api.models.errors._
import api.models.outcomes.ResponseWrapper
import play.api.libs.json.Json
import play.api.mvc.Result
import uk.gov.hmrc.http.HeaderCarrier
import v1.mocks.requestParsers.MockDeleteEmploymentFinancialDetailsRequestParser
import v1.mocks.services.MockDeleteEmploymentFinancialDetailsService
import v1.models.request.deleteEmploymentFinancialDetails.{DeleteEmploymentFinancialDetailsRawData, DeleteEmploymentFinancialDetailsRequest}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class DeleteEmploymentFinancialDetailsControllerSpec
    extends ControllerBaseSpec
    with MockEnrolmentsAuthService
    with MockMtdIdLookupService
    with MockDeleteEmploymentFinancialDetailsService
    with MockAuditService
    with MockDeleteEmploymentFinancialDetailsRequestParser
    with MockIdGenerator {

  val nino: String          = "AA123456A"
  val taxYear: String       = "2019-20"
  val employmentId: String  = "4557ecb5-fd32-48cc-81f5-e6acd1099f3c"
  val correlationId: String = "a1e8057e-fbbc-47a8-a8b4-78d9f015c253"

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

  def event(auditResponse: AuditResponse): AuditEvent[GenericAuditDetail] =
    AuditEvent(
      auditType = "DeleteEmploymentFinancialDetails",
      transactionName = "delete-employment-financial-details",
      detail = GenericAuditDetail(
        "Individual",
        None,
        Map("nino" -> nino, "taxYear" -> taxYear, "employmentId" -> employmentId),
        None,
        correlationId,
        auditResponse
      )
    )

  trait Test {
    val hc: HeaderCarrier = HeaderCarrier()

    val controller = new DeleteEmploymentFinancialDetailsController(
      authService = mockEnrolmentsAuthService,
      lookupService = mockMtdIdLookupService,
      requestParser = mockDeleteEmploymentFinancialDetailsRequestParser,
      service = mockDeleteEmploymentFinancialDetailsService,
      auditService = mockAuditService,
      cc = cc,
      idGenerator = mockIdGenerator
    )

    MockedMtdIdLookupService.lookup(nino).returns(Future.successful(Right("test-mtd-id")))
    MockedEnrolmentsAuthService.authoriseUser()
    MockIdGenerator.generateCorrelationId.returns(correlationId)
  }

  "deleteEmploymentFinancialDetailsController" should {
    "return NO_content" when {
      "happy path" in new Test {

        MockDeleteEmploymentFinancialDetailsRequestParser
          .parse(rawData)
          .returns(Right(requestData))

        MockDeleteEmploymentFinancialDetailsService
          .delete(requestData)
          .returns(Future.successful(Right(ResponseWrapper(correlationId, ()))))

        val result: Future[Result] = controller.deleteEmploymentFinancialDetails(nino, taxYear, employmentId)(fakeDeleteRequest)

        status(result) shouldBe NO_CONTENT
        contentAsString(result) shouldBe ""
        header("X-CorrelationId", result) shouldBe Some(correlationId)

        val auditResponse: AuditResponse = AuditResponse(NO_CONTENT, None, None)
        MockedAuditService.verifyAuditEvent(event(auditResponse)).once
      }
    }

    "return the error as per spec" when {
      "parser errors occur" must {
        def errorsFromParserTester(error: MtdError, expectedStatus: Int): Unit = {
          s"a ${error.code} error is returned from the parser" in new Test {

            MockDeleteEmploymentFinancialDetailsRequestParser
              .parse(rawData)
              .returns(Left(ErrorWrapper(correlationId, error, None)))

            val result: Future[Result] = controller.deleteEmploymentFinancialDetails(nino, taxYear, employmentId)(fakeDeleteRequest)

            status(result) shouldBe expectedStatus
            contentAsJson(result) shouldBe Json.toJson(error)
            header("X-CorrelationId", result) shouldBe Some(correlationId)

            val auditResponse: AuditResponse = AuditResponse(expectedStatus, Some(Seq(AuditError(error.code))), None)
            MockedAuditService.verifyAuditEvent(event(auditResponse)).once
          }
        }

        val input = Seq(
          (BadRequestError, BAD_REQUEST),
          (NinoFormatError, BAD_REQUEST),
          (TaxYearFormatError, BAD_REQUEST),
          (EmploymentIdFormatError, BAD_REQUEST),
          (RuleTaxYearNotSupportedError, BAD_REQUEST),
          (RuleTaxYearRangeInvalidError, BAD_REQUEST)
        )

        input.foreach(args => (errorsFromParserTester _).tupled(args))
      }

      "service errors occur" must {
        def serviceErrors(mtdError: MtdError, expectedStatus: Int): Unit = {
          s"a $mtdError error is returned from the service" in new Test {

            MockDeleteEmploymentFinancialDetailsRequestParser
              .parse(rawData)
              .returns(Right(requestData))

            MockDeleteEmploymentFinancialDetailsService
              .delete(requestData)
              .returns(Future.successful(Left(ErrorWrapper(correlationId, mtdError))))

            val result: Future[Result] = controller.deleteEmploymentFinancialDetails(nino, taxYear, employmentId)(fakeDeleteRequest)

            status(result) shouldBe expectedStatus
            contentAsJson(result) shouldBe Json.toJson(mtdError)
            header("X-CorrelationId", result) shouldBe Some(correlationId)

            val auditResponse: AuditResponse = AuditResponse(expectedStatus, Some(Seq(AuditError(mtdError.code))), None)
            MockedAuditService.verifyAuditEvent(event(auditResponse)).once
          }
        }

        val input = Seq(
          (NinoFormatError, BAD_REQUEST),
          (TaxYearFormatError, BAD_REQUEST),
          (EmploymentIdFormatError, BAD_REQUEST),
          (NotFoundError, NOT_FOUND),
          (InternalError, INTERNAL_SERVER_ERROR),
          (RuleTaxYearNotSupportedError, BAD_REQUEST)
        )

        input.foreach(args => (serviceErrors _).tupled(args))
      }
    }
  }

}
