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
import v1.models.request.amendCustomEmployment.{AmendCustomEmploymentRequest, AmendCustomEmploymentRequestBody}

import scala.concurrent.Future

class AmendCustomEmploymentConnectorSpec extends ConnectorSpec {

  val nino: String = "AA111111A"
  val taxYear: String = "2021-22"
  val employmentId: String = "4557ecb5-fd32-48cc-81f5-e6acd1099f3c"

  val amendCustomEmploymentRequestBody: AmendCustomEmploymentRequestBody = AmendCustomEmploymentRequestBody(
    employerRef = Some("123/AB56797"),
    employerName = "AMD infotech Ltd",
    startDate = "2019-01-01",
    cessationDate = Some("2020-06-01"),
    payrollId = Some("124214112412")
  )

  val request: AmendCustomEmploymentRequest = AmendCustomEmploymentRequest(
    nino = Nino(nino),
    taxYear = taxYear,
    employmentId = employmentId,
    body = amendCustomEmploymentRequestBody
  )

  class Test extends MockHttpClient with MockAppConfig {

    val connector: AmendCustomEmploymentConnector = new AmendCustomEmploymentConnector(
      http = mockHttpClient,
      appConfig = mockAppConfig
    )

    MockedAppConfig.desBaseUrl returns baseUrl
    MockedAppConfig.desToken returns "des-token"
    MockedAppConfig.desEnvironment returns "des-environment"
  }

  "AmendCustomEmploymentConnector" when {
    ".amendEmployment" should {
      "return a success upon HttpClient success" in new Test {
        val outcome = Right(ResponseWrapper(correlationId, ()))

        MockedHttpClient
          .put(
            url = s"$baseUrl/income-tax/income/employments/$nino/$taxYear/custom/$employmentId",
            body = amendCustomEmploymentRequestBody,
            requiredHeaders = requiredHeaders :_*
          ).returns(Future.successful(outcome))

        await(connector.amendEmployment(request)) shouldBe outcome
      }
    }
  }
}