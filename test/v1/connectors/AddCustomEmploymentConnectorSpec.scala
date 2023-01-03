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
import v1.models.request.addCustomEmployment.{AddCustomEmploymentRequest, AddCustomEmploymentRequestBody}
import v1.models.response.addCustomEmployment.AddCustomEmploymentResponse

import scala.concurrent.Future

class AddCustomEmploymentConnectorSpec extends ConnectorSpec {

  val nino: String    = "AA111111A"
  val taxYear: String = "2021-22"

  val addCustomEmploymentRequestBody: AddCustomEmploymentRequestBody = AddCustomEmploymentRequestBody(
    employerRef = Some("123/AB56797"),
    employerName = "AMD infotech Ltd",
    startDate = "2019-01-01",
    cessationDate = Some("2020-06-01"),
    payrollId = Some("124214112412"),
    occupationalPension = false
  )

  val request: AddCustomEmploymentRequest = AddCustomEmploymentRequest(
    nino = Nino(nino),
    taxYear = taxYear,
    body = addCustomEmploymentRequestBody
  )

  val response: AddCustomEmploymentResponse = AddCustomEmploymentResponse("4557ecb5-fd32-48cc-81f5-e6acd1099f3c")

  class Test extends MockHttpClient with MockAppConfig {

    val connector: AddCustomEmploymentConnector = new AddCustomEmploymentConnector(
      http = mockHttpClient,
      appConfig = mockAppConfig
    )

    MockedAppConfig.api1661BaseUrl returns baseUrl
    MockedAppConfig.api1661Token returns "api1661-token"
    MockedAppConfig.api1661Environment returns "api1661-environment"
    MockedAppConfig.api1661EnvironmentHeaders returns Some(allowedIfsHeaders)
  }

  "AddCustomEmploymentConnector" when {
    ".addEmployment" should {
      "return a success upon HttpClient success" in new Test {
        val outcome                    = Right(ResponseWrapper(correlationId, response))
        implicit val hc: HeaderCarrier = HeaderCarrier(otherHeaders = otherHeaders ++ Seq("Content-Type" -> "application/json"))
        val requiredApi1661HeadersPost: Seq[(String, String)] = requiredApi1661Headers ++ Seq("Content-Type" -> "application/json")

        MockedHttpClient
          .post(
            url = s"$baseUrl/income-tax/income/employments/$nino/$taxYear/custom",
            config = dummyIfsHeaderCarrierConfig,
            body = addCustomEmploymentRequestBody,
            requiredHeaders = requiredApi1661HeadersPost,
            excludedHeaders = Seq("AnotherHeader" -> "HeaderValue")
          )
          .returns(Future.successful(outcome))

        await(connector.addEmployment(request)) shouldBe outcome
      }
    }
  }

}
