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
import v1.models.request.addUkSavingsAccount.{AddUkSavingsAccountRequest, AddUkSavingsAccountRequestBody}
import v1.models.response.addUkSavingsAccount.AddUkSavingsAccountResponse

import scala.concurrent.Future

class AddUkSavingsAccountConnectorSpec extends ConnectorSpec {

  val nino: String = "AA111111A"

  val addUkSavingsAccountRequestBody: AddUkSavingsAccountRequestBody = AddUkSavingsAccountRequestBody(accountName = "Shares savings account")

  val addUkSavingsAccountRequest: AddUkSavingsAccountRequest = AddUkSavingsAccountRequest(
    nino = Nino(nino),
    body = addUkSavingsAccountRequestBody
  )

  val addUkSavingsAccountResponse: AddUkSavingsAccountResponse = AddUkSavingsAccountResponse(
    savingsAccountId = "SAVKB2UVwUTBQGJ"
  )

  class Test extends MockHttpClient with MockAppConfig {

    val connector: AddUkSavingsAccountConnector = new AddUkSavingsAccountConnector(
      http = mockHttpClient,
      appConfig = mockAppConfig
    )

    MockedAppConfig.desBaseUrl returns baseUrl
    MockedAppConfig.desToken returns "des-token"
    MockedAppConfig.desEnvironment returns "des-environment"
    MockedAppConfig.desEnvironmentHeaders returns Some(allowedDesHeaders)
  }

  "addSavings" should {
    "return a 200 status for a success scenario" when {
      "valid request is supplied" in new Test {
        val outcome                                       = Right(ResponseWrapper(correlationId, addUkSavingsAccountResponse))
        implicit val hc: HeaderCarrier                    = HeaderCarrier(otherHeaders = otherHeaders ++ Seq("Content-Type" -> "application/json"))
        val requiredDesHeadersPost: Seq[(String, String)] = requiredDesHeaders ++ Seq("Content-Type" -> "application/json")

        MockedHttpClient
          .post(
            url = s"$baseUrl/income-tax/income-sources/nino/$nino",
            config = dummyDesHeaderCarrierConfig,
            body = addUkSavingsAccountRequestBody,
            requiredHeaders = requiredDesHeadersPost,
            excludedHeaders = Seq("AnotherHeader" -> "HeaderValue")
          )
          .returns(Future.successful(outcome))

        await(connector.addSavings(addUkSavingsAccountRequest)) shouldBe outcome
      }
    }
  }

}
