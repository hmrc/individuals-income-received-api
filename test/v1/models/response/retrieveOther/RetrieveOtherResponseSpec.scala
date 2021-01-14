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

package v1.models.response.retrieveOther

import play.api.libs.json.{JsError, JsObject, Json}
import support.UnitSpec

class RetrieveOtherResponseSpec extends UnitSpec {

  private val json = Json.parse(
    """
      |{
      |   "submittedOn": "2019-04-04T01:01:01Z",
      |   "businessReceipts": [
      |      {
      |         "grossAmount": 5000.99,
      |         "taxYear": "2018-19"
      |      },
      |      {
      |         "grossAmount": 6000.99,
      |         "taxYear": "2019-20"
      |      }
      |   ],
      |   "allOtherIncomeReceivedWhilstAbroad": [
      |      {
      |         "countryCode": "FRA",
      |         "amountBeforeTax": 1999.99,
      |         "taxTakenOff": 2.23,
      |         "specialWithholdingTax": 3.23,
      |         "foreignTaxCreditRelief": false,
      |         "taxableAmount": 4.23,
      |         "residentialFinancialCostAmount": 2999.99,
      |         "broughtFwdResidentialFinancialCostAmount": 1999.99
      |      },
      |      {
      |         "countryCode": "IND",
      |         "amountBeforeTax": 2999.99,
      |         "taxTakenOff": 3.23,
      |         "specialWithholdingTax": 4.23,
      |         "foreignTaxCreditRelief": true,
      |         "taxableAmount": 5.23,
      |         "residentialFinancialCostAmount": 3999.99,
      |         "broughtFwdResidentialFinancialCostAmount": 2999.99
      |      }
      |   ],
      |   "overseasIncomeAndGains": {
      |      "gainAmount": 3000.99
      |   },
      |   "chargeableForeignBenefitsAndGifts": {
      |      "transactionBenefit": 1999.99,
      |      "protectedForeignIncomeSourceBenefit": 2999.99,
      |      "protectedForeignIncomeOnwardGift": 3999.99,
      |      "benefitReceivedAsASettler": 4999.99,
      |      "onwardGiftReceivedAsASettler": 5999.99
      |   },
      |   "omittedForeignIncome": {
      |      "amount": 4000.99
      |   }
      |}
    """.stripMargin
  )

  private val businessReceiptsItemModel = Seq(
    BusinessReceiptsItem(
      grossAmount = 5000.99,
      taxYear = "2018-19"
    ),
    BusinessReceiptsItem(
      grossAmount = 6000.99,
      taxYear = "2019-20"
    )
  )

  private val allOtherIncomeReceivedWhilstAbroadItemModel = Seq(
    AllOtherIncomeReceivedWhilstAbroadItem(
      countryCode = "FRA",
      amountBeforeTax = Some(1999.99),
      taxTakenOff = Some(2.23),
      specialWithholdingTax = Some(3.23),
      foreignTaxCreditRelief = false,
      taxableAmount = 4.23,
      residentialFinancialCostAmount = Some(2999.99),
      broughtFwdResidentialFinancialCostAmount = Some(1999.99)
    ),
    AllOtherIncomeReceivedWhilstAbroadItem(
      countryCode = "IND",
      amountBeforeTax = Some(2999.99),
      taxTakenOff = Some(3.23),
      specialWithholdingTax = Some(4.23),
      foreignTaxCreditRelief = true,
      taxableAmount = 5.23,
      residentialFinancialCostAmount = Some(3999.99),
      broughtFwdResidentialFinancialCostAmount = Some(2999.99)
    )
  )

  private val overseasIncomeAndGainsModel = OverseasIncomeAndGains(gainAmount = 3000.99)

  private val chargeableForeignBenefitsAndGiftsModel = ChargeableForeignBenefitsAndGifts(
    transactionBenefit = Some(1999.99),
    protectedForeignIncomeSourceBenefit = Some(2999.99),
    protectedForeignIncomeOnwardGift = Some(3999.99),
    benefitReceivedAsASettler = Some(4999.99),
    onwardGiftReceivedAsASettler = Some(5999.99)
  )

  private val omittedForeignIncomeModel = OmittedForeignIncome(amount = 4000.99)

  private val responseModel = RetrieveOtherResponse(
    submittedOn = "2019-04-04T01:01:01Z",
    Some(businessReceiptsItemModel),
    Some(allOtherIncomeReceivedWhilstAbroadItemModel),
    Some(overseasIncomeAndGainsModel),
    Some(chargeableForeignBenefitsAndGiftsModel),
    Some(omittedForeignIncomeModel)
  )

  "RetrieveOtherResponse" when {
    "read from valid JSON" should {
      "produce the expected RetrieveOtherResponse object" in {
        json.as[RetrieveOtherResponse] shouldBe responseModel
      }
    }

    "read from json with empty chargeableForeignBenefitsAndGifts object, businessReceipts and allOtherIncomeReceivedWhilstAbroad arrays" should {
      "produce a JsError" in {
        val invalidJson = Json.parse(
          """
            |{
            |   "businessReceipts": [ ],
            |   "allOtherIncomeReceivedWhilstAbroad": [ ],
            |   "chargeableForeignBenefitsAndGifts": { }
            |}
          """.stripMargin
        )

        invalidJson.validate[RetrieveOtherResponse] shouldBe a[JsError]

      }
    }

    "read from empty JSON" should {
      "produce a JsError" in {
        val emptyJson = JsObject.empty

        emptyJson.validate[RetrieveOtherResponse] shouldBe a[JsError]
      }
    }

    "read from a valid JSON with submittedOn field," should {
      "produce a expected RetrieveOtherResponse object" in {
        val json = Json.parse(
          """
            |{
            |   "submittedOn":"2019-04-04T01:01:01Z"
            |}
          """.stripMargin
        )

        json.as[RetrieveOtherResponse] shouldBe
          RetrieveOtherResponse(submittedOn = "2019-04-04T01:01:01Z", None, None, None, None, None)
      }
    }

    "written to JSON" should {
      "produce the expected JsObject" in {
        Json.toJson(responseModel) shouldBe json
      }
    }
  }
}
