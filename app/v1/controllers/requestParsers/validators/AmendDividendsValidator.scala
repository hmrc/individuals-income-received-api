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

package v1.controllers.requestParsers.validators

import config.AppConfig
import javax.inject.Inject
import v1.controllers.requestParsers.validators.validations._
import v1.models.errors.MtdError
import v1.models.request.amendDividends._

class AmendDividendsValidator @Inject()(implicit appConfig: AppConfig)
  extends Validator[AmendDividendsRawData] with ValueFormatErrorMessages {

  private val validationSet = List(parameterFormatValidation, parameterRuleValidation, bodyFormatValidator, bodyValueValidator)

  override def validate(data: AmendDividendsRawData): List[MtdError] = {
    run(validationSet, data).distinct
  }

  private def parameterFormatValidation: AmendDividendsRawData => List[List[MtdError]] = (data: AmendDividendsRawData) => {
    List(
      NinoValidation.validate(data.nino),
      TaxYearValidation.validate(data.taxYear)
    )
  }

  private def parameterRuleValidation: AmendDividendsRawData => List[List[MtdError]] = { data =>
    List(
      TaxYearNotSupportedValidation.validate(data.taxYear)
    )
  }

  private def bodyFormatValidator: AmendDividendsRawData => List[List[MtdError]] = { data =>
    List(
      JsonFormatValidation.validate[AmendDividendsRequestBody](data.body.json)
    )
  }

  private def bodyValueValidator: AmendDividendsRawData => List[List[MtdError]] = { data =>

    val requestBodyData = data.body.json.as[AmendDividendsRequestBody]

    List(Validator.flattenErrors(
      List(
        requestBodyData.foreignDividend.map(_.zipWithIndex.flatMap {
          case (data, index) => validateForeignDividend(data, index)
        }).getOrElse(NoValidationErrors).toList,
        requestBodyData.dividendIncomeReceivedWhilstAbroad.map(_.zipWithIndex.flatMap {
          case (data, index) => validateDividendIncomeReceivedWhilstAbroad(data, index)
        }).getOrElse(NoValidationErrors).toList,
        requestBodyData.stockDividend.map { data => validateCommonDividends(data, "stockDividend")}.getOrElse(NoValidationErrors),
        requestBodyData.redeemableShares.map { data => validateCommonDividends(data, "redeemableShares")}.getOrElse(NoValidationErrors),
        requestBodyData.bonusIssuesOfSecurities.map { data => validateCommonDividends(data, "bonusIssuesOfSecurities")}.getOrElse(NoValidationErrors),
        requestBodyData.closeCompanyLoansWrittenOff.map { data => validateCommonDividends(data, "closeCompanyLoansWrittenOff")}.getOrElse(NoValidationErrors)
      )
    ))
  }

  private def validateForeignDividend(foreignDividend: AmendForeignDividendItem, arrayIndex: Int): List[MtdError] = {
    List(
      CountryCodeValidation.validate(foreignDividend.countryCode).map(
        _.copy(paths = Some(Seq(s"/foreignDividend/$arrayIndex/countryCode")))
      ),
      DecimalValueValidation.validateOptional(
        amount = foreignDividend.amountBeforeTax,
        path = s"/foreignDividend/$arrayIndex/amountBeforeTax"
      ),
      DecimalValueValidation.validateOptional(
        amount = foreignDividend.taxTakenOff,
        path = s"/foreignDividend/$arrayIndex/taxTakenOff"
      ),
      DecimalValueValidation.validateOptional(
        amount = foreignDividend.specialWithholdingTax,
        path = s"/foreignDividend/$arrayIndex/specialWithholdingTax"
      ),
      DecimalValueValidation.validate(
        amount = foreignDividend.taxableAmount,
        path = s"/foreignDividend/$arrayIndex/taxableAmount"
      )
    ).flatten
  }

  private def validateDividendIncomeReceivedWhilstAbroad(dividendIncomeReceivedWhilstAbroad: AmendDividendIncomeReceivedWhilstAbroadItem, arrayIndex: Int): List[MtdError] = {
    List(
      CountryCodeValidation.validate(dividendIncomeReceivedWhilstAbroad.countryCode).map(
        _.copy(paths = Some(Seq(s"/dividendIncomeReceivedWhilstAbroad/$arrayIndex/countryCode")))
      ),
      DecimalValueValidation.validateOptional(
        amount = dividendIncomeReceivedWhilstAbroad.amountBeforeTax,
        path = s"/dividendIncomeReceivedWhilstAbroad/$arrayIndex/amountBeforeTax"
      ),
      DecimalValueValidation.validateOptional(
        amount = dividendIncomeReceivedWhilstAbroad.taxTakenOff,
        path = s"/dividendIncomeReceivedWhilstAbroad/$arrayIndex/taxTakenOff"
      ),
      DecimalValueValidation.validateOptional(
        amount = dividendIncomeReceivedWhilstAbroad.specialWithholdingTax,
        path = s"/dividendIncomeReceivedWhilstAbroad/$arrayIndex/specialWithholdingTax"
      ),
      DecimalValueValidation.validate(
        amount = dividendIncomeReceivedWhilstAbroad.taxableAmount,
        path = s"/dividendIncomeReceivedWhilstAbroad/$arrayIndex/taxableAmount"
      )
    ).flatten
  }

  private def validateCommonDividends(commonDividends: AmendCommonDividends, fieldName: String): List[MtdError] = {
    List(
      CustomerRefInsuranceValidation.validateOptional(commonDividends.customerReference).map(
        _.copy(paths = Some(Seq(s"/$fieldName/customerReference")))
      ),
      DecimalValueValidation.validate(
        amount = commonDividends.grossAmount,
        path = s"/$fieldName/grossAmount"
      )
    ).flatten
  }
}