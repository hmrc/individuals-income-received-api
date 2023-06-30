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

package v2.models.response.retrieveOther

import api.models.domain.Timestamp
import play.api.libs.json.{JsError, JsObject, Json}
import support.UnitSpec

class RetrieveOtherResponseSpec extends UnitSpec {

  private val json = Json.parse(
    """
      |{
      |   "submittedOn":"2019-04-04T01:01:01.000Z",
      |   "postCessationReceipts":[
      |      {
      |         "customerReference":"String",
      |         "businessName":"LsMBEqEWnG9j,9JP9RpgkGmIcF2I30.NpxZRtgN3zA7-b8h-LvHvApdJtpY",
      |         "dateBusinessCeased":"2023-06-01",
      |         "businessDescription":"u2e'VarLXLa\\W&RHojlOZIqm9NDG",
      |         "incomeSource":"string",
      |         "amount":99999999999.99,
      |         "taxYearIncomeToBeTaxed":"2019-20"
      |      }
      |   ],
      |   "businessReceipts":[
      |      {
      |         "grossAmount":5000.99,
      |         "taxYear":"2018-19"
      |      },
      |      {
      |         "grossAmount":6000.99,
      |         "taxYear":"2019-20"
      |      }
      |   ],
      |   "allOtherIncomeReceivedWhilstAbroad":[
      |      {
      |         "countryCode":"FRA",
      |         "amountBeforeTax":1999.99,
      |         "taxTakenOff":2.23,
      |         "specialWithholdingTax":3.23,
      |         "taxableAmount":4.23,
      |         "residentialFinancialCostAmount":2999.99,
      |         "broughtFwdResidentialFinancialCostAmount":1999.99
      |      },
      |      {
      |         "countryCode":"IND",
      |         "amountBeforeTax":2999.99,
      |         "taxTakenOff":3.23,
      |         "specialWithholdingTax":4.23,
      |         "foreignTaxCreditRelief":true,
      |         "taxableAmount":5.23,
      |         "residentialFinancialCostAmount":3999.99,
      |         "broughtFwdResidentialFinancialCostAmount":2999.99
      |      }
      |   ],
      |   "overseasIncomeAndGains":{
      |      "gainAmount":3000.99
      |   },
      |   "chargeableForeignBenefitsAndGifts":{
      |      "transactionBenefit":1999.99,
      |      "protectedForeignIncomeSourceBenefit":2999.99,
      |      "protectedForeignIncomeOnwardGift":3999.99,
      |      "benefitReceivedAsASettler":4999.99,
      |      "onwardGiftReceivedAsASettler":5999.99
      |   },
      |   "omittedForeignIncome":{
      |      "amount":4000.99
      |   }
      |}
    """.stripMargin
  )

  private val postCessationReceiptsItemModel = Seq(
    PostCessationReceiptsItem(
      customerReference = Some("String"),
      businessName = Some("LsMBEqEWnG9j,9JP9RpgkGmIcF2I30.NpxZRtgN3zA7-b8h-LvHvApdJtpY"),
      dateBusinessCeased = Some("2023-06-01"),
      businessDescription = Some("u2e'VarLXLa\\W&RHojlOZIqm9NDG"),
      incomeSource = Some("string"),
      amount = 99999999999.99,
      taxYearIncomeToBeTaxed = "2019-20"
    )
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
      foreignTaxCreditRelief = None,
      taxableAmount = 4.23,
      residentialFinancialCostAmount = Some(2999.99),
      broughtFwdResidentialFinancialCostAmount = Some(1999.99)
    ),
    AllOtherIncomeReceivedWhilstAbroadItem(
      countryCode = "IND",
      amountBeforeTax = Some(2999.99),
      taxTakenOff = Some(3.23),
      specialWithholdingTax = Some(4.23),
      foreignTaxCreditRelief = Some(true),
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
    submittedOn = Timestamp("2019-04-04T01:01:01.000Z"),
    Some(postCessationReceiptsItemModel),
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
          RetrieveOtherResponse(submittedOn = Timestamp("2019-04-04T01:01:01.000Z"), None, None, None, None, None, None)
      }
    }

    "read from a valid JSON with missing foreignTaxCreditRelief field in allOtherIncomeReceivedWhilstAbroad" should {
      "produce an expected RetrieveOtherResponse object with foreignTaxCreditRelief as false" in {
        val json = Json.parse(
          """
            |{
            |   "submittedOn": "2019-04-04T01:01:01Z",
            |   "allOtherIncomeReceivedWhilstAbroad": [
            |      {
            |         "countryCode": "FRA",
            |         "taxableAmount": 4.23
            |      }
            |   ]
            |}
          """.stripMargin
        )

        json.as[RetrieveOtherResponse] shouldBe
          RetrieveOtherResponse(
            submittedOn = Timestamp("2019-04-04T01:01:01.000Z"),
            None,
            None,
            Some(Seq(AllOtherIncomeReceivedWhilstAbroadItem("FRA", None, None, None, None, 4.23, None, None))),
            None,
            None,
            None
          )

      }
    }

    "written to JSON" should {
      "produce the expected JsObject" in {
        Json.toJson(responseModel) shouldBe json
      }
    }
  }

}
