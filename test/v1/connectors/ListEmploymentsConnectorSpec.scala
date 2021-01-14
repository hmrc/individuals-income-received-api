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
import v1.models.request.listEmployments.ListEmploymentsRequest
import v1.models.response.listEmployment.{Employment, ListEmploymentResponse}

import scala.concurrent.Future

class ListEmploymentsConnectorSpec extends ConnectorSpec {

  val nino: String = "AA111111A"
  val taxYear: String = "2019"

  val request: ListEmploymentsRequest = ListEmploymentsRequest(Nino(nino), taxYear)

  private val validResponse = ListEmploymentResponse(
    employments = Some(Seq(Employment(employmentId = "00000000-0000-1000-8000-000000000000",
      employerName = "Vera Lynn",
      dateIgnored = Some("2020-06-17T10:53:38Z")),
      Employment(employmentId = "00000000-0000-1000-8000-000000000001",
        employerName = "Vera Lynn",
        dateIgnored = Some("2020-06-17T10:53:38Z")))),
    customEmployments =
      Some(Seq(Employment(employmentId = "00000000-0000-1000-8000-000000000002",
        employerName = "Vera Lynn"),
        Employment(employmentId = "00000000-0000-1000-8000-000000000003",
          employerName = "Vera Lynn")))
  )

  class Test extends MockHttpClient with MockAppConfig {

    val connector: ListEmploymentsConnector = new ListEmploymentsConnector(
      http = mockHttpClient,
      appConfig = mockAppConfig
    )

    MockedAppConfig.desBaseUrl returns baseUrl
    MockedAppConfig.desToken returns "des-token"
    MockedAppConfig.desEnvironment returns "des-environment"
  }

  "ListEmploymentsConnector" when {
    "listEmployments" must {
      "return a 200 status for a success scenario" in new Test {

        val outcome = Right(ResponseWrapper(correlationId, validResponse))

        MockedHttpClient
          .get(
            url = s"$baseUrl/income-tax/income/employments/$nino/$taxYear",
            requiredHeaders = requiredHeaders :_*
          )
          .returns(Future.successful(outcome))

        await(connector.listEmployments(request)) shouldBe outcome
      }
    }
  }
}