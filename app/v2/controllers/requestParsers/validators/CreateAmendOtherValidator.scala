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

package v2.controllers.requestParsers.validators

import api.controllers.requestParsers.validators.Validator
import api.controllers.requestParsers.validators.validations._
import api.models.errors.MtdError
import config.AppConfig
import v2.controllers.requestParsers.validators.validations.DateFormatValidation
import v2.models.request.createAmendOther._

import javax.inject.{Inject, Singleton}

@Singleton
class CreateAmendOtherValidator @Inject() (implicit appConfig: AppConfig) extends Validator[CreateAmendOtherRawData] with ValueFormatErrorMessages {

  private val validationSet = List(parameterFormatValidation, parameterRuleValidation, bodyFormatValidator, bodyValueValidator)

  override def validate(data: CreateAmendOtherRawData): List[MtdError] = {
    run(validationSet, data).distinct
  }

  private def parameterFormatValidation: CreateAmendOtherRawData => List[List[MtdError]] = (data: CreateAmendOtherRawData) => {
    List(
      NinoValidation.validate(data.nino),
      TaxYearValidation.validate(data.taxYear)
    )
  }

  private def parameterRuleValidation: CreateAmendOtherRawData => List[List[MtdError]] = { data =>
    List(
      TaxYearNotSupportedValidation.validate(data.taxYear, appConfig.minimumPermittedTaxYear)
    )
  }

  private def bodyFormatValidator: CreateAmendOtherRawData => List[List[MtdError]] = { data =>
    List(
      JsonFormatValidation.validate[CreateAmendOtherRequestBody](data.body.json)
    )
  }

  private def bodyValueValidator: CreateAmendOtherRawData => List[List[MtdError]] = { data =>
    val requestBodyData = data.body.json.as[CreateAmendOtherRequestBody]

    List(
      flattenErrors(
        List(
          requestBodyData.postCessationReceipts.map(_.zipWithIndex.flatMap{ case (data, index) =>
              validatePostCessationReceiptsItem(data, index)
          }).getOrElse(NoValidationErrors)
            .toList,
          requestBodyData.businessReceipts
            .map(_.zipWithIndex.flatMap { case (data, index) =>
              validateBusinessReceipts(data, index)
            })
            .getOrElse(NoValidationErrors)
            .toList,
          requestBodyData.allOtherIncomeReceivedWhilstAbroad
            .map(_.zipWithIndex.flatMap { case (data, index) =>
              validateAllOtherIncomeReceivedWhilstAbroad(data, index)
            })
            .getOrElse(NoValidationErrors)
            .toList,
          requestBodyData.overseasIncomeAndGains.map(validateOverseasIncomeAndGains).getOrElse(NoValidationErrors),
          requestBodyData.chargeableForeignBenefitsAndGifts.map(validateChargeableForeignBenefitsAndGifts).getOrElse(NoValidationErrors),
          requestBodyData.omittedForeignIncome.map(validateOmittedForeignIncome).getOrElse(NoValidationErrors)
        )
      ))
  }

  private def validateBusinessReceipts(businessReceipts: BusinessReceiptsItem, arrayIndex: Int): List[MtdError] = {
    List(
      DecimalValueValidation.validate(
        amount = businessReceipts.grossAmount,
        path = s"/businessReceipts/$arrayIndex/grossAmount"
      ),
      TaxYearValidation
        .validate(businessReceipts.taxYear)
        .map(
          _.copy(paths = Some(Seq(s"/businessReceipts/$arrayIndex/taxYear")))
        )
    ).flatten
  }
    private def validatePostCessationReceiptsItem(postCessationReceiptsItem: PostCessationReceiptsItem, arrayIndex: Int): List[MtdError] = {
      List(
        DateFormatValidation.validateOptional(
          date = postCessationReceiptsItem.dateBusinessCeased,
          path = Some(s"/postCessationReceipts/$arrayIndex/dateBusinessCeased")
        )
      ).flatten
  }

