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

package v1.connectors

import api.connectors.ConnectorSpec
import api.mocks.MockHttpClient
import api.models.domain.Nino
import api.models.outcomes.ResponseWrapper
import mocks.MockAppConfig
import uk.gov.hmrc.http.HeaderCarrier
import v1.models.request.listUkSavingsAccounts.ListUkSavingsAccountsRequest
import v1.models.response.listUkSavingsAccounts.{ListUkSavingsAccountsResponse, UkSavingsAccount}

import scala.concurrent.Future

class ListUkSavingsAccountsConnectorSpec extends ConnectorSpec {

  val nino: String     = "AA111111A"
  val taxYear: String  = "2019"
  val savingsAccountId = "SAVKB2UVwUTBQGJ"

  val request: ListUkSavingsAccountsRequest                     = ListUkSavingsAccountsRequest(Nino(nino), None)
  val requestWithSavingsAccountId: ListUkSavingsAccountsRequest = ListUkSavingsAccountsRequest(Nino(nino), Some(savingsAccountId))

  private val validResponse = ListUkSavingsAccountsResponse(
    savingsAccounts = Some(
      Seq(
        UkSavingsAccount(savingsAccountId = "000000000000001", accountName = "Bank Account 1"),
        UkSavingsAccount(savingsAccountId = "000000000000002", accountName = "Bank Account 2"),
        UkSavingsAccount(savingsAccountId = "000000000000003", accountName = "Bank Account 3")
      )
    )
  )

  class Test extends MockHttpClient with MockAppConfig {

    val connector: ListUkSavingsAccountsConnector = new ListUkSavingsAccountsConnector(
      http = mockHttpClient,
      appConfig = mockAppConfig
    )

    MockedAppConfig.desBaseUrl returns baseUrl
    MockedAppConfig.desToken returns "des-token"
    MockedAppConfig.desEnvironment returns "des-environment"
    MockedAppConfig.desEnvironmentHeaders returns Some(allowedDesHeaders)
  }

  "ListUkSavingsAccountsConnector" when {
    "listUkSavingsAccounts" must {
      "return a valid response, list of uk savings accounts " +
        "upon receiving SUCCESS response from the backend service" in new Test {
          val outcome                    = Right(ResponseWrapper(correlationId, validResponse))
          implicit val hc: HeaderCarrier = HeaderCarrier(otherHeaders = otherHeaders)

          MockedHttpClient
            .get(
              url = s"$baseUrl/income-tax/income-sources/nino/$nino",
              parameters = Seq("incomeSourceType" -> "interest-from-uk-banks"),
              config = dummyIfsHeaderCarrierConfig,
              requiredHeaders = requiredDesHeaders,
              excludedHeaders = Seq("AnotherHeader" -> "HeaderValue")
            )
            .returns(Future.successful(outcome))

          await(connector.listUkSavingsAccounts(request)) shouldBe outcome
        }

      "return a valid response, list of a single uk savings account " +
        "when incomeSourceId was sent as part of the request and " +
        "upon receiving SUCCESS response from the backend service " in new Test {
          val outcome                    = Right(ResponseWrapper(correlationId, validResponse))
          implicit val hc: HeaderCarrier = HeaderCarrier(otherHeaders = otherHeaders)

          MockedHttpClient
            .get(
              url = s"$baseUrl/income-tax/income-sources/nino/$nino",
              parameters = Seq("incomeSourceType" -> "interest-from-uk-banks", "incomeSourceId" -> savingsAccountId),
              config = dummyIfsHeaderCarrierConfig,
              requiredHeaders = requiredDesHeaders,
              excludedHeaders = Seq("AnotherHeader" -> "HeaderValue")
            )
            .returns(Future.successful(outcome))

          await(connector.listUkSavingsAccounts(requestWithSavingsAccountId)) shouldBe outcome
        }
    }
  }

}
