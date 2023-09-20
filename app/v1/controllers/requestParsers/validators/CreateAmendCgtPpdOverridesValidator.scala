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
import api.models.errors._
import config.AppConfig
import v1.models.request.createAmendCgtPpdOverrides._
import v1.controllers.requestParsers.validators.validations.DateFormatValidation

import javax.inject.{Inject, Singleton}

@Singleton
class CreateAmendCgtPpdOverridesValidator @Inject() (implicit appConfig: AppConfig)
    extends Validator[CreateAmendCgtPpdOverridesRawData]
    with ValueFormatErrorMessages {

  private val validationSet = List(parameterFormatValidation, parameterRuleValidation, bodyFormatValidator, bodyValueValidator, rulesValidator)

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
      TaxYearNotSupportedValidation.validate(data.taxYear, appConfig.minimumPermittedTaxYear)
    )
  }

  private def bodyFormatValidator: CreateAmendCgtPpdOverridesRawData => List[List[MtdError]] = { data =>
    val jsonFormatError = List(
      JsonFormatValidation.validate[CreateAmendCgtPpdOverridesRequestBody](data.body.json)
    )

    val requestBodyObj = data.body.json.asOpt[CreateAmendCgtPpdOverridesRequestBody]
    val emptyValidation: List[List[MtdError]] = List(
      requestBodyObj
        .map { body =>
          val emptyMultiplePropertyDisposalsError =
            if (body.multiplePropertyDisposals.exists(_.isEmpty)) List("/multiplePropertyDisposals") else List()
          val emptySinglePropertyDisposalsError = if (body.singlePropertyDisposals.exists(_.isEmpty)) List("/singlePropertyDisposals") else List()

          val allMissingFields = emptyMultiplePropertyDisposalsError ++ emptySinglePropertyDisposalsError
          if (allMissingFields.isEmpty) NoValidationErrors else List(RuleIncorrectOrEmptyBodyError.copy(paths = Some(allMissingFields)))
        }
        .getOrElse(NoValidationErrors))

    jsonFormatError ++ emptyValidation
  }

  private def rulesValidator: CreateAmendCgtPpdOverridesRawData => List[List[MtdError]] = (data: CreateAmendCgtPpdOverridesRawData) => {
    val requestBody: CreateAmendCgtPpdOverridesRequestBody = data.body.json.as[CreateAmendCgtPpdOverridesRequestBody]

    val singleDisposalsIndexed   = requestBody.singlePropertyDisposals.toList.flatten.zipWithIndex
    val multipleDisposalsIndexed = requestBody.multiplePropertyDisposals.toList.flatten.zipWithIndex

    List(
      flattenErrors(
        List(
          multipleDisposalsIndexed.flatMap { case (data, index) =>
            validateBothSuppliedMultipleDisposals(data, index)
          },
          singleDisposalsIndexed.flatMap { case (data, index) =>
            validateBothSuppliedSingleDisposals(data, index)
          }
        )
      ))
  }

  private def validateBothSuppliedMultipleDisposals(multiplePropertyDisposals: MultiplePropertyDisposals, arrayIndex: Int): List[MtdError] = {

    if (multiplePropertyDisposals.isBothSupplied) {
      List(RuleAmountGainLossError.copy(paths = Some(Seq(s"/multiplePropertyDisposals/$arrayIndex"))))
    } else if (multiplePropertyDisposals.isNetAmountEmpty) {
      List(RuleAmountGainLossError.copy(paths = Some(Seq(s"/multiplePropertyDisposals/$arrayIndex"))))
    } else {
      NoValidationErrors
    }
  }

  private def validateBothSuppliedSingleDisposals(singlePropertyDisposals: SinglePropertyDisposals, arrayIndex: Int): List[MtdError] = {

    if (singlePropertyDisposals.isBothSupplied) {
      List(RuleAmountGainLossError.copy(paths = Some(Seq(s"/singlePropertyDisposals/$arrayIndex"))))
    } else if (singlePropertyDisposals.isBothEmpty) {
      List(RuleAmountGainLossError.copy(paths = Some(Seq(s"/singlePropertyDisposals/$arrayIndex"))))
    } else {
      NoValidationErrors
    }
  }

  private def bodyValueValidator: CreateAmendCgtPpdOverridesRawData => List[List[MtdError]] = { data =>
    val requestBodyData = data.body.json.as[CreateAmendCgtPpdOverridesRequestBody]

    List(
      flattenErrors(
        List(
          requestBodyData.multiplePropertyDisposals
            .map(_.zipWithIndex.flatMap { case (data, index) =>
              validateMultiplePropertyDisposals(data, index)
            })
            .getOrElse(NoValidationErrors)
            .toList,
          requestBodyData.singlePropertyDisposals
            .map(_.zipWithIndex.flatMap { case (data, index) =>
              validateSinglePropertyDisposals(data, index)
            })
            .getOrElse(NoValidationErrors)
            .toList
        )
      ))
  }

  private def validateMultiplePropertyDisposals(multiplePropertyDisposals: MultiplePropertyDisposals, arrayIndex: Int): List[MtdError] = {

    List(
      PpdSubmissionIdValidation.validate(
        multiplePropertyDisposals.ppdSubmissionId,
        PpdSubmissionIdFormatError.copy(paths = Some(Seq(s"/multiplePropertyDisposals/$arrayIndex/ppdSubmissionId")))),
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
      PpdSubmissionIdValidation.validate(
        singlePropertyDisposals.ppdSubmissionId,
        PpdSubmissionIdFormatError.copy(paths = Some(Seq(s"/singlePropertyDisposals/$arrayIndex/ppdSubmissionId")))),
      DateFormatValidation.validateWithPath(singlePropertyDisposals.completionDate, path = s"/singlePropertyDisposals/$arrayIndex/completionDate"),
      DecimalValueValidation.validate(
        amount = singlePropertyDisposals.disposalProceeds,
        path = s"/singlePropertyDisposals/$arrayIndex/disposalProceeds"
      ),
      DateFormatValidation.validateOptionalWithPath(
        singlePropertyDisposals.acquisitionDate,
        path = s"/singlePropertyDisposals/$arrayIndex/acquisitionDate"),
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
