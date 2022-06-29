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
import api.mocks.MockHttpClient
import api.models.domain.{Nino, TaxYear}
import api.models.outcomes.ResponseWrapper
import mocks.MockAppConfig
import uk.gov.hmrc.http.HeaderCarrier
import v1.models.request.retrieveUkSavingsAnnualSummary.RetrieveUkSavingsAnnualSummaryRequest
import v1.models.response.retrieveUkSavingsAnnualSummary.RetrieveUkSavingsAnnualSummaryResponse

import scala.concurrent.Future

class RetrieveUkSavingsAccountAnnualSummaryConnectorSpec extends ConnectorSpec {

  val nino: String              = "AA111111A"
  val taxYearMtd: String        = "2018-19"
  val taxYearDownstream: String = "2019"
  val incomeSourceId: String    = "SAVKB2UVwUTBQGJ"

  val request: RetrieveUkSavingsAnnualSummaryRequest =
    RetrieveUkSavingsAnnualSummaryRequest(
      Nino(nino),
      TaxYear.fromMtd(taxYearMtd),
      incomeSourceId
    )

  private val validResponse = RetrieveUkSavingsAnnualSummaryResponse(
    taxedUkInterest = Some(1230.55),
    untaxedUkInterest = Some(1230.55)
  )

  class Test extends MockHttpClient with MockAppConfig {

    val connector: RetrieveUkSavingsAccountAnnualSummaryConnector = new RetrieveUkSavingsAccountAnnualSummaryConnector(
      http = mockHttpClient,
      appConfig = mockAppConfig
    )

    MockedAppConfig.desBaseUrl returns baseUrl
    MockedAppConfig.desToken returns "des-token"
    MockedAppConfig.desEnvironment returns "des-environment"
    MockedAppConfig.desEnvironmentHeaders returns Some(allowedDesHeaders)
  }

  "RetrieveUkSavingsAccountAnnualSummaryConnector" when {
    "retrieveUkSavingsAccountAnnualSummary" must {
      "return a 200 status for a success scenario" in new Test {
        val outcome: Right[Nothing, ResponseWrapper[RetrieveUkSavingsAnnualSummaryResponse]] = Right(ResponseWrapper(correlationId, validResponse))
        implicit val hc: HeaderCarrier = HeaderCarrier(otherHeaders = otherHeaders)

        MockedHttpClient
          .get(
            url = s"$baseUrl/income-tax/nino/$nino/income-source/savings/annual/$taxYearDownstream?incomeSourceId=$incomeSourceId",
            config = dummyDesHeaderCarrierConfig,
            requiredHeaders = requiredDesHeaders,
            excludedHeaders = Seq("AnotherHeader" -> "HeaderValue")
          )
          .returns(Future.successful(outcome))

        await(connector.retrieveUkSavingsAccountAnnualSummary(request)) shouldBe outcome
      }
    }
  }

}
