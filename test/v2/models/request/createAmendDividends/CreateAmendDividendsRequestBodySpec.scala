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

package v2.models.request.createAmendDividends

import play.api.libs.json.{JsError, JsObject, Json}
import support.UnitSpec

class CreateAmendDividendsRequestBodySpec extends UnitSpec {

  private val json = Json.parse(
    """
      |{
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

  private val jsonWithoutForeignTaxCreditRelief = Json.parse(
    """
      |{
      |    "foreignDividend": [
      |      {
      |        "countryCode": "DEU",
      |        "amountBeforeTax": 1232.22,
      |        "taxTakenOff": 22.22,
      |        "specialWithholdingTax": 22.22,
      |        "taxableAmount": 2321.22
      |      }
      |    ],
      |    "dividendIncomeReceivedWhilstAbroad": [
      |      {
      |        "countryCode": "DEU",
      |        "amountBeforeTax": 1232.22,
      |        "taxTakenOff": 22.22,
      |        "specialWithholdingTax": 22.22,
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
    CreateAmendForeignDividendItem(
      countryCode = "DEU",
      amountBeforeTax = Some(1232.22),
      taxTakenOff = Some(22.22),
      specialWithholdingTax = Some(22.22),
      foreignTaxCreditRelief = Some(true),
      taxableAmount = 2321.22
    )
  )

  private val foreignDividendModelWithoutForeignTaxCreditRelief = Seq(
    CreateAmendForeignDividendItem(
      countryCode = "DEU",
      amountBeforeTax = Some(1232.22),
      taxTakenOff = Some(22.22),
      specialWithholdingTax = Some(22.22),
      foreignTaxCreditRelief = None,
      taxableAmount = 2321.22
    )
  )

  private val dividendIncomeReceivedWhilstAbroadModel = Seq(
    CreateAmendDividendIncomeReceivedWhilstAbroadItem(
      countryCode = "DEU",
      amountBeforeTax = Some(1232.22),
      taxTakenOff = Some(22.22),
      specialWithholdingTax = Some(22.22),
      foreignTaxCreditRelief = Some(true),
      taxableAmount = 2321.22
    )
  )

  private val dividendIncomeReceivedWhilstAbroadModelWithoutForeignTaxCreditRelief = Seq(
    CreateAmendDividendIncomeReceivedWhilstAbroadItem(
      countryCode = "DEU",
      amountBeforeTax = Some(1232.22),
      taxTakenOff = Some(22.22),
      specialWithholdingTax = Some(22.22),
      foreignTaxCreditRelief = None,
      taxableAmount = 2321.22
    )
  )

  private val stockDividend = CreateAmendCommonDividends(customerReference = Some("my divs"), grossAmount = 12321.22)

  private val redeemableShares = CreateAmendCommonDividends(customerReference = Some("my shares"), grossAmount = 12321.22)

  private val bonusIssuesOfSecurities = CreateAmendCommonDividends(customerReference = Some("my secs"), grossAmount = 12321.22)

  private val closeCompanyLoansWrittenOff = CreateAmendCommonDividends(customerReference = Some("write off"), grossAmount = 12321.22)

  private val requestBodyModel = CreateAmendDividendsRequestBody(
    Some(foreignDividendModel),
    Some(dividendIncomeReceivedWhilstAbroadModel),
    Some(stockDividend),
    Some(redeemableShares),
    Some(bonusIssuesOfSecurities),
    Some(closeCompanyLoansWrittenOff)
  )

  "CreateAmendDividendsRequestBody" when {
    "read from valid JSON" should {
      "produce the expected CreateAmendDividendsRequestBody object" in {
        json.as[CreateAmendDividendsRequestBody] shouldBe requestBodyModel
      }

      "produce the expected object without foreignTaxCreditRelief" in {
        jsonWithoutForeignTaxCreditRelief.as[CreateAmendDividendsRequestBody] shouldBe
          requestBodyModel.copy(foreignDividend = Some(foreignDividendModelWithoutForeignTaxCreditRelief),
            dividendIncomeReceivedWhilstAbroad = Some(dividendIncomeReceivedWhilstAbroadModelWithoutForeignTaxCreditRelief))
      }
    }

    "read from empty JSON" should {
      "produce an empty CreateAmendDividendsRequestBody object" in {
        val emptyJson = JsObject.empty

        emptyJson.as[CreateAmendDividendsRequestBody] shouldBe CreateAmendDividendsRequestBody.empty
      }
    }

    "read from valid JSON with empty foreignDividend and dividendIncomeReceivedWhilstAbroad arrays" should {
      "produce an empty CreateAmendDividendsRequestBody object" in {
        val json = Json.parse(
          """
            |{
            |   "foreignDividend": [ ],
            |   "dividendIncomeReceivedWhilstAbroad": [ ]
            |}
        """.stripMargin
        )

        json.as[CreateAmendDividendsRequestBody] shouldBe CreateAmendDividendsRequestBody.empty
      }
    }

    "read from invalid JSON" should {
      "produce a JsError" in {
        val json = Json.parse(
          """
            |{
            |    "foreignDividend": [
            |      {
            |        "countryCode": true,
            |        "amountBeforeTax": 1232.22,
            |        "taxTakenOff": 22.22,
            |        "specialWithholdingTax": 22.22,
            |        "foreignTaxCreditRelief": true,
            |        "taxableAmount": 2321.22
            |      }
            |    ]
            |}
      """.stripMargin
        )

        json.validate[CreateAmendDividendsRequestBody] shouldBe a[JsError]
      }
    }

    "written to JSON" should {
      "produce the expected JsObject" in {
        Json.toJson(requestBodyModel) shouldBe json
      }
    }
  }

}
