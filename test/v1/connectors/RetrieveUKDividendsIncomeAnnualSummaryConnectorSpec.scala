/*
 * Copyright 2022 HM Revenue & Customs
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

package v1.connectors

import api.connectors.ConnectorSpec
import api.models.domain.{Nino, TaxYear}
import api.models.outcomes.ResponseWrapper
import uk.gov.hmrc.http.HeaderCarrier
import v1.models.request.retrieveUkDividendsAnnualIncomeSummary.RetrieveUkDividendsAnnualIncomeSummaryRequest
import v1.models.response.retrieveUkDividendsAnnualIncomeSummary.RetrieveUkDividendsAnnualIncomeSummaryResponse

import scala.concurrent.Future

class RetrieveUKDividendsIncomeAnnualSummaryConnectorSpec extends ConnectorSpec {

  val nino: String              = "AA111111A"
  val taxYearMtd: String        = "2018-19"
  val taxYearDownstream: String = "2019"
  val tysTaxYear: String        = "2023"

  private val validResponse = RetrieveUkDividendsAnnualIncomeSummaryResponse(
    ukDividends = Some(10.12),
    otherUkDividends = Some(11.12)
  )

  "RetrieveUkDividendsIncomeAnnualSummaryConnectorSpec" when {
    "retrieveUKDividendsIncomeAnnualSummary is called" must {
      "return a 200 for success scenario" in {
        new DesTest with Test {
          def taxYear: TaxYear = TaxYear.fromMtd("2018-19")

          val outcome                             = Right(ResponseWrapper(correlationId, validResponse))
          override implicit val hc: HeaderCarrier = HeaderCarrier(otherHeaders = otherHeaders)

          MockedHttpClient
            .get(
              url = s"$baseUrl/income-tax/nino/$nino/income-source/dividends/annual/$taxYearDownstream",
              config = dummyDesHeaderCarrierConfig,
              requiredHeaders = requiredDesHeaders,
              excludedHeaders = Seq("AnotherHeader" -> "HeaderValue")
            )
            .returns(Future.successful(outcome))

          await(connector.retrieveUKDividendsIncomeAnnualSummary(request)) shouldBe outcome

        }
      }
    }

    "retrieveUkDividendsIncomeAnnualSummary is called for a TaxYearSpecific tax year" must {
      "return a 200 for success scenario" in {
        new TysIfsTest with Test {
          def taxYear: TaxYear = TaxYear.fromMtd("2023-24")

          val outcome = Right(ResponseWrapper(correlationId, validResponse))

          willGet()

          MockedHttpClient
            .get(
              url = s"$baseUrl/income-tax/${taxYear.asTysDownstream}/$nino/income-source/dividends/annual",
              config = dummyIfsHeaderCarrierConfig,
              requiredHeaders = requiredDownstreamHeaders,
              excludedHeaders = Seq("OneMoreHeader" -> "HeaderValue")
            )
            .returns(Future.successful(outcome))

          await(connector.retrieveUKDividendsIncomeAnnualSummary(tysRequest)) shouldBe outcome
        }
      }
    }
  }

  trait Test { _: ConnectorTest =>
    def taxYear: TaxYear

    protected val connector: RetrieveUKDividendsIncomeAnnualSummaryConnector =
      new RetrieveUKDividendsIncomeAnnualSummaryConnector(http = mockHttpClient, appConfig = mockAppConfig)

    protected val request: RetrieveUkDividendsAnnualIncomeSummaryRequest =
      RetrieveUkDividendsAnnualIncomeSummaryRequest(Nino("AA111111A"), TaxYear.fromMtd(taxYearMtd))

    protected val tysRequest: RetrieveUkDividendsAnnualIncomeSummaryRequest =
      RetrieveUkDividendsAnnualIncomeSummaryRequest(Nino("AA111111A"), taxYear = taxYear)

  }

}
