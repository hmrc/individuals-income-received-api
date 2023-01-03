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

package v1.requestParsers.validators

import api.models.errors.MtdError
import api.requestParsers.validators.Validator
import config.AppConfig
import v1.models.request.retrieveUkSavingsAnnualSummary.RetrieveUkSavingsAnnualSummaryRawData
import v1.requestParsers.validators.validations.{NinoValidation, SavingsAccountIdValidation, TaxYearNotSupportedValidation, TaxYearValidation}

import javax.inject.{Inject, Singleton}

@Singleton
class RetrieveUkSavingsAccountValidator @Inject()(implicit appConfig: AppConfig) extends Validator[RetrieveUkSavingsAnnualSummaryRawData] {

  private val validationSet = List(parameterFormatValidation, parameterRuleValidation)

  override def validate(data: RetrieveUkSavingsAnnualSummaryRawData): List[MtdError] = {
    run(validationSet, data).distinct
  }

  private def parameterFormatValidation: RetrieveUkSavingsAnnualSummaryRawData => List[List[MtdError]] = (data: RetrieveUkSavingsAnnualSummaryRawData) => {
    List(
      NinoValidation.validate(data.nino),
      TaxYearValidation.validate(data.taxYear),
      SavingsAccountIdValidation.validate(data.savingsAccountId)
    )
  }

  private def parameterRuleValidation: RetrieveUkSavingsAnnualSummaryRawData => List[List[MtdError]] = (data: RetrieveUkSavingsAnnualSummaryRawData) => {
    List(
      TaxYearNotSupportedValidation.validate(data.taxYear, appConfig.minimumPermittedTaxYear)
    )
  }

}
