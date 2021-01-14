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
import v1.models.request.deleteCustomEmployment.DeleteCustomEmploymentRawData

class DeleteCustomEmploymentValidator @Inject()(implicit appConfig: AppConfig)
  extends Validator[DeleteCustomEmploymentRawData] {

  private val validationSet = List(parameterFormatValidation, parameterRuleValidation)

  override def validate(data: DeleteCustomEmploymentRawData): List[MtdError] = {
    run(validationSet, data).distinct
  }

  private def parameterFormatValidation: DeleteCustomEmploymentRawData => List[List[MtdError]] = (data: DeleteCustomEmploymentRawData) => {
    List(
      NinoValidation.validate(data.nino),
      TaxYearValidation.validate(data.taxYear),
      EmploymentIdValidation.validate(data.employmentId)
    )
  }

  private def parameterRuleValidation: DeleteCustomEmploymentRawData => List[List[MtdError]] = (data: DeleteCustomEmploymentRawData) => {
    List(
      TaxYearNotSupportedValidation.validate(data.taxYear)
    )
  }
}