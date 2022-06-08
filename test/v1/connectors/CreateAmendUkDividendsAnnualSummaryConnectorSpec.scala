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
import api.models.domain.{DesTaxYear, Nino}
import api.models.outcomes.ResponseWrapper
import mocks.MockAppConfig
import uk.gov.hmrc.http.HeaderCarrier
import v1.models.request.createAmendUkDividendsIncomeAnnualSummary.{
  CreateAmendUkDividendsIncomeAnnualSummaryBody,
  CreateAmendUkDividendsIncomeAnnualSummaryRequest
}

import scala.concurrent.Future

class CreateAmendUkDividendsAnnualSummaryConnectorSpec extends ConnectorSpec {

  private val nino              = "AA111111A"
  private val taxYear           = "2019-20"
  private val downstreamTaxYear = "2020"

  private val body = CreateAmendUkDividendsIncomeAnnualSummaryBody(None, None)

  private val request = CreateAmendUkDividendsIncomeAnnualSummaryRequest(
    nino = Nino(nino),
    taxYear = DesTaxYear.fromMtd(taxYear),
    body = body
  )

  class Test extends MockHttpClient with MockAppConfig {

    val connector: CreateAmendUkDividendsAnnualSummaryConnector = new CreateAmendUkDividendsAnnualSummaryConnector(
      http = mockHttpClient,
      appConfig = mockAppConfig
    )

    MockedAppConfig.desBaseUrl returns baseUrl
    MockedAppConfig.desToken returns "des-token"
    MockedAppConfig.desEnvironment returns "des-environment"
    MockedAppConfig.desEnvironmentHeaders returns Some(allowedDesHeaders)
  }

  "CreateAmendUkDividendsAnnualSummaryConnector" when {
    "createOrAmendAnnualSummary called" must {
      "return a 200 status for a success scenario" in new Test {
        val outcome                                      = Right(ResponseWrapper(correlationId, ()))
        implicit val hc: HeaderCarrier                   = HeaderCarrier(otherHeaders = otherHeaders ++ Seq("Content-Type" -> "application/json"))
        val requiredDesHeadersPut: Seq[(String, String)] = requiredDesHeaders ++ Seq("Content-Type" -> "application/json")

        MockedHttpClient
          .post(
            url = s"$baseUrl/income-tax/nino/$nino/income-source/dividends/annual/$downstreamTaxYear",
            config = dummyDesHeaderCarrierConfig,
            body = body,
            requiredHeaders = requiredDesHeadersPut,
            excludedHeaders = Seq("AnotherHeader" -> "HeaderValue")
          )
          .returns(Future.successful(outcome))

        await(connector.createOrAmendAnnualSummary(request)) shouldBe outcome
      }
    }
  }

}