  private def validateAllOtherIncomeReceivedWhilstAbroad(allOtherIncomeReceivedWhilstAbroad: AllOtherIncomeReceivedWhilstAbroadItem,
                                                         arrayIndex: Int): List[MtdError] = {
    List(
      CountryCodeValidation
        .validate(allOtherIncomeReceivedWhilstAbroad.countryCode)
        .map(
          _.copy(paths = Some(Seq(s"/allOtherIncomeReceivedWhilstAbroad/$arrayIndex/countryCode")))
        ),
      DecimalValueValidation.validateOptional(
        amount = allOtherIncomeReceivedWhilstAbroad.amountBeforeTax,
        path = s"/allOtherIncomeReceivedWhilstAbroad/$arrayIndex/amountBeforeTax"
      ),
      DecimalValueValidation.validateOptional(
        amount = allOtherIncomeReceivedWhilstAbroad.taxTakenOff,
        path = s"/allOtherIncomeReceivedWhilstAbroad/$arrayIndex/taxTakenOff"
      ),
      DecimalValueValidation.validateOptional(
        amount = allOtherIncomeReceivedWhilstAbroad.specialWithholdingTax,
        path = s"/allOtherIncomeReceivedWhilstAbroad/$arrayIndex/specialWithholdingTax"
      ),
      DecimalValueValidation.validate(
        amount = allOtherIncomeReceivedWhilstAbroad.taxableAmount,
        path = s"/allOtherIncomeReceivedWhilstAbroad/$arrayIndex/taxableAmount"
      ),
      DecimalValueValidation.validateOptional(
        amount = allOtherIncomeReceivedWhilstAbroad.residentialFinancialCostAmount,
        path = s"/allOtherIncomeReceivedWhilstAbroad/$arrayIndex/residentialFinancialCostAmount"
      ),
      DecimalValueValidation.validateOptional(
        amount = allOtherIncomeReceivedWhilstAbroad.broughtFwdResidentialFinancialCostAmount,
        path = s"/allOtherIncomeReceivedWhilstAbroad/$arrayIndex/broughtFwdResidentialFinancialCostAmount"
      )
    ).flatten
  }

  private def validateOverseasIncomeAndGains(overseasIncomeAndGains: OverseasIncomeAndGains): List[MtdError] = {
    List(
      DecimalValueValidation.validate(
        amount = overseasIncomeAndGains.gainAmount,
        path = "/overseasIncomeAndGains/gainAmount"
      )
    ).flatten
  }

  private def validateChargeableForeignBenefitsAndGifts(chargeableForeignBenefitsAndGifts: ChargeableForeignBenefitsAndGifts): List[MtdError] = {
    List(
      DecimalValueValidation.validateOptional(
        amount = chargeableForeignBenefitsAndGifts.transactionBenefit,
        path = "/chargeableForeignBenefitsAndGifts/transactionBenefit"
      ),
      DecimalValueValidation.validateOptional(
        amount = chargeableForeignBenefitsAndGifts.protectedForeignIncomeSourceBenefit,
        path = "/chargeableForeignBenefitsAndGifts/protectedForeignIncomeSourceBenefit"
      ),
      DecimalValueValidation.validateOptional(
        amount = chargeableForeignBenefitsAndGifts.protectedForeignIncomeOnwardGift,
        path = "/chargeableForeignBenefitsAndGifts/protectedForeignIncomeOnwardGift"
      ),
      DecimalValueValidation.validateOptional(
        amount = chargeableForeignBenefitsAndGifts.benefitReceivedAsASettler,
        path = "/chargeableForeignBenefitsAndGifts/benefitReceivedAsASettler"
      ),
      DecimalValueValidation.validateOptional(
        amount = chargeableForeignBenefitsAndGifts.onwardGiftReceivedAsASettler,
        path = "/chargeableForeignBenefitsAndGifts/onwardGiftReceivedAsASettler"
      )
    ).flatten
  }

  private def validateOmittedForeignIncome(omittedForeignIncome: OmittedForeignIncome): List[MtdError] = {
    List(
      DecimalValueValidation.validate(
        amount = omittedForeignIncome.amount,
        path = "/omittedForeignIncome/amount"
      )
    ).flatten
  }

}
