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

package v1.fixtures.other

import play.api.libs.json.{JsValue, Json}
import v1.models.request.createAmendOther._

object CreateAmendOtherFixtures {

  val businessReceiptsModel: BusinessReceiptsItem = BusinessReceiptsItem(
    grossAmount = 1000.99,
    taxYear = "2018-19"
  )

  val businessReceiptsJson: JsValue = Json.parse(
    s"""
       |{
       |   "grossAmount": 1000.99,
       |   "taxYear": "2018-19"
       |}
       |""".stripMargin
  )

  val allOtherIncomeReceivedWhilstAbroadModel: AllOtherIncomeReceivedWhilstAbroadItem = AllOtherIncomeReceivedWhilstAbroadItem(
    countryCode = "FRA",
    amountBeforeTax = Some(1000.99),
    taxTakenOff = Some(2000.99),
    specialWithholdingTax = Some(3000.99),
    foreignTaxCreditRelief = false,
    taxableAmount = 4000.99,
    residentialFinancialCostAmount = Some(5000.99),
    broughtFwdResidentialFinancialCostAmount = Some(6000.99)
  )

  val allOtherIncomeReceivedWhilstAbroadJson: JsValue = Json.parse(
    s"""
       |{
       |   "countryCode": "FRA",
       |   "amountBeforeTax": 1000.99,
       |   "taxTakenOff": 2000.99,
       |   "specialWithholdingTax": 3000.99,
       |   "foreignTaxCreditRelief": false,
       |   "taxableAmount": 4000.99,
       |   "residentialFinancialCostAmount": 5000.99,
       |   "broughtFwdResidentialFinancialCostAmount": 6000.99
       |}
       |""".stripMargin
  )

  val overseasIncomeAndGainsModel: OverseasIncomeAndGains = OverseasIncomeAndGains(gainAmount = 3000.99)

  val overseasIncomeAndGainsJson: JsValue = Json.parse(
    s"""
       |{
       |   "gainAmount": 3000.99
       |}
       |""".stripMargin
  )

  val chargeableForeignBenefitsAndGiftsModel: ChargeableForeignBenefitsAndGifts = ChargeableForeignBenefitsAndGifts(
    transactionBenefit = Some(1999.99),
    protectedForeignIncomeSourceBenefit = Some(2999.99),
    protectedForeignIncomeOnwardGift = Some(3999.99),
    benefitReceivedAsASettler = Some(4999.99),
    onwardGiftReceivedAsASettler = Some(5999.99)
  )

  val chargeableForeignBenefitsAndGiftsJson: JsValue = Json.parse(
    s"""
       |{
       |   "transactionBenefit": 1999.99,
       |   "protectedForeignIncomeSourceBenefit": 2999.99,
       |   "protectedForeignIncomeOnwardGift": 3999.99,                    
       |   "benefitReceivedAsASettler": 4999.99,
       |   "onwardGiftReceivedAsASettler": 5999.99
       |}
       |""".stripMargin
  )

  val omittedForeignIncomeModel: OmittedForeignIncome = OmittedForeignIncome(amount = 4000.99)

  val omittedForeignIncomeJson: JsValue = Json.parse(
    s"""
       |{
       |   "amount": 4000.99
       |}
       |""".stripMargin
  )

  val requestBodyModel: CreateAmendOtherRequestBody = CreateAmendOtherRequestBody(
    businessReceipts = Some(Seq(businessReceiptsModel)),
    allOtherIncomeReceivedWhilstAbroad = Some(Seq(allOtherIncomeReceivedWhilstAbroadModel)),
    overseasIncomeAndGains = Some(overseasIncomeAndGainsModel),
    chargeableForeignBenefitsAndGifts = Some(chargeableForeignBenefitsAndGiftsModel),
    omittedForeignIncome = Some(omittedForeignIncomeModel)
  )

  val requestBodyJson: JsValue = Json.parse(
    s"""
      |{
      |   "businessReceipts": [$businessReceiptsJson],
      |   "allOtherIncomeReceivedWhilstAbroad": [$allOtherIncomeReceivedWhilstAbroadJson],
      |   "overseasIncomeAndGains": $overseasIncomeAndGainsJson,
      |   "chargeableForeignBenefitsAndGifts": $chargeableForeignBenefitsAndGiftsJson,
      |   "omittedForeignIncome": $omittedForeignIncomeJson
      |}
    """.stripMargin
  )

  def responseWithHateoasLinks(taxYear: String): JsValue = Json.parse(
    s"""
       |{
       |   "links":[
       |      {
       |         "href":"/individuals/income-received/other/AA123456A/$taxYear",
       |         "rel":"create-and-amend-other-income",
       |         "method":"PUT"
       |      },
       |      {
       |         "href":"/individuals/income-received/other/AA123456A/$taxYear",
       |         "rel":"self",
       |         "method":"GET"
       |      },
       |      {
       |         "href":"/individuals/income-received/other/AA123456A/$taxYear",
       |         "rel":"delete-other-income",
       |         "method":"DELETE"
       |      }
       |   ]
       |}
    """.stripMargin
  )

}
