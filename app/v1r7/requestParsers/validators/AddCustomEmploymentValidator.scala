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

package v1r7.requestParsers.validators

import api.models.errors.MtdError
import api.requestParsers.validators.Validator
import config.{AppConfig, FeatureSwitch}

import javax.inject.{Inject, Singleton}
import utils.CurrentDateTime
import v1r7.requestParsers.validators.validations._
import v1r7.models.request.addCustomEmployment._
import v1r7.requestParsers.validators.validations.{
  CustomEmploymentDateValidation,
  EmployerNameValidation,
  EmployerRefValidation,
  JsonFormatValidation,
  NinoValidation,
  PayrollIdValidation,
  TaxYearNotEndedValidation,
  TaxYearNotSupportedValidation,
  TaxYearValidation
}

@Singleton
class AddCustomEmploymentValidator @Inject() (implicit currentDateTime: CurrentDateTime, appConfig: AppConfig)
    extends Validator[AddCustomEmploymentRawData] {

  private val validationSet = List(parameterFormatValidation, parameterRuleValidation, bodyFormatValidator, bodyValueValidator)

  override def validate(data: AddCustomEmploymentRawData): List[MtdError] = {
    run(validationSet, data).distinct
  }

  private def parameterFormatValidation: AddCustomEmploymentRawData => List[List[MtdError]] = (data: AddCustomEmploymentRawData) => {
    List(
      NinoValidation.validate(data.nino),
      TaxYearValidation.validate(data.taxYear)
    )
  }

  private def parameterRuleValidation: AddCustomEmploymentRawData => List[List[MtdError]] = (data: AddCustomEmploymentRawData) => {
    val featureSwitch = FeatureSwitch(appConfig.featureSwitch)

    List(
      TaxYearNotSupportedValidation.validate(data.taxYear, appConfig.minimumPermittedTaxYear),
      if (featureSwitch.isTaxYearNotEndedRuleEnabled) TaxYearNotEndedValidation.validate(data.taxYear) else List.empty[MtdError]
    )
  }

  private def bodyFormatValidator: AddCustomEmploymentRawData => List[List[MtdError]] = { data =>
    List(
      JsonFormatValidation.validate[AddCustomEmploymentRequestBody](data.body.json)
    )
  }

  private def bodyValueValidator: AddCustomEmploymentRawData => List[List[MtdError]] = (data: AddCustomEmploymentRawData) => {
    val requestBodyData: AddCustomEmploymentRequestBody = data.body.json.as[AddCustomEmploymentRequestBody]

    List(
      EmployerRefValidation.validateOptional(requestBodyData.employerRef),
      EmployerNameValidation.validateCustomEmployment(requestBodyData.employerName),
      CustomEmploymentDateValidation.validate(requestBodyData.startDate, requestBodyData.cessationDate, data.taxYear),
      PayrollIdValidation.validateOptional(requestBodyData.payrollId)
    )
  }

}
