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
import play.api.libs.json.Json
import v1.models.request.deleteUkDividendsIncomeAnnualSummary.DeleteUkDividendsIncomeAnnualSummaryRequest

import scala.concurrent.Future

class DeleteUkDividendsIncomeAnnualSummaryConnectorSpec extends ConnectorSpec {

  val nino: String              = "AA123456A"
  val taxYearMtd: String        = "2017-18"
  val taxYearDownstream: String = "2018"

  class Test extends MockHttpClient with MockAppConfig {

    val connector: DeleteUkDividendsIncomeAnnualSummaryConnector = new DeleteUkDividendsIncomeAnnualSummaryConnector(
      http = mockHttpClient,
      appConfig = mockAppConfig
    )

    MockedAppConfig.desBaseUrl returns baseUrl
    MockedAppConfig.desToken returns "des-token"
    MockedAppConfig.desEnvironment returns "des-environment"
    MockedAppConfig.desEnvironmentHeaders returns Some(allowedDesHeaders)
  }

  "delete" should {

    val request: DeleteUkDividendsIncomeAnnualSummaryRequest = DeleteUkDividendsIncomeAnnualSummaryRequest(Nino(nino), TaxYear.fromMtd(taxYearMtd))

    "return a result" when {
      "the downstream call is successful" in new Test {
        val outcome = Right(ResponseWrapper(correlationId, ()))

        MockedHttpClient
          .post(
            url = s"$baseUrl/income-tax/nino/$nino/income-source/dividends/annual/$taxYearDownstream",
            config = dummyDesHeaderCarrierConfig,
            requiredHeaders = requiredDesHeaders,
            excludedHeaders = Seq("AnotherHeader" -> "HeaderValue"),
            body = Json.parse("""{}""")
          )
          .returns(Future.successful(outcome))

        await(connector.delete(request)) shouldBe outcome
      }
    }
  }

}
