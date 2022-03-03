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

package v1.controllers.requestParsers.validators

import config.AppConfig
import javax.inject.{Inject, Singleton}
import utils.CurrentDateTime
import v1.controllers.requestParsers.validators.validations._
import v1.models.errors.MtdError
import v1.models.request.createAmendNonPayeEmployment.{CreateAmendNonPayeEmploymentRawData, CreateAmendNonPayeEmploymentRequestBody}

@Singleton
class CreateAmendNonPayeEmploymentValidator @Inject()(implicit currentDateTime: CurrentDateTime, appConfig: AppConfig)
    extends Validator[CreateAmendNonPayeEmploymentRawData] {
  private val validationSet = List(
    parameterFormatValidation,
    parameterRuleValidation,
    bodyFormatValidator,
    bodyValueValidator
  )

  override def validate(data: CreateAmendNonPayeEmploymentRawData): List[MtdError] = {
    run(validationSet, data).distinct
  }

  private def parameterFormatValidation: CreateAmendNonPayeEmploymentRawData => List[List[MtdError]] = data => {
    List(
      NinoValidation.validate(data.nino),
      TaxYearValidation.validate(data.taxYear)
    )
  }

  private def parameterRuleValidation: CreateAmendNonPayeEmploymentRawData => List[List[MtdError]] = data => {
    List(
      TaxYearNotSupportedValidation.validate(data.taxYear, appConfig.minimumPermittedTaxYear),
      TaxYearNotEndedValidation.validate(data.taxYear)
    )
  }

  private def bodyFormatValidator: CreateAmendNonPayeEmploymentRawData => List[List[MtdError]] = { data =>
    List(
      JsonFormatValidation.validate[CreateAmendNonPayeEmploymentRequestBody](data.body.json)
    )
  }

  private def bodyValueValidator: CreateAmendNonPayeEmploymentRawData => List[List[MtdError]] = { data =>
    val requestBody = data.body.json.as[CreateAmendNonPayeEmploymentRequestBody]

    List(
      TipsValidation.validateWithPath(
        amount = requestBody.tips,
        path = s"/tips"
      )
    )
  }
}
