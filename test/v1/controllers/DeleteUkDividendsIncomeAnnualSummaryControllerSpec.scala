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
import api.models.audit.{AuditEvent, AuditResponse, FlattenedGenericAuditDetail}
import api.models.auth.UserDetails
import api.models.domain.{Nino, TaxYear}
import api.models.errors._
import api.models.outcomes.ResponseWrapper
import play.api.libs.json.JsValue
import play.api.mvc.Result
import v1.mocks.requestParsers.MockDeleteUkDividendsIncomeAnnualSummaryRequestParser
import v1.mocks.services.MockDeleteUkDividendsIncomeAnnualSummaryService
import v1.models.request.deleteUkDividendsIncomeAnnualSummary.{
  DeleteUkDividendsIncomeAnnualSummaryRawData,
  DeleteUkDividendsIncomeAnnualSummaryRequest
}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class DeleteUkDividendsIncomeAnnualSummaryControllerSpec
    extends ControllerBaseSpec
    with ControllerTestRunner
    with MockDeleteUkDividendsIncomeAnnualSummaryService
    with MockDeleteUkDividendsIncomeAnnualSummaryRequestParser {

  private val taxYear = "2017-18"
  private val mtdId   = "test-mtd-id"

  private val rawData: DeleteUkDividendsIncomeAnnualSummaryRawData = DeleteUkDividendsIncomeAnnualSummaryRawData(
    nino = nino,
    taxYear = taxYear
  )

  private val requestData: DeleteUkDividendsIncomeAnnualSummaryRequest = DeleteUkDividendsIncomeAnnualSummaryRequest(
    nino = Nino(nino),
    taxYear = TaxYear.fromMtd(taxYear)
  )

  "DeleteDividendsController" should {
    "return a successful response with status 204 (No Content)" when {
      "a valid request is supplied" in new Test {
        MockDeleteUkDividendsIncomeAnnualSummaryRequestParser
          .parse(rawData)
          .returns(Right(requestData))

        MockDeleteUkDividendsIncomeAnnualSummaryService
          .delete(requestData)
          .returns(Future.successful(Right(ResponseWrapper(correlationId, ()))))

        runOkTest(expectedStatus = NO_CONTENT)
      }
    }

    "return the error as per spec" when {
      "the parser validation fails" in new Test {
        MockDeleteUkDividendsIncomeAnnualSummaryRequestParser
          .parse(rawData)
          .returns(Left(ErrorWrapper(correlationId, NinoFormatError)))

        runErrorTest(NinoFormatError)
      }

      "service returns an error" in new Test {
        MockDeleteUkDividendsIncomeAnnualSummaryRequestParser
          .parse(rawData)
          .returns(Right(requestData))

        MockDeleteUkDividendsIncomeAnnualSummaryService
          .delete(requestData)
          .returns(Future.successful(Left(ErrorWrapper(correlationId, RuleTaxYearNotSupportedError))))

        runErrorTest(RuleTaxYearNotSupportedError)
      }
    }
  }

  trait Test extends ControllerTest with AuditEventChecking[FlattenedGenericAuditDetail] {

    val controller = new DeleteUkDividendsIncomeAnnualSummaryController(
      authService = mockEnrolmentsAuthService,
      lookupService = mockMtdIdLookupService,
      parser = mockDeleteUkDividendsIncomeAnnualSummaryRequestParser,
      service = mockDeleteUkDividendsIncomeAnnualSummaryService,
      auditService = mockAuditService,
      cc = cc,
      idGenerator = mockIdGenerator
    )

    protected def callController(): Future[Result] = controller.deleteUkDividends(nino, taxYear)(fakeDeleteRequest)

    def event(auditResponse: AuditResponse, requestBody: Option[JsValue]): AuditEvent[FlattenedGenericAuditDetail] =
      AuditEvent(
        auditType = "DeleteUkDividendsIncome",
        transactionName = "delete-uk-dividends-income",
        detail = FlattenedGenericAuditDetail(
          versionNumber = Some("1.0"),
          userDetails = UserDetails(mtdId, "Individual", None),
          params = Map("nino" -> nino, "taxYear" -> taxYear),
          request = None,
          `X-CorrelationId` = correlationId,
          auditResponse = auditResponse
        )
      )

  }

}
