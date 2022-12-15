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
import uk.gov.hmrc.http.HeaderCarrier
import v1.models.request.amendPensions.{AmendForeignPensionsItem, AmendOverseasPensionContributions, AmendPensionsRequest, AmendPensionsRequestBody}

import scala.concurrent.Future

class AmendPensionsConnectorSpec extends ConnectorSpec {

  private val nino: String    = "AA111111A"
  private val taxYear: String = "2019-20"

  private val foreignPensionsModel = Seq(
    AmendForeignPensionsItem(
      countryCode = "DEU",
      amountBeforeTax = Some(100.23),
      taxTakenOff = Some(1.23),
      specialWithholdingTax = Some(2.23),
      foreignTaxCreditRelief = false,
      taxableAmount = 3.23
    ),
    AmendForeignPensionsItem(
      countryCode = "FRA",
      amountBeforeTax = Some(200.23),
      taxTakenOff = Some(3.21),
      specialWithholdingTax = Some(4.32),
      foreignTaxCreditRelief = true,
      taxableAmount = 5.55
    )
  )

  private val overseasPensionContributionsModel = Seq(
    AmendOverseasPensionContributions(
      customerReference = Some("PENSIONINCOME555"),
      exemptEmployersPensionContribs = 300.33,
      migrantMemReliefQopsRefNo = Some("QOPS000001"),
      dblTaxationRelief = Some(1.23),
      dblTaxationCountryCode = Some("ENG"),
      dblTaxationArticle = Some("AB1123-1"),
      dblTaxationTreaty = Some("Treaty"),
      sf74reference = Some("SF74-654321")
    ),
    AmendOverseasPensionContributions(
      customerReference = Some("PENSIONINCOME245"),
      exemptEmployersPensionContribs = 200.23,
      migrantMemReliefQopsRefNo = Some("QOPS000000"),
      dblTaxationRelief = Some(4.23),
      dblTaxationCountryCode = Some("FRA"),
      dblTaxationArticle = Some("AB3211-1"),
      dblTaxationTreaty = Some("Treaty"),
      sf74reference = Some("SF74-123456")
    )
  )

  private val amendPensionsRequestBody: AmendPensionsRequestBody = AmendPensionsRequestBody(
    foreignPensions = Some(foreignPensionsModel),
    overseasPensionContributions = Some(overseasPensionContributionsModel)
  )

  private def amendPensionsRequest(taxYear: String): AmendPensionsRequest = AmendPensionsRequest(
    nino = Nino(nino),
    taxYear = TaxYear.fromMtd(taxYear),
    body = amendPensionsRequestBody
  )

  trait Test extends MockHttpClient with MockAppConfig {

    val connector: AmendPensionsConnector = new AmendPensionsConnector(
      http = mockHttpClient,
      appConfig = mockAppConfig
    )

    MockedAppConfig.ifsBaseUrl returns baseUrl
    MockedAppConfig.ifsToken returns "ifs-token"
    MockedAppConfig.ifsEnvironment returns "ifs-environment"
    MockedAppConfig.ifsEnvironmentHeaders returns Some(allowedIfsHeaders)
  }

  "AmendPensionsConnector" when {
    val outcome                                      = Right(ResponseWrapper(correlationId, ()))
    val requiredIfsHeadersPut: Seq[(String, String)] = requiredIfsHeaders ++ Seq("Content-Type" -> "application/json")
    "amendPensions" must {
      "return a 204 status for a success scenario" in new IfsTest with Test {
        implicit override val hc: HeaderCarrier = HeaderCarrier(otherHeaders = otherHeaders ++ Seq("Content-Type" -> "application/json"))

        MockedHttpClient
          .put(
            url = s"$baseUrl/income-tax/income/pensions/$nino/$taxYear",
            config = dummyIfsHeaderCarrierConfig,
            body = amendPensionsRequestBody,
            requiredHeaders = requiredIfsHeadersPut,
            excludedHeaders = Seq("AnotherHeader" -> "HeaderValue")
          )
          .returns(Future.successful(outcome))

        await(connector.amendPensions(amendPensionsRequest(taxYear))) shouldBe outcome
      }
    }
    "amend pensions for a TYS tax year" must {
      "return a 204 status for a success scenario" in new TysIfsTest with Test {
        implicit override val hc: HeaderCarrier = HeaderCarrier(otherHeaders = otherHeaders ++ Seq("Content-Type" -> "application/json"))

        lazy val taxYear: String = "2023-24"

        MockedHttpClient
          .put(
            url = s"$baseUrl/income-tax/income/pensions/23-24/$nino",
            config = dummyIfsHeaderCarrierConfig,
            body = amendPensionsRequestBody,
            requiredHeaders = requiredTysIfsHeaders,
            excludedHeaders = Seq("AnotherHeader" -> "HeaderValue")
          )
          .returns(Future.successful(outcome))

        await(connector.amendPensions(amendPensionsRequest(taxYear))) shouldBe outcome

      }
    }
  }

}
