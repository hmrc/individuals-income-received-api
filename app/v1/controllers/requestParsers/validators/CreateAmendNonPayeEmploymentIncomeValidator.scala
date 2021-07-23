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
import v1.controllers.requestParsers.validators.validations._
import v1.models.errors._
import v1.models.request.createAmendNonPayeEmploymentIncome._

import javax.inject.{Inject, Singleton}

@Singleton
class CreateAmendNonPayeEmploymentIncomeValidator @Inject()(appConfig: AppConfig) extends Validator[CreateAmendNonPayeEmploymentIncomeRawData] {
  private val validationSet = List(
    parameterFormatValidation,
    parameterRuleValidation,
    bodyFormatValidator,
    bodyValueValidator
  )

  override def validate(data: CreateAmendNonPayeEmploymentIncomeRawData): List[MtdError] = {
    run(validationSet, data).distinct
  }

  private def parameterFormatValidation: CreateAmendNonPayeEmploymentIncomeRawData => List[List[MtdError]] = data => {
    List(
      NinoValidation.validate(data.nino),
      TaxYearValidation.validate(data.taxYear)
    )
  }

  private def parameterRuleValidation: CreateAmendNonPayeEmploymentIncomeRawData => List[List[MtdError]] = data => {
    List(
      TaxYearNotSupportedValidation.validate(data.taxYear, appConfig.minimumPermittedTaxYear)
    )
  }

  private def bodyFormatValidator: CreateAmendNonPayeEmploymentIncomeRawData => List[List[MtdError]] = { data =>
    List(
      JsonFormatValidation.validate[CreateAmendNonPayeEmploymentIncomeRequestBody](data.body.json)
    )
  }

  private def bodyValueValidator: CreateAmendNonPayeEmploymentIncomeRawData => List[List[MtdError]] = { data =>
    val requestBody = data.body.json.as[CreateAmendNonPayeEmploymentIncomeRequestBody]

    List(
      TipsValidation.validate(amount = requestBody.tips)
    )
  }
}
