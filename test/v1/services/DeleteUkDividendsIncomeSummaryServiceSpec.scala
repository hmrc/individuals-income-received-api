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

package v1.services

import api.controllers.EndpointLogContext
import api.models.domain.{Nino, TaxYear}
import api.models.errors._
import api.models.outcomes.ResponseWrapper
import support.UnitSpec
import uk.gov.hmrc.http.HeaderCarrier
import v1.mocks.connectors.MockDeleteUkDividendsIncomeAnnualSummaryConnector
import v1.models.request.deleteUkDividendsIncomeAnnualSummary.DeleteUkDividendsIncomeAnnualSummaryRequest

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class DeleteUkDividendsIncomeSummaryServiceSpec extends UnitSpec {

  "DeleteUkDividendsIncomeSummaryServiceSpec" should {
    "delete" must {
      "return correct result for a success" in new Test {
        val outcome = Right(ResponseWrapper("resultId", ()))

        MockDeleteUkDividendsIncomeAnnualSummaryConnector
          .delete(requestData)
          .returns(Future.successful(Right(ResponseWrapper("resultId", ()))))

        await(service.delete(requestData)) shouldBe outcome
      }

      "map errors according to spec" when {
        def serviceError(downstreamErrorCode: String, error: MtdError): Unit =
          s"return ${error.code} error when $downstreamErrorCode error is returned from the connector" in new Test {

            MockDeleteUkDividendsIncomeAnnualSummaryConnector
              .delete(requestData)
              .returns(Future.successful(Left(ResponseWrapper("resultId", DownstreamErrors.single(DownstreamErrorCode(downstreamErrorCode))))))

            await(service.delete(requestData)) shouldBe Left(ErrorWrapper("resultId", error))
          }

        val errors = Seq(
          ("INVALID_NINO", NinoFormatError),
          ("INVALID_TYPE", StandardDownstreamError),
          ("INVALID_TAXYEAR", TaxYearFormatError),
          ("INVALID_PAYLOAD", StandardDownstreamError),
          ("NOT_FOUND_INCOME_SOURCE", NotFoundError),
          ("MISSING_CHARITIES_NAME_GIFT_AID", StandardDownstreamError),
          ("MISSING_GIFT_AID_AMOUNT", StandardDownstreamError),
          ("MISSING_CHARITIES_NAME_INVESTMENT", StandardDownstreamError),
          ("MISSING_INVESTMENT_AMOUNT", StandardDownstreamError),
          ("INVALID_ACCOUNTING_PERIOD", RuleTaxYearNotSupportedError),
          ("SERVICE_ERROR", StandardDownstreamError),
          ("SERVICE_UNAVAILABLE", StandardDownstreamError),
          ("GONE", NotFoundError),
          ("NOT_FOUND", NotFoundError)
        )

        val extraTysErrors = Seq(
          ("INVALID_INCOMESOURCE_TYPE", StandardDownstreamError),
          ("INVALID_TAX_YEAR", TaxYearFormatError),
          ("INVALID_CORRELATION_ID", StandardDownstreamError),
          ("INVALID_INCOMESOURCE_ID", StandardDownstreamError),
          ("INCOME_SOURCE_DATA_NOT_FOUND", NotFoundError),
          ("PERIOD_NOT_FOUND", NotFoundError),
          ("PERIOD_ALREADY_DELETED", NotFoundError)
        )

        (errors ++ extraTysErrors).foreach(args => (serviceError _).tupled(args))
      }
    }
  }

  trait Test extends MockDeleteUkDividendsIncomeAnnualSummaryConnector {
    implicit val hc: HeaderCarrier              = HeaderCarrier()
    implicit val logContext: EndpointLogContext = EndpointLogContext("c", "ep")

    val validNino: String              = "AA123456A"
    val validTaxYear: String           = "2019-20"
    implicit val correlationId: String = "X-123"

    val requestData: DeleteUkDividendsIncomeAnnualSummaryRequest =
      DeleteUkDividendsIncomeAnnualSummaryRequest(Nino(validNino), TaxYear.fromMtd(validTaxYear))

    val service = new DeleteUkDividendsIncomeAnnualSummaryService(
      connector = mockConnector
    )

  }

}
