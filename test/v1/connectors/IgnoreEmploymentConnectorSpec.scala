/*
 * Copyright 2021 HM Revenue & Customs
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

import mocks.MockAppConfig
import uk.gov.hmrc.domain.Nino
import v1.mocks.MockHttpClient
import v1.models.outcomes.ResponseWrapper
import v1.models.request.ignoreEmployment.{IgnoreEmploymentRequest, IgnoreEmploymentRequestBody}

import scala.concurrent.Future

class IgnoreEmploymentConnectorSpec extends ConnectorSpec {

  val nino: String = "AA111111A"
  val taxYear: String = "2021-22"
  val employmentId: String = "4557ecb5-fd32-48cc-81f5-e6acd1099f3c"

  val ignoreEmploymentRequestBody: IgnoreEmploymentRequestBody = IgnoreEmploymentRequestBody(ignoreEmployment = true)

  val request: IgnoreEmploymentRequest = IgnoreEmploymentRequest(
    nino = Nino(nino),
    taxYear = taxYear,
    employmentId = employmentId,
    body = ignoreEmploymentRequestBody
  )

  class Test extends MockHttpClient with MockAppConfig {

    val connector: IgnoreEmploymentConnector = new IgnoreEmploymentConnector(
      http = mockHttpClient,
      appConfig = mockAppConfig
    )

    MockedAppConfig.desBaseUrl returns baseUrl
    MockedAppConfig.desToken returns "des-token"
    MockedAppConfig.desEnvironment returns "des-environment"
  }

  "IgnoreEmploymentConnector" when {
    "ignoreEmployment" should {
      "return a 204 status upon HttpClient success" in new Test {
        val outcome = Right(ResponseWrapper(correlationId, ()))

        MockedHttpClient
          .put(
            url = s"$baseUrl/income-tax/income/employments/$nino/$taxYear/$employmentId/ignore",
            body = ignoreEmploymentRequestBody,
            requiredHeaders = requiredHeaders :_*
          ).returns(Future.successful(outcome))

        await(connector.ignoreEmployment(request)) shouldBe outcome
      }
    }
  }
}