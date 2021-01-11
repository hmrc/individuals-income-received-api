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
import v1.models.request.amendPensions.{AmendForeignPensionsItem, AmendOverseasPensionContributionsItem, AmendPensionsRequest, AmendPensionsRequestBody}

import scala.concurrent.Future


class AmendPensionsConnectorSpec extends ConnectorSpec {

  private val nino: String = "AA111111A"
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
    AmendOverseasPensionContributionsItem(
      customerReference = Some("PENSIONINCOME555"),
      exemptEmployersPensionContribs = 300.33,
      migrantMemReliefQopsRefNo = Some("QOPS000001"),
      dblTaxationRelief = Some(1.23),
      dblTaxationCountryCode = Some("ENG"),
      dblTaxationArticle = Some("AB1123-1"),
      dblTaxationTreaty = Some("Treaty"),
      sf74reference = Some("SF74-654321")
    ),
    AmendOverseasPensionContributionsItem(
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

  private val amendPensionsRequest: AmendPensionsRequest = AmendPensionsRequest(
      nino = Nino(nino),
      taxYear = taxYear,
      body = amendPensionsRequestBody
  )

  class Test extends MockHttpClient with MockAppConfig {

    val connector: AmendPensionsConnector = new AmendPensionsConnector(
      http = mockHttpClient,
      appConfig = mockAppConfig
    )

    MockedAppConfig.desBaseUrl returns baseUrl
    MockedAppConfig.desToken returns "des-token"
    MockedAppConfig.desEnvironment returns "des-environment"
  }

  "AmendPensionsConnector" when {
    "amendPensions" must {
      "return a 204 status for a success scenario" in new Test {
        val outcome = Right(ResponseWrapper(correlationId, ()))

        MockedHttpClient
          .put(
            url = s"$baseUrl/income-tax/income/pensions/$nino/$taxYear",
            body = amendPensionsRequestBody,
            requiredHeaders = requiredHeaders :_*
          ).returns(Future.successful(outcome))

        await(connector.amendPensions(amendPensionsRequest)) shouldBe outcome
      }
    }
  }
}