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

package v1.controllers.requestParsers.validators

import api.controllers.requestParsers.validators.Validator
import api.controllers.requestParsers.validators.validations._
import api.models.errors.MtdError
import config.AppConfig
import v1.models.request.createAmendUkDividendsIncomeAnnualSummary._

import javax.inject.{Inject, Singleton}

@Singleton
class CreateAmendUkDividendsIncomeAnnualSummaryValidator @Inject() (appConfig: AppConfig)
    extends Validator[CreateAmendUkDividendsIncomeAnnualSummaryRawData] {

  private val validationSet = List(
    parameterFormatValidation,
    parameterRuleValidation,
    bodyFormatValidator,
    bodyValueValidator
  )

  override def validate(data: CreateAmendUkDividendsIncomeAnnualSummaryRawData): List[MtdError] = {
    run(validationSet, data).distinct
  }

  private def parameterFormatValidation: CreateAmendUkDividendsIncomeAnnualSummaryRawData => List[List[MtdError]] = data => {
    List(
      NinoValidation.validate(data.nino),
      TaxYearValidation.validate(data.taxYear)
    )
  }

  private def parameterRuleValidation: CreateAmendUkDividendsIncomeAnnualSummaryRawData => List[List[MtdError]] = data => {
    List(
      TaxYearNotSupportedValidation.validate(data.taxYear, appConfig.ukDividendsMinimumTaxYear)
    )
  }

  private def bodyFormatValidator: CreateAmendUkDividendsIncomeAnnualSummaryRawData => List[List[MtdError]] = { data =>
    List(
      JsonFormatValidation.validate[CreateAmendUkDividendsIncomeAnnualSummaryBody](data.body.json)
    )

  }

  private def bodyValueValidator: CreateAmendUkDividendsIncomeAnnualSummaryRawData => List[List[MtdError]] = { data =>
    val requestBody = data.body.json.as[CreateAmendUkDividendsIncomeAnnualSummaryBody]

    List(
      flattenErrors(
        List(
          DecimalValueValidation.validateOptional(
            amount = requestBody.ukDividends,
            path = "/ukDividends"
          ),
          DecimalValueValidation.validateOptional(
            amount = requestBody.otherUkDividends,
            path = "/otherUkDividends"
          )
        )
      )
    )

  }

}
