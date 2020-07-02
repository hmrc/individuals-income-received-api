/*
 * Copyright 2020 HM Revenue & Customs
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

import play.api.libs.json.{JsError, JsObject, Json}
import support.UnitSpec

class RetrieveDividendsResponseSpec extends UnitSpec {

  private val json = Json.parse(
    """
      |{
      |    "foreignDividend": [
      |      {
      |        "countryCode": "GER",
      |        "amountBeforeTax": 1232.22,
      |        "taxTakenOff": 22.22,
      |        "specialWithholdingTax": 22.22,
      |        "foreignTaxCreditRelief": true,
      |        "taxableAmount": 2321.22
      |      }
      |    ],
      |    "dividendIncomeReceivedWhilstAbroad": [
      |      {
      |        "countryCode": "GER",
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
      countryCode = "GER",
      amountBeforeTax = Some(1232.22),
      taxTakenOff = Some(22.22),
      specialWithholdingTax = Some(22.22),
      foreignTaxCreditRelief = true,
      taxableAmount = 2321.22
    )
  )

  private val dividendIncomeReceivedWhilstAbroadModel = Seq(
    DividendIncomeReceivedWhilstAbroadItem(
      countryCode = "GER",
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
  }

  "read from empty JSON" should {
    "produce an empty RetrieveDividendsResponse object" in {
      val emptyJson = JsObject.empty

      emptyJson.as[RetrieveDividendsResponse] shouldBe RetrieveDividendsResponse.empty
    }
  }

  "read from valid JSON with empty foreignDividend and dividendIncomeReceivedWhilstAbroad arrays" should {
    "produce an empty RetrieveDividendsResponse object" in {
      val json = Json.parse(
        """
          |{
          |   "foreignDividend": [ ],
          |   "dividendIncomeReceivedWhilstAbroad": [ ]
          |}
        """.stripMargin
      )

      json.as[RetrieveDividendsResponse] shouldBe RetrieveDividendsResponse.empty
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
