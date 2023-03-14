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
import api.connectors.DownstreamUri.DesUri
import api.mocks.MockHttpClient
import api.models.outcomes.ResponseWrapper
import mocks.MockAppConfig
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.Future

class DeleteCustomEmploymentConnectorSpec extends ConnectorSpec {

  val nino: String    = "AA111111A"
  val taxYear: String = "2019-20"

  class Test extends MockHttpClient with MockAppConfig {

    val connector: DeleteCustomEmploymentConnector = new DeleteCustomEmploymentConnector(
      http = mockHttpClient,
      appConfig = mockAppConfig
    )

    MockedAppConfig.desBaseUrl returns baseUrl
    MockedAppConfig.desToken returns "des-token"
    MockedAppConfig.desEnvironment returns "des-environment"
    MockedAppConfig.desEnvironmentHeaders returns Some(allowedDesHeaders)
  }

  "DeleteCustomEmploymentConnector" when {
    "delete" must {
      "return a 204 status for a success scenario" in new Test {

        val outcome                       = Right(ResponseWrapper(correlationId, ()))
        implicit val desUri: DesUri[Unit] = DesUri[Unit](s"income-tax/income/savings/$nino/$taxYear")
        implicit val hc: HeaderCarrier    = HeaderCarrier(otherHeaders = otherHeaders)

        MockedHttpClient
          .delete(
            url = s"$baseUrl/income-tax/income/savings/$nino/$taxYear",
            config = dummyDesHeaderCarrierConfig,
            requiredHeaders = requiredDesHeaders,
            excludedHeaders = Seq("AnotherHeader" -> "HeaderValue")
          )
          .returns(Future.successful(outcome))

        await(connector.delete()) shouldBe outcome
      }
    }
  }

}
