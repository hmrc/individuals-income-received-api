/*
 * Copyright 2022 HM Revenue & Customs
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

import javax.inject.{Inject, Singleton}
import v1.requestParsers.validators.validations._
import v1.models.request.retrieveFinancialDetails.RetrieveFinancialDetailsRawData
import v1.requestParsers.validators.validations.{
  EmploymentIdValidation,
  EmploymentSourceValidation,
  NinoValidation,
  TaxYearNotSupportedValidation,
  TaxYearValidation
}

@Singleton
class RetrieveFinancialDetailsValidator @Inject() (implicit appConfig: AppConfig) extends Validator[RetrieveFinancialDetailsRawData] {

  private val validationSet = List(parameterFormatValidation, parameterRuleValidation)

  override def validate(data: RetrieveFinancialDetailsRawData): List[MtdError] = {
    run(validationSet, data).distinct
  }

  private def parameterFormatValidation: RetrieveFinancialDetailsRawData => List[List[MtdError]] = (data: RetrieveFinancialDetailsRawData) => {
    List(
      NinoValidation.validate(data.nino),
      TaxYearValidation.validate(data.taxYear),
      EmploymentIdValidation.validate(data.employmentId),
      data.source.map(EmploymentSourceValidation.validate).getOrElse(Nil)
    )
  }

  private def parameterRuleValidation: RetrieveFinancialDetailsRawData => List[List[MtdError]] = (data: RetrieveFinancialDetailsRawData) => {
    List(
      TaxYearNotSupportedValidation.validate(data.taxYear, appConfig.minimumPermittedTaxYear)
    )
  }

}