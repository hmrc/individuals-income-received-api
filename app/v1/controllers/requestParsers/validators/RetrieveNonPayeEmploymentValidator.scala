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
import v1.models.request.retrieveNonPayeEmploymentIncome.RetrieveNonPayeEmploymentIncomeRawData

import javax.inject.Inject

class RetrieveNonPayeEmploymentValidator @Inject() (implicit appConfig: AppConfig) extends Validator[RetrieveNonPayeEmploymentIncomeRawData] {

  private val validationSet = List(parameterFormatValidation, parameterRuleValidation)

  override def validate(data: RetrieveNonPayeEmploymentIncomeRawData): List[MtdError] = {
    run(validationSet, data).distinct
  }

  private def parameterFormatValidation: RetrieveNonPayeEmploymentIncomeRawData => List[List[MtdError]] =
    (data: RetrieveNonPayeEmploymentIncomeRawData) => {
      List(
        NinoValidation.validate(data.nino),
        TaxYearValidation.validate(data.taxYear),
        data.source.map(EmploymentSourceValidation.validate).getOrElse(Nil)
      )
    }

  private def parameterRuleValidation: RetrieveNonPayeEmploymentIncomeRawData => List[List[MtdError]] =
    (data: RetrieveNonPayeEmploymentIncomeRawData) => {
      List(
        TaxYearNotSupportedValidation.validate(data.taxYear, appConfig.minimumPermittedTaxYear)
      )
    }

}
