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
import api.models.domain.{Nino, TaxYear}
import api.models.outcomes.ResponseWrapper
import mocks.MockAppConfig
import play.api.Configuration
import uk.gov.hmrc.http.HeaderCarrier
import v1.models.request.createAmendUkDividendsIncomeAnnualSummary.{
  CreateAmendUkDividendsIncomeAnnualSummaryBody,
  CreateAmendUkDividendsIncomeAnnualSummaryRequest
}

import scala.concurrent.Future

class CreateAmendUkDividendsAnnualSummaryConnectorSpec extends ConnectorSpec {

  private val nino = "AA111111A"
  private val body = CreateAmendUkDividendsIncomeAnnualSummaryBody(None, None)

  "CreateAmendUkDividendsAnnualSummaryConnector" when {
    "createOrAmendAnnualSummary called" must {
      "return a 200 status for a success scenario" in new DesTest {
        def taxYear           = "2019-20"
        val downstreamTaxYear = "2020"

        val outcome = Right(ResponseWrapper(correlationId, ()))

        implicit val hc: HeaderCarrier =
          HeaderCarrier(otherHeaders = otherHeaders ++ Seq("Content-Type" -> "application/json"))

        val requiredDownstreamHeaders: Seq[(String, String)] =
          requiredDesHeaders ++ Seq("Content-Type" -> "application/json")

        MockedHttpClient
          .post(
            url = s"$baseUrl/income-tax/nino/$nino/income-source/dividends/annual/$downstreamTaxYear",
            config = dummyDesHeaderCarrierConfig,
            body = body,
            requiredHeaders = requiredDownstreamHeaders,
            excludedHeaders = Seq("AnotherHeader" -> "HeaderValue")
          )
          .returns(Future.successful(outcome))

        await(connector.createOrAmendAnnualSummary(request)) shouldBe outcome
      }
    }

    "createOrAmendAnnualSummary called for a Tax Year Specific tax year" must {
      "return a 200 status for a success scenario" in new TysIfsTest {
        def taxYear           = "2023-24"
        val downstreamTaxYear = "23-24"

        val outcome = Right(ResponseWrapper(correlationId, ()))

        implicit val hc: HeaderCarrier =
          HeaderCarrier(otherHeaders = otherHeaders ++ Seq("Content-Type" -> "application/json"))

        val requiredDownstreamHeaders: Seq[(String, String)] =
          requiredTysIfsHeaders ++ Seq("Content-Type" -> "application/json")

        MockedHttpClient
          .post(
            url = s"$baseUrl/income-tax/$downstreamTaxYear/$nino/income-source/dividends/annual",
            config = dummyDesHeaderCarrierConfig,
            body = body,
            requiredHeaders = requiredDownstreamHeaders,
            excludedHeaders = Seq("AnotherHeader" -> "HeaderValue")
          )
          .returns(Future.successful(outcome))

        await(connector.createOrAmendAnnualSummary(request)) shouldBe outcome
      }
    }
  }

  private trait DesTest extends MockHttpClient with MockAppConfig {
    def taxYear: String

    val request = CreateAmendUkDividendsIncomeAnnualSummaryRequest(
      nino = Nino(nino),
      taxYear = TaxYear.fromMtd(taxYear),
      body = body
    )

    MockedAppConfig.desBaseUrl returns baseUrl
    MockedAppConfig.desToken returns "des-token"
    MockedAppConfig.desEnvironment returns "des-environment"
    MockedAppConfig.desEnvironmentHeaders returns Some(allowedDesHeaders)

    MockedAppConfig.featureSwitches returns Configuration("tys-api.enabled" -> false)

    val connector: CreateAmendUkDividendsAnnualSummaryConnector = new CreateAmendUkDividendsAnnualSummaryConnector(
      http = mockHttpClient,
      appConfig = mockAppConfig
    )

  }

  private trait TysIfsTest extends MockHttpClient with MockAppConfig {
    def taxYear: String

    val request = CreateAmendUkDividendsIncomeAnnualSummaryRequest(
      nino = Nino(nino),
      taxYear = TaxYear.fromMtd(taxYear),
      body = body
    )

    MockedAppConfig.tysIfsBaseUrl returns baseUrl
    MockedAppConfig.tysIfsToken returns "TYS-IFS-token"
    MockedAppConfig.tysIfsEnvironment returns "TYS-IFS-environment"
    MockedAppConfig.tysIfsEnvironmentHeaders returns Some(allowedIfsHeaders)

    MockedAppConfig.featureSwitches returns Configuration("tys-api.enabled" -> true)

    val connector: CreateAmendUkDividendsAnnualSummaryConnector = new CreateAmendUkDividendsAnnualSummaryConnector(
      http = mockHttpClient,
      appConfig = mockAppConfig
    )

  }

}
