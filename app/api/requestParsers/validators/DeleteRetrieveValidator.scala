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

package api.requestParsers.validators

import api.models.errors.MtdError
import api.models.request.DeleteRetrieveRawData
import config.AppConfig
import v1r7.requestParsers.validators.validations.{NinoValidation, TaxYearNotSupportedValidation, TaxYearValidation}

import javax.inject.{Inject, Singleton}

@Singleton
class DeleteRetrieveValidator @Inject() (implicit appConfig: AppConfig) extends Validator[DeleteRetrieveRawData] {

  private val validationSet = List(parameterFormatValidation, parameterRuleValidation)

  override def validate(data: DeleteRetrieveRawData): List[MtdError] = {
    run(validationSet, data).distinct
  }

  private def parameterFormatValidation: DeleteRetrieveRawData => List[List[MtdError]] = (data: DeleteRetrieveRawData) => {
    List(
      NinoValidation.validate(data.nino),
      TaxYearValidation.validate(data.taxYear)
    )
  }

  private def parameterRuleValidation: DeleteRetrieveRawData => List[List[MtdError]] = (data: DeleteRetrieveRawData) => {
    List(
      TaxYearNotSupportedValidation.validate(data.taxYear, appConfig.minimumPermittedTaxYear)
    )
  }

}
