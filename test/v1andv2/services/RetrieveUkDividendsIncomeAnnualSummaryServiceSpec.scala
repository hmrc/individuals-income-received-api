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

package v1andv2.services

import api.controllers.EndpointLogContext
import api.models.domain.{Nino, TaxYear}
import api.models.errors._
import api.models.outcomes.ResponseWrapper
import api.services.ServiceSpec
import v1andv2.mocks.connectors.MockRetrieveUKDividendsIncomeAnnualSummaryConnector
import v1andv2.models.request.retrieveUkDividendsAnnualIncomeSummary.RetrieveUkDividendsAnnualIncomeSummaryRequest
import v1andv2.models.response.retrieveUkDividendsAnnualIncomeSummary.RetrieveUkDividendsAnnualIncomeSummaryResponse

import scala.concurrent.Future

class RetrieveUkDividendsIncomeAnnualSummaryServiceSpec extends ServiceSpec {

  private val nino    = "AA112233A"
  private val taxYear = "2019-20"

  private val requestData = RetrieveUkDividendsAnnualIncomeSummaryRequest(Nino(nino), TaxYear.fromMtd(taxYear))

  private val validResponse = RetrieveUkDividendsAnnualIncomeSummaryResponse(
    ukDividends = Some(10.12),
    otherUkDividends = Some(11.12)
  )

  trait Test extends MockRetrieveUKDividendsIncomeAnnualSummaryConnector {
    implicit val logContext: EndpointLogContext = EndpointLogContext("c", "ep")

    val service: RetrieveUkDividendsIncomeAnnualSummaryService =
      new RetrieveUkDividendsIncomeAnnualSummaryService(connector = mockRetrieveUKDividendsIncomeAnnualSummaryConnector)

  }

  "RetrieveUKDividendsIncomeAnnualSummaryService" when {
    "retrieveUKDividendsIncomeAnnualSummary" must {
      "return correct result for a success" in new Test {
        val outcome = Right(ResponseWrapper(correlationId, validResponse))

        MockRetrieveUKDividendsIncomeAnnualSummaryConnector
          .retrieveUKDividendsIncomeAnnualSummary(requestData)
          .returns(Future.successful(outcome))

        await(service.retrieveUKDividendsIncomeAnnualSummary(requestData)) shouldBe outcome
      }

      "map errors according to spec" when {

        def serviceError(downstreamErrorCode: String, error: MtdError): Unit =
          s"a $downstreamErrorCode error is returned from the service" in new Test {

            MockRetrieveUKDividendsIncomeAnnualSummaryConnector
              .retrieveUKDividendsIncomeAnnualSummary(requestData)
              .returns(Future.successful(Left(ResponseWrapper(correlationId, DownstreamErrors.single(DownstreamErrorCode(downstreamErrorCode))))))

            await(service.retrieveUKDividendsIncomeAnnualSummary(requestData)) shouldBe Left(ErrorWrapper(correlationId, error))
          }

        val errors = List(
          ("INVALID_NINO", NinoFormatError),
          ("INVALID_TYPE", InternalError),
          ("INVALID_TAXYEAR", TaxYearFormatError),
          ("INVALID_INCOME_SOURCE", InternalError),
          ("NOT_FOUND_PERIOD", NotFoundError),
          ("NOT_FOUND_INCOME_SOURCE", NotFoundError),
          ("SERVER_ERROR", InternalError),
          ("SERVICE_UNAVAILABLE", InternalError)
        )

        val extraTysErrors = List(
          ("INVALID_TAX_YEAR", TaxYearFormatError),
          ("INVALID_INCOMESOURCE_ID", InternalError),
          ("INVALID_INCOMESOURCE_TYPE", InternalError),
          ("INVALID_CORRELATION_ID", InternalError),
          ("SUBMISSION_PERIOD_NOT_FOUND", NotFoundError),
          ("INCOME_DATA_SOURCE_NOT_FOUND", NotFoundError),
          ("TAX_YEAR_NOT_SUPPORTED", RuleTaxYearNotSupportedError)
        )

        (errors ++ extraTysErrors).foreach(args => (serviceError _).tupled(args))
      }
    }
  }

}
