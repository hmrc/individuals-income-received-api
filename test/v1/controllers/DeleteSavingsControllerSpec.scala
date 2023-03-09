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
import v1.mocks.requestParsers.MockDeleteSavingsRequestParser
import v1.mocks.services.MockDeleteSavingsService
import v1.models.request.deleteSavings.{DeleteSavingsRawData, DeleteSavingsRequest}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class DeleteSavingsControllerSpec
    extends ControllerBaseSpec
    with MockEnrolmentsAuthService
    with MockMtdIdLookupService
    with MockDeleteSavingsService
    with MockDeleteSavingsRequestParser
    with MockAuditService
    with MockIdGenerator {

  val nino: String          = "AA123456A"
  val taxYear: String       = "2021-22"
  val correlationId: String = "X-123"

  val rawData: DeleteSavingsRawData = DeleteSavingsRawData(
    nino = nino,
    taxYear = taxYear
  )

  val requestData: DeleteSavingsRequest = DeleteSavingsRequest(
    nino = Nino(nino),
    taxYear = TaxYear.fromMtd(taxYear)
  )

  def event(auditResponse: AuditResponse): AuditEvent[GenericAuditDetail] =
    AuditEvent(
      auditType = "DeleteSavingsIncome",
      transactionName = "delete-savings-income",
      detail = GenericAuditDetail(
        "Individual",
        None,
        Map("nino" -> nino, "taxYear" -> taxYear),
        None,
        None,
        correlationId,
        auditResponse
      )
    )

  trait Test {
    val hc: HeaderCarrier = HeaderCarrier()

    val controller = new DeleteSavingsController(
      authService = mockEnrolmentsAuthService,
      lookupService = mockMtdIdLookupService,
      requestParser = mockDeleteSavingsRequestParser,
      service = mockDeleteSavingsService,
      auditService = mockAuditService,
      cc = cc,
      idGenerator = mockIdGenerator
    )

    MockedMtdIdLookupService.lookup(nino).returns(Future.successful(Right("test-mtd-id")))
    MockedEnrolmentsAuthService.authoriseUser()
    MockIdGenerator.generateCorrelationId.returns(correlationId)
  }

  "DeleteSavingsController" should {
    "return NO_content" when {
      "happy path" in new Test {

        MockDeleteSavingsRequestParser
          .parse(rawData)
          .returns(Right(requestData))

        MockDeleteSavingsService
          .deleteSavings(requestData)
          .returns(Future.successful(Right(ResponseWrapper(correlationId, ()))))

        val result: Future[Result] = controller.deleteSaving(nino, taxYear)(fakeDeleteRequest)

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

            MockDeleteSavingsRequestParser
              .parse(rawData)
              .returns(Left(ErrorWrapper(correlationId, error, None)))

            val result: Future[Result] = controller.deleteSaving(nino, taxYear)(fakeDeleteRequest)

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
          (RuleTaxYearNotSupportedError, BAD_REQUEST),
          (RuleTaxYearRangeInvalidError, BAD_REQUEST)
        )

        input.foreach(args => (errorsFromParserTester _).tupled(args))
      }

      "service errors occur" must {
        def serviceErrors(mtdError: MtdError, expectedStatus: Int): Unit = {
          s"a $mtdError error is returned from the service" in new Test {

            MockDeleteSavingsRequestParser
              .parse(rawData)
              .returns(Right(requestData))

            MockDeleteSavingsService
              .deleteSavings(requestData)
              .returns(Future.successful(Left(ErrorWrapper(correlationId, mtdError))))

            val result: Future[Result] = controller.deleteSaving(nino, taxYear)(fakeDeleteRequest)

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
          (NotFoundError, NOT_FOUND),
          (InternalError, INTERNAL_SERVER_ERROR),
          (RuleTaxYearRangeInvalidError, BAD_REQUEST),
          (RuleTaxYearNotSupportedError, BAD_REQUEST)
        )

        input.foreach(args => (serviceErrors _).tupled(args))
      }
    }
  }

}
