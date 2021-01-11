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

package v1.models.response.retrieveDividends

import mocks.MockAppConfig
import play.api.libs.json.{JsError, Json}
import support.UnitSpec
import v1.hateoas.HateoasFactory
import v1.models.hateoas.{HateoasWrapper, Link}
import v1.models.hateoas.Method.{DELETE, GET, PUT}

class RetrieveDividendsResponseSpec extends UnitSpec {

  private val json = Json.parse(
    """
      |{
      |    "submittedOn": "2020-07-06T09:37:17Z",
      |    "foreignDividend": [
      |      {
      |        "countryCode": "DEU",
      |        "amountBeforeTax": 1232.22,
      |        "taxTakenOff": 22.22,
      |        "specialWithholdingTax": 22.22,
      |        "foreignTaxCreditRelief": true,
      |        "taxableAmount": 2321.22
      |      }
      |    ],
      |    "dividendIncomeReceivedWhilstAbroad": [
      |      {
      |        "countryCode": "DEU",
      |        "amountBeforeTax": 1232.22,
      |        "taxTakenOff": 22.22,
      |        "specialWithholdingTax": 22.22,
      |        "foreignTaxCreditRelief": true,
      |        "taxableAmount": 2321.22
      |      }
      |    ],
      |    "stockDividend": {
      |      "customerReference": "my divs",
      |      "grossAmount": 12321.22
      |      },
      |    "redeemableShares": {
      |      "customerReference": "my shares",
      |      "grossAmount": 12321.22
      |    },
      |      "bonusIssuesOfSecurities": {
      |        "customerReference": "my secs",
      |        "grossAmount": 12321.22
      |    },
      |    "closeCompanyLoansWrittenOff": {
      |      "customerReference": "write off",
      |      "grossAmount": 12321.22
      |    }
      |}
    """.stripMargin
  )

  private val foreignDividendModel = Seq(
    ForeignDividendItem(
      countryCode = "DEU",
      amountBeforeTax = Some(1232.22),
      taxTakenOff = Some(22.22),
      specialWithholdingTax = Some(22.22),
      foreignTaxCreditRelief = true,
      taxableAmount = 2321.22
    )
  )

  private val dividendIncomeReceivedWhilstAbroadModel = Seq(
    DividendIncomeReceivedWhilstAbroadItem(
      countryCode = "DEU",
      amountBeforeTax = Some(1232.22),
      taxTakenOff = Some(22.22),
      specialWithholdingTax = Some(22.22),
      foreignTaxCreditRelief = true,
      taxableAmount = 2321.22
    )
  )

  private val stockDividendModel = StockDividend(customerReference = Some ("my divs"), grossAmount = 12321.22)

  private val redeemableSharesModel = RedeemableShares(customerReference = Some ("my shares"), grossAmount = 12321.22)

  private val bonusIssuesOfSecuritiesModel = BonusIssuesOfSecurities(customerReference = Some ("my secs"), grossAmount = 12321.22)

  private val closeCompanyLoansWrittenOffModel = CloseCompanyLoansWrittenOff(customerReference = Some ("write off"), grossAmount = 12321.22)

  private val responseModel = RetrieveDividendsResponse(
    "2020-07-06T09:37:17Z",
    Some(foreignDividendModel),
    Some(dividendIncomeReceivedWhilstAbroadModel),
    Some(stockDividendModel),
    Some(redeemableSharesModel),
    Some(bonusIssuesOfSecuritiesModel),
    Some(closeCompanyLoansWrittenOffModel)
  )

  "RetrieveDividendsResponse" when {
    "read from valid JSON" should {
      "produce the expected RetrieveDividendsResponse object" in {
        json.as[RetrieveDividendsResponse] shouldBe responseModel
      }
    }

    "read from invalid JSON" should {
      "produce a JsError" in {
        val invalidJson = Json.parse(
          """
            |{
            |   "foreignDividend": [
            |      {
            |        "countryCode": true,
            |        "amountBeforeTax": 1232.22,
            |        "taxTakenOff": 22.22,
            |        "specialWitholdingTax": 22.22,
            |        "foreignTaxCreditRelief": true,
            |        "taxableAmount": 2321.22
            |      }
            |    ]
            |}
          """.stripMargin
        )
        invalidJson.validate[RetrieveDividendsResponse] shouldBe a[JsError]
      }
    }
    "written to JSON" should {
      "produce the expected JsObject" in {
        Json.toJson(responseModel) shouldBe json
      }
    }
  }
  "LinksFactory" when {
    class Test extends MockAppConfig {
      val hateoasFactory = new HateoasFactory(mockAppConfig)
      val nino = "someNino"
      val taxYear = "2019-20"
      MockedAppConfig.apiGatewayContext.returns("individuals/income-received").anyNumberOfTimes
    }

    "wrapping a RetrieveForeignResponse object" should {
      "expose the correct hateoas links" in new Test {
        hateoasFactory.wrap(responseModel, RetrieveDividendsHateoasData(nino, taxYear)) shouldBe
          HateoasWrapper(
            responseModel,
            Seq(
              Link(s"/individuals/income-received/dividends/$nino/$taxYear", PUT, "create-and-amend-dividends-income"),
              Link(s"/individuals/income-received/dividends/$nino/$taxYear", GET, "self"),
              Link(s"/individuals/income-received/dividends/$nino/$taxYear", DELETE, "delete-dividends-income")
            )
          )
      }
    }
  }
}
