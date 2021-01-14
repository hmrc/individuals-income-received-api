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
import v1.models.request.amendDividends._

import scala.concurrent.Future

class AmendDividendsConnectorSpec extends ConnectorSpec {

  private val nino: String = "AA111111A"
  private val taxYear: String = "2019-20"

  private val foreignDividendModel = Seq(
    AmendForeignDividendItem(
      countryCode = "DEU",
      amountBeforeTax = Some(1232.22),
      taxTakenOff = Some(22.22),
      specialWithholdingTax = Some(27.35),
      foreignTaxCreditRelief = true,
      taxableAmount = 2321.22
    ),
    AmendForeignDividendItem(
      countryCode = "FRA",
      amountBeforeTax = Some(1350.55),
      taxTakenOff = Some(25.27),
      specialWithholdingTax = Some(30.59),
      foreignTaxCreditRelief = false,
      taxableAmount = 2500.99
    )
  )

  private val dividendIncomeReceivedWhilstAbroadModel = Seq(
    AmendDividendIncomeReceivedWhilstAbroadItem(
      countryCode = "DEU",
      amountBeforeTax = Some(1232.22),
      taxTakenOff = Some(22.22),
      specialWithholdingTax = Some(27.35),
      foreignTaxCreditRelief = true,
      taxableAmount = 2321.22
    ),
    AmendDividendIncomeReceivedWhilstAbroadItem(
      countryCode = "FRA",
      amountBeforeTax = Some(1350.55),
      taxTakenOff = Some(25.27),
      specialWithholdingTax = Some(30.59),
      foreignTaxCreditRelief = false,
      taxableAmount = 2500.99
    )
  )

  private val stockDividendModel = AmendCommonDividends(
    customerReference = Some("my divs"),
    grossAmount = 12321.22
  )

  private val redeemableSharesModel = AmendCommonDividends(
    customerReference = Some("my shares"),
    grossAmount = 12345.75
  )

  private val bonusIssuesOfSecuritiesModel = AmendCommonDividends(
    customerReference = Some("my secs"),
    grossAmount = 12500.89
  )

  private val closeCompanyLoansWrittenOffModel = AmendCommonDividends(
    customerReference = Some("write off"),
    grossAmount = 13700.55
  )

  private val amendDividendsRequestBody: AmendDividendsRequestBody = AmendDividendsRequestBody(
    Some(foreignDividendModel),
    Some(dividendIncomeReceivedWhilstAbroadModel),
    Some(stockDividendModel),
    Some(redeemableSharesModel),
    Some(bonusIssuesOfSecuritiesModel),
    Some(closeCompanyLoansWrittenOffModel)
  )

  val amendDividendsRequest: AmendDividendsRequest = AmendDividendsRequest(
      nino = Nino(nino),
      taxYear = taxYear,
      body = amendDividendsRequestBody
  )

  class Test extends MockHttpClient with MockAppConfig {

    val connector: AmendDividendsConnector = new AmendDividendsConnector(
      http = mockHttpClient,
      appConfig = mockAppConfig
    )

    MockedAppConfig.desBaseUrl returns baseUrl
    MockedAppConfig.desToken returns "des-token"
    MockedAppConfig.desEnvironment returns "des-environment"
  }

  "AmendDividendsConnector" when {
    "amendDividends" must {
      "return a 204 status for a success scenario" in new Test {
        val outcome = Right(ResponseWrapper(correlationId, ()))

        MockedHttpClient
          .put(
            url = s"$baseUrl/income-tax/income/dividends/$nino/$taxYear",
            body = amendDividendsRequestBody,
            requiredHeaders = requiredHeaders :_*
          ).returns(Future.successful(outcome))

        await(connector.amendDividends(amendDividendsRequest)) shouldBe outcome
      }
    }
  }
}