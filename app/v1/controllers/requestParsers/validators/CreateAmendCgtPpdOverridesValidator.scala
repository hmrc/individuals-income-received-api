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
import utils.CurrentDateTime
import v1.controllers.requestParsers.validators.validations._
import v1.models.errors._
import v1.models.request.createAmendCgtPpdOverrides._

@Singleton
class CreateAmendCgtPpdOverridesValidator @Inject()(implicit currentDateTime: CurrentDateTime, appConfig: AppConfig)
  extends Validator[CreateAmendCgtPpdOverridesRawData] with ValueFormatErrorMessages {

  private val validationSet = List(
    parameterFormatValidation,
    parameterRuleValidation,
    bodyFormatValidator,
    lossOrGainsValidator,
    bodyValueValidator,
    emptySingleOrMultipleDisposalsValidator)


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
      TaxYearNotSupportedValidation.validate(data.taxYear),
      TaxYearNotEndedValidation.validate(data.taxYear)
    )
  }

  private def bodyFormatValidator: CreateAmendCgtPpdOverridesRawData => List[List[MtdError]] = { data =>
    List(
      JsonFormatValidation.validate[CreateAmendCgtPpdOverridesRequestBody](data.body.json)
    )
  }

  private def emptySingleOrMultipleDisposalsValidator: CreateAmendCgtPpdOverridesRawData => List[List[MtdError]] = { data =>
    val requestBody: CreateAmendCgtPpdOverridesRequestBody = data.body.json.as[CreateAmendCgtPpdOverridesRequestBody]

    if (requestBody.singlePropertyDisposals.isEmpty) {
      List(List(RuleIncorrectOrEmptyBodyError.copy(paths = Some(Seq("/singlePropertyDisposals")))))
    }
    else if (requestBody.multiplePropertyDisposals.isEmpty) {
      List(List(RuleIncorrectOrEmptyBodyError.copy(paths = Some(Seq("/multiplePropertyDisposals")))))
    }
    else {
      NoValidationErrors
    }
  }


  private def lossOrGainsValidator: CreateAmendCgtPpdOverridesRawData => List[List[MtdError]] = (data: CreateAmendCgtPpdOverridesRawData) => {
    val requestBody: CreateAmendCgtPpdOverridesRequestBody = data.body.json.as[CreateAmendCgtPpdOverridesRequestBody]
    List(Validator.flattenErrors(
      List(
        requestBody.multiplePropertyDisposals.map(_.zipWithIndex.flatMap {
          case (data, index) => validateBothSuppliedMultipleDisposals(data, index)
        }).getOrElse(NoValidationErrors).toList,
        requestBody.singlePropertyDisposals.map(_.zipWithIndex.flatMap {
          case (data, index) => validateBothSuppliedSingleDisposals(data, index)
        }).getOrElse(NoValidationErrors).toList,
        requestBody.singlePropertyDisposals.map(_.zipWithIndex.flatMap {
          case (data, index) => validateGainGreaterThanLoss(data, index)
        }).getOrElse(NoValidationErrors).toList
      )
    )
    )
  }

  private def validateBothSuppliedMultipleDisposals(multiplePropertyDisposals: MultiplePropertyDisposals, arrayIndex: Int): List[MtdError] = {

    if (multiplePropertyDisposals.isBothSupplied) {
      List(RuleAmountGainLossError.copy(paths = Some(Seq(s"/multiplePropertyDisposals/$arrayIndex"))))
    }
    else if (multiplePropertyDisposals.isEmpty) {
      List(RuleAmountGainLossError.copy(paths = Some(Seq(s"/multiplePropertyDisposals/$arrayIndex"))))
    } else {
      NoValidationErrors
    }
  }

  private def validateBothSuppliedSingleDisposals(singlePropertyDisposals: SinglePropertyDisposals, arrayIndex: Int): List[MtdError] = {

    if (singlePropertyDisposals.isBothSupplied) {
      List(RuleAmountGainLossError.copy(paths = Some(Seq(s"/singlePropertyDisposals/$arrayIndex"))))
    }
    else if (singlePropertyDisposals.isBothEmpty) {
      List(RuleAmountGainLossError.copy(paths = Some(Seq(s"/singlePropertyDisposals/$arrayIndex"))))
    } else {
      NoValidationErrors
    }
  }

  private def validateGainGreaterThanLoss(singlePropertyDisposals: SinglePropertyDisposals, arrayIndex: Int): List[MtdError] = {
    if (singlePropertyDisposals.amountOfNetGain.isDefined &&
      (singlePropertyDisposals.lossesFromPreviousYear > singlePropertyDisposals.amountOfNetGain ||
        singlePropertyDisposals.lossesFromThisYear > singlePropertyDisposals.amountOfNetGain)) {
      List(RuleLossesGreaterThanGainError.copy(paths = Some(Seq(s"singlePropertyDisposals/$arrayIndex"))))
    } else {
      NoValidationErrors
    }
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
        }).getOrElse(NoValidationErrors).toList
      )
    ))
  }

  private def validateMultiplePropertyDisposals(multiplePropertyDisposals: MultiplePropertyDisposals, arrayIndex: Int): List[MtdError] = {

    List(
      SubmissionIdValidation.validate(
        multiplePropertyDisposals.submissionId, SubmissionIdFormatError.copy(paths = Some(Seq(s"multiplePropertyDisposals/$arrayIndex/submissionId")))),
      DecimalValueValidation.validateOptional(
        amount = multiplePropertyDisposals.amountOfNetGain,
        path = s"/multiplePropertyDisposals/$arrayIndex/amountOfNetGain"
      ),
      DecimalValueValidation.validateOptional(
        amount = multiplePropertyDisposals.amountOfNetLoss,
        path = s"/multiplePropertyDisposals/$arrayIndex/amountOfNetLoss"
      )
    ).flatten
  }

  private def validateSinglePropertyDisposals(singlePropertyDisposals: SinglePropertyDisposals, arrayIndex: Int): List[MtdError] = {

    List(
      SubmissionIdValidation.validate(
        singlePropertyDisposals.submissionId, SubmissionIdFormatError.copy(paths = Some(Seq(s"singlePropertyDisposals/$arrayIndex/submissionId")))),
      DateFormatValidation.validate(
        singlePropertyDisposals.completionDate, DateFormatError.copy(paths = Some(Seq(s"singlePropertyDisposals/$arrayIndex/completionDate")))),
      DecimalValueValidation.validate(
        amount = singlePropertyDisposals.disposalProceeds,
        path = s"/singlePropertyDisposals/$arrayIndex/disposalProceeds"
      ),
      DateFormatValidation.validate(singlePropertyDisposals.acquisitionDate, DateFormatError),
      DecimalValueValidation.validate(
        amount = singlePropertyDisposals.acquisitionAmount,
        path = s"/singlePropertyDisposals/$arrayIndex/acquisitionAmount"
      ),
      DecimalValueValidation.validate(
        amount = singlePropertyDisposals.improvementCosts,
        path = s"/singlePropertyDisposals/$arrayIndex/improvementCosts"
      ),
      DecimalValueValidation.validate(
        amount = singlePropertyDisposals.additionalCosts,
        path = s"/singlePropertyDisposals/$arrayIndex/additionalCosts"
      ),
      DecimalValueValidation.validate(
        amount = singlePropertyDisposals.prfAmount,
        path = s"/singlePropertyDisposals/$arrayIndex/prfAmount"
      ),
      DecimalValueValidation.validate(
        amount = singlePropertyDisposals.otherReliefAmount,
        path = s"/singlePropertyDisposals/$arrayIndex/otherReliefAmount"
      ),
      DecimalValueValidation.validateOptional(
        amount = singlePropertyDisposals.lossesFromThisYear,
        path = s"/singlePropertyDisposals/$arrayIndex/lossesFromThisYear"
      ),
      DecimalValueValidation.validateOptional(
        amount = singlePropertyDisposals.lossesFromPreviousYear,
        path = s"/singlePropertyDisposals/$arrayIndex/lossesFromPreviousYear"
      ),
      DecimalValueValidation.validateOptional(
        amount = singlePropertyDisposals.amountOfNetGain,
        path = s"/singlePropertyDisposals/$arrayIndex/amountOfNetGain"
      ),
      DecimalValueValidation.validateOptional(
        amount = singlePropertyDisposals.amountOfNetLoss,
        path = s"/singlePropertyDisposals/$arrayIndex/amountOfNetLoss"
      )
    ).flatten
  }

}
