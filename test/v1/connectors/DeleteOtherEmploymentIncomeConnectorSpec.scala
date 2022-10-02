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
import api.connectors.DownstreamUri.DesUri
import api.mocks.MockHttpClient
import api.models.domain.{Nino, TaxYear}
import api.models.outcomes.ResponseWrapper
import mocks.MockAppConfig
import play.api.Configuration
import play.api.libs.json.Json
import v1.models.request.deleteOtherEmploymentIncome.DeleteOtherEmploymentIncomeRequest

import scala.concurrent.Future

class DeleteOtherEmploymentIncomeConnectorSpec extends ConnectorSpec {

  val nino: String              = "AA123456A"
  val taxYearMtd: String        = "2017-18"
  val taxYearDownstream: String = "2018"

  class Test extends MockHttpClient with MockAppConfig {

    val connector: DeleteOtherEmploymentIncomeConnector = new DeleteOtherEmploymentIncomeConnector(
      http = mockHttpClient,
      appConfig = mockAppConfig
    )

    val desUri: DesUri[Unit] = DesUri[Unit](
      s"income-tax/income/other/employments/$nino/$taxYearDownstream"
    )

    MockedAppConfig.desBaseUrl returns baseUrl
    MockedAppConfig.desToken returns "des-token"
    MockedAppConfig.desEnvironment returns "des-environment"
    MockedAppConfig.desEnvironmentHeaders returns Some(allowedDesHeaders)

    MockedAppConfig.tysIfsBaseUrl returns baseUrl
    MockedAppConfig.tysIfsEnvironment returns "TYS-IFS-environment"
    MockedAppConfig.tysIfsToken returns "TYS-IFS-token"
    MockedAppConfig.tysIfsEnvironmentHeaders returns Some(allowedIfsHeaders)
  }

  "delete" should {

    "return a result" when {
      "the downstream call is successful when not tax year specific" in new Test {
        val request: DeleteOtherEmploymentIncomeRequest = DeleteOtherEmploymentIncomeRequest(Nino(nino), TaxYear.fromMtd(taxYearMtd))
        val outcome                                     = Right(ResponseWrapper(correlationId, ()))

        MockedAppConfig.featureSwitches returns Configuration("tys-api.enabled" -> false)

        MockedHttpClient
          .post(
            url = s"$baseUrl/income-tax/income/other/employments/${request.nino}/${request.taxYear.asDownstream}",
            config = dummyDesHeaderCarrierConfig,
            requiredHeaders = requiredDesHeaders,
            excludedHeaders = Seq("AnotherHeader" -> "HeaderValue"),
            body = Json.parse("""{}""")
          )
          .returns(Future.successful(outcome))

        await(connector.deleteOtherEmploymentIncome(request, desUri)) shouldBe outcome
      }

      "the downstream call is successful when tax year specific" in new Test {
        val tysRequest: DeleteOtherEmploymentIncomeRequest = DeleteOtherEmploymentIncomeRequest(Nino(nino), TaxYear.fromMtd("2023-24"))
        val outcome                                        = Right(ResponseWrapper(correlationId, ()))

        MockedAppConfig.featureSwitches returns Configuration("tys-api.enabled" -> true)

        MockedHttpClient
          .post(
            url = s"$baseUrl/income-tax/income/other/employments/${tysRequest.taxYear.asTysDownstream}/${tysRequest.nino}",
            config = dummyIfsHeaderCarrierConfig,
            requiredHeaders = requiredTysIfsHeaders,
            excludedHeaders = Seq("AnotherHeader" -> "HeaderValue"),
            body = Json.parse("""{}""")
          )
          .returns(Future.successful(outcome))

        await(connector.deleteOtherEmploymentIncome(tysRequest, desUri)) shouldBe outcome
      }
    }
  }

}
