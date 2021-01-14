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

package v1.fixtures.other

import v1.models.request.amendOther._

object AmendOtherServiceConnectorFixture {

  val businessReceiptsModel: Seq[AmendBusinessReceiptsItem] = Seq(
    AmendBusinessReceiptsItem(
      grossAmount = 5000.99,
      taxYear = "2018-19"
    ),
    AmendBusinessReceiptsItem(
      grossAmount = 6000.99,
      taxYear = "2019-20"
    )
  )

  val allOtherIncomeReceivedWhilstAbroadModel: Seq[AmendAllOtherIncomeReceivedWhilstAbroadItem] = Seq(
    AmendAllOtherIncomeReceivedWhilstAbroadItem(
      countryCode = "FRA",
      amountBeforeTax = Some(1999.99),
      taxTakenOff = Some(2.23),
      specialWithholdingTax = Some(3.23),
      foreignTaxCreditRelief = false,
      taxableAmount = 4.23,
      residentialFinancialCostAmount = Some(2999.99),
      broughtFwdResidentialFinancialCostAmount = Some(1999.99)
    ),
    AmendAllOtherIncomeReceivedWhilstAbroadItem(
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

  val overseasIncomeAndGainsModel = AmendOverseasIncomeAndGains(gainAmount = 3000.99)

  val chargeableForeignBenefitsAndGiftsModel = AmendChargeableForeignBenefitsAndGifts(
    transactionBenefit = Some(1999.99),
    protectedForeignIncomeSourceBenefit = Some(2999.99),
    protectedForeignIncomeOnwardGift = Some(3999.99),
    benefitReceivedAsASettler = Some(4999.99),
    onwardGiftReceivedAsASettler = Some(5999.99)
  )

  val omittedForeignIncomeModel = AmendOmittedForeignIncome(amount = 4000.99)

  val requestBodyModel = AmendOtherRequestBody(
    Some(businessReceiptsModel),
    Some(allOtherIncomeReceivedWhilstAbroadModel),
    Some(overseasIncomeAndGainsModel),
    Some(chargeableForeignBenefitsAndGiftsModel),
    Some(omittedForeignIncomeModel)
  )
}
