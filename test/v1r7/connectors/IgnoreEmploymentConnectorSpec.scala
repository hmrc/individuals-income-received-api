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

package v1r7.connectors

import mocks.MockAppConfig
import v1r7.models.domain.Nino
import uk.gov.hmrc.http.HeaderCarrier
import v1r7.mocks.MockHttpClient
import v1r7.models.outcomes.ResponseWrapper
import v1r7.models.request.EmptyBody
import v1r7.models.request.ignoreEmployment.IgnoreEmploymentRequest

import scala.concurrent.Future

class IgnoreEmploymentConnectorSpec extends ConnectorSpec {

  val nino: String = "AA111111A"
  val taxYear: String = "2021-22"
  val employmentId: String = "4557ecb5-fd32-48cc-81f5-e6acd1099f3c"

  val request: IgnoreEmploymentRequest = IgnoreEmploymentRequest(
    nino = Nino(nino),
    taxYear = taxYear,
    employmentId = employmentId
  )

  class Test extends MockHttpClient with MockAppConfig {

    val connector: IgnoreEmploymentConnector = new IgnoreEmploymentConnector(
      http = mockHttpClient,
      appConfig = mockAppConfig
    )

    MockedAppConfig.release6BaseUrl returns baseUrl
    MockedAppConfig.release6Token returns "release6-token"
    MockedAppConfig.release6Environment returns "release6-environment"
    MockedAppConfig.release6EnvironmentHeaders returns Some(allowedIfsHeaders)
  }

  "IgnoreEmploymentConnector" when {
    "ignoreEmployment" should {
      "return a 204 status upon HttpClient success" in new Test {
        val outcome = Right(ResponseWrapper(correlationId, ()))
        implicit val hc: HeaderCarrier = HeaderCarrier(otherHeaders = otherHeaders ++ Seq("Content-Type" -> "application/json"))
        val requiredRelease6HeadersPut: Seq[(String, String)] = requiredRelease6Headers ++ Seq("Content-Type" -> "application/json")

        MockedHttpClient
          .put(
            url = s"$baseUrl/income-tax/income/employments/$nino/$taxYear/$employmentId/ignore",
            config = dummyIfsHeaderCarrierConfig,
            body = EmptyBody,
            requiredHeaders = requiredRelease6HeadersPut,
            excludedHeaders = Seq("AnotherHeader" -> "HeaderValue")
          ).returns(Future.successful(outcome))

        await(connector.ignoreEmployment(request)) shouldBe outcome
      }
    }
  }
}