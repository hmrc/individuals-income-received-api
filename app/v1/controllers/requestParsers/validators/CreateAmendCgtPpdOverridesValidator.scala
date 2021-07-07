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
import javax.inject.{Inject, Singleton}
import v1.controllers.requestParsers.validators.validations._
import v1.models.errors._
import v1.models.request.createAmendCgtPpdOverrides._

@Singleton
class CreateAmendCgtPpdOverridesValidator @Inject()(implicit val appConfig: AppConfig)
  extends Validator[CreateAmendCgtPpdOverridesRawData] with ValueFormatErrorMessages {

  private val validationSet = List(parameterFormatValidation, parameterRuleValidation, lossOrGainsValidator, bodyFormatValidator, incorrectOrEmptyBodyValidator, bodyValueValidator)


  override def validate(data: CreateAmendCgtPpdOverridesRawData): List[MtdError] = {
    run(validationSet, data).distinct
  }

  private def parameterFormatValidation: CreateAmendCgtPpdOverridesRawData => List[List[MtdError]] = (data: CreateAmendCgtPpdOverridesRawData) => {
    List(
      NinoValidation.validate(data.nino),
      TaxYearValidation.validate(data.taxYear)
    )
  }

  private def parameterRuleValidation: CreateAmendCgtPpdOverridesRawData => List[List[MtdError]] = (data: CreateAmendCgtPpdOverridesRawData) => {
    List(
      TaxYearNotSupportedValidation.validate(data.taxYear)
    )
  }

  private def bodyFormatValidator: CreateAmendCgtPpdOverridesRawData => List[List[MtdError]] = { data =>
    List(
      JsonFormatValidation.validate[CreateAmendCgtPpdOverridesRequestBody](data.body.json)
    )
  }

  private def incorrectOrEmptyBodyValidator: CreateAmendCgtPpdOverridesRawData => List[List[MtdError]] = { data =>
    val requestBody: CreateAmendCgtPpdOverridesRequestBody = data.body.json.as[CreateAmendCgtPpdOverridesRequestBody]

    if(requestBody.isEmptyOrIncorrectBody) List(List(RuleIncorrectOrEmptyBodyError)) else NoValidationErrors
  }


  private def lossOrGainsValidator: CreateAmendCgtPpdOverridesRawData => List[List[MtdError]] = (data: CreateAmendCgtPpdOverridesRawData) => {
    val requestBody: CreateAmendCgtPpdOverridesRequestBody = data.body.json.as[CreateAmendCgtPpdOverridesRequestBody]

    List(
      requestBody.multiplePropertyDisposals.map(_.zipWithIndex.flatMap {
        case (data, index) => validateBothSupplied(data, index)
      }).getOrElse(NoValidationErrors).toList
    )
  }

  private def validateBothSupplied(multiplePropertyDisposals: MultiplePropertyDisposals, arrayIndex: Int): List[MtdError] = {

      if(multiplePropertyDisposals.isBothSupplied) {
        List(RuleAmountGainLossError)
      }
      else if(multiplePropertyDisposals.isEmpty) {
        List(RuleAmountGainLossError)
      } else NoValidationErrors
  }

  private def bodyValueValidator: CreateAmendCgtPpdOverridesRawData => List[List[MtdError]] = { data =>

    val requestBodyData = data.body.json.as[CreateAmendCgtPpdOverridesRequestBody]

    List(Validator.flattenErrors(
      List(
        requestBodyData.multiplePropertyDisposals.map(_.zipWithIndex.flatMap {
          case (data, index) => validateMultiplePropertyDisposals(data, index)
        }).getOrElse(NoValidationErrors).toList,
        requestBodyData.singlePropertyDisposals.map(_.zipWithIndex.flatMap {
          case (data, index) => validateSinglePropertyDisposals(data, index)
        }).getOrElse(NoValidationErrors).toList,
      )
    ))
  }

  private def validateMultiplePropertyDisposals(multiplePropertyDisposals: MultiplePropertyDisposals, arrayIndex: Int): List[MtdError] = {
    List(
      SubmissionIdValidation.validate(multiplePropertyDisposals.submissionId),
      DecimalValueValidation.validateOptional(
        amount = multiplePropertyDisposals.amountOfNetGain,
        path = s"/multiplePropertyDisposals/amountOfNetGain"
      ),
      DecimalValueValidation.validateOptional(
        amount = multiplePropertyDisposals.amountOfNetLoss,
        path = s"/multiplePropertyDisposals/amountOfNetLoss"
      )
    ).flatten
  }

  private def validateSinglePropertyDisposals(singlePropertyDisposals: SinglePropertyDisposals, arrayIndex: Int): List[MtdError] = {
    List(
      SubmissionIdValidation.validate(singlePropertyDisposals.submissionId),
      DateFormatValidation.validate(singlePropertyDisposals.completionDate, DateFormatError),
      DecimalValueValidation.validate(
        amount = singlePropertyDisposals.disposalProceeds,
        path = s"/singlePropertyDisposals/disposalProceeds"
      ),
      DateFormatValidation.validate(singlePropertyDisposals.acquisitionDate, DateFormatError),
      DecimalValueValidation.validate(
        amount = singlePropertyDisposals.acquisitionAmount,
        path = s"/singlePropertyDisposals/acquisitionAmount"
      ),
      DecimalValueValidation.validate(
        amount = singlePropertyDisposals.improvementCosts,
        path = s"/singlePropertyDisposals/improvementCosts"
      ),
      DecimalValueValidation.validate(
        amount = singlePropertyDisposals.additionalCosts,
        path = s"/singlePropertyDisposals/additionalCosts"
      ),
      DecimalValueValidation.validate(
        amount = singlePropertyDisposals.prfAmount,
        path = s"/singlePropertyDisposals/prfAmount"
      ),
      DecimalValueValidation.validate(
        amount = singlePropertyDisposals.otherReliefAmount,
        path = s"/singlePropertyDisposals/otherReliefAmount"
      ),
      DecimalValueValidation.validateOptional(
        amount = singlePropertyDisposals.lossesFromThisYear,
        path = s"/singlePropertyDisposals/lossesFromThisYear"
      ),
      DecimalValueValidation.validateOptional(
        amount = singlePropertyDisposals.lossesFromPreviousYear,
        path = s"/singlePropertyDisposals/lossesFromPreviousYear"
      ),
      DecimalValueValidation.validateOptional(
        amount = singlePropertyDisposals.amountOfNetGain,
        path = s"/singlePropertyDisposals/amountOfNetGain"
      ),
      DecimalValueValidation.validateOptional(
        amount = singlePropertyDisposals.amountOfNetLoss,
        path = s"/singlePropertyDisposals/amountOfNetLoss"
      )
    ).flatten
  }

}
