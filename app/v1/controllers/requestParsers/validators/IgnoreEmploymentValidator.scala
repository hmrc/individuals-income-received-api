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

import config.{AppConfig, FeatureSwitch}
import javax.inject.Inject
import utils.CurrentDateTime
import v1.controllers.requestParsers.validators.validations._
import v1.models.errors.MtdError
import v1.models.request.ignoreEmployment.{IgnoreEmploymentRawData, IgnoreEmploymentRequestBody}

class IgnoreEmploymentValidator @Inject()(implicit currentDateTime: CurrentDateTime, appConfig: AppConfig)
  extends Validator[IgnoreEmploymentRawData] {

  private val validationSet = List(parameterFormatValidation, parameterRuleValidation, bodyFormatValidator)

  override def validate(data: IgnoreEmploymentRawData): List[MtdError] = {
    run(validationSet, data).distinct
  }

  private def parameterFormatValidation: IgnoreEmploymentRawData => List[List[MtdError]] = (data: IgnoreEmploymentRawData) => {
    List(
      NinoValidation.validate(data.nino),
      TaxYearValidation.validate(data.taxYear),
      EmploymentIdValidation.validate(data.employmentId)
    )
  }

  private def parameterRuleValidation: IgnoreEmploymentRawData => List[List[MtdError]] = (data: IgnoreEmploymentRawData) => {
    val featureSwitch = FeatureSwitch(appConfig.featureSwitch)

    List(
      TaxYearNotSupportedValidation.validate(data.taxYear),
      if (featureSwitch.isTaxYearNotEndedRuleEnabled) TaxYearNotEndedValidation.validate(data.taxYear)  else List.empty[MtdError]
    )
  }

  private def bodyFormatValidator: IgnoreEmploymentRawData => List[List[MtdError]] = { data =>
    List(
      JsonFormatValidation.validate[IgnoreEmploymentRequestBody](data.body.json)
    )
  }
}
