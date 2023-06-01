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
import v1.models.request.createAmendUkSavingsAnnualSummary.{CreateAmendUkSavingsAnnualSummaryBody, CreateAmendUkSavingsAnnualSummaryRawData}

import javax.inject.{Inject, Singleton}

@Singleton
class CreateAmendUkSavingsAccountAnnualSummaryValidator @Inject() (appConfig: AppConfig) extends Validator[CreateAmendUkSavingsAnnualSummaryRawData] {

  private val validationSet = List(
    parameterFormatValidation,
    parameterRuleValidation,
    bodyFormatValidation,
    bodyValueValidation
  )

  override def validate(data: CreateAmendUkSavingsAnnualSummaryRawData): List[MtdError] = {
    run(validationSet, data).distinct
  }

  private def parameterFormatValidation: CreateAmendUkSavingsAnnualSummaryRawData => List[List[MtdError]] = data => {
    List(
      NinoValidation.validate(data.nino),
      TaxYearValidation.validate(data.taxYear),
      SavingsAccountIdValidation.validate(data.savingsAccountId)
    )
  }

  private def parameterRuleValidation: CreateAmendUkSavingsAnnualSummaryRawData => List[List[MtdError]] = data => {
    List(
      TaxYearNotSupportedValidation.validate(data.taxYear, appConfig.ukDividendsMinimumTaxYear)
    )
  }

  private def bodyFormatValidation: CreateAmendUkSavingsAnnualSummaryRawData => List[List[MtdError]] = { data =>
    List(
      JsonFormatValidation.validate[CreateAmendUkSavingsAnnualSummaryBody](data.body.json)
    )

  }

  private def bodyValueValidation: CreateAmendUkSavingsAnnualSummaryRawData => List[List[MtdError]] = { data =>
    val requestBody = data.body.json.as[CreateAmendUkSavingsAnnualSummaryBody]

    List(
      flattenErrors(
        List(
          DecimalValueValidation.validateOptional(
            amount = requestBody.taxedUkInterest,
            path = "/taxedUkInterest"
          ),
          DecimalValueValidation.validateOptional(
            amount = requestBody.untaxedUkInterest,
            path = "/untaxedUkInterest"
          )
        )
      )
    )

  }

}
