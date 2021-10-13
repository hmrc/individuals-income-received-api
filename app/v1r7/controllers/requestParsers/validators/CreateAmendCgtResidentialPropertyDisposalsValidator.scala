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

package v1r7.controllers.requestParsers.validators

import config.AppConfig
import utils.CurrentDateTime
import v1r7.controllers.requestParsers.validators.validations._
import v1r7.models.errors._
import v1r7.models.request.createAmendCgtResidentialPropertyDisposals._

import javax.inject.{ Inject, Singleton }

@Singleton
class CreateAmendCgtResidentialPropertyDisposalsValidator @Inject()(implicit currentDateTime: CurrentDateTime, appConfig: AppConfig)
    extends Validator[CreateAmendCgtResidentialPropertyDisposalsRawData] with DisposalDateErrorMessages {

  private val validationSet = List(
    parameterFormatValidation,
    parameterRuleValidation,
    bodyFormatValidator,
    bodyValueValidator,
    bodyRuleValidator,
    lossOrGainsValidator
  )

  override def validate(data: CreateAmendCgtResidentialPropertyDisposalsRawData): List[MtdError] = {
    run(validationSet, data).distinct
  }

  private def parameterFormatValidation: CreateAmendCgtResidentialPropertyDisposalsRawData => List[List[MtdError]] = data => {
    List(
      NinoValidation.validate(data.nino),
      TaxYearValidation.validate(data.taxYear)
    )
  }

  private def parameterRuleValidation: CreateAmendCgtResidentialPropertyDisposalsRawData => List[List[MtdError]] = data => {
    List(
      TaxYearNotSupportedValidation.validate(data.taxYear, appConfig.minimumPermittedTaxYear)
    )
  }

  private def bodyFormatValidator: CreateAmendCgtResidentialPropertyDisposalsRawData => List[List[MtdError]] = { data =>
    val jsonFormatError = List(
      JsonFormatValidation.validate[CreateAmendCgtResidentialPropertyDisposalsRequestBody](data.body.json)
    )

    val requestBodyObj = data.body.json.asOpt[CreateAmendCgtResidentialPropertyDisposalsRequestBody]
    val emptyValidation: List[List[MtdError]] = List(
      requestBodyObj
        .map { body =>
          val emptyDisposalsError: List[String] = if (body.disposals.isEmpty) List("/disposals") else List()
          if (emptyDisposalsError.isEmpty) NoValidationErrors else List(RuleIncorrectOrEmptyBodyError.copy(paths = Some(emptyDisposalsError)))
        }
        .getOrElse(NoValidationErrors))

    jsonFormatError ++ emptyValidation
  }

  private def bodyValueValidator: CreateAmendCgtResidentialPropertyDisposalsRawData => List[List[MtdError]] = { data =>
    val requestBodyData = data.body.json.as[CreateAmendCgtResidentialPropertyDisposalsRequestBody]

    List(
      Validator.flattenErrors(
        List(
          requestBodyData.disposals.zipWithIndex.flatMap {
            case (disposal, index) => validateDisposalFormat(disposal, index)
          }
        ).map(_.toList)
      )
    )
  }

  private def validateDisposalFormat(disposal: Disposal, index: Int): List[MtdError] = {
    List(
      DecimalValueValidation.validate(
        amount = disposal.disposalProceeds,
        path = s"/disposals/$index/disposalProceeds"
      ),
      DecimalValueValidation.validate(
        amount = disposal.acquisitionAmount,
        path = s"/disposals/$index/acquisitionAmount"
      ),
      DecimalValueValidation.validateOptional(
        amount = disposal.improvementCosts,
        path = s"/disposals/$index/improvementCosts"
      ),
      DecimalValueValidation.validateOptional(
        amount = disposal.additionalCosts,
        path = s"/disposals/$index/additionalCosts"
      ),
      DecimalValueValidation.validateOptional(
        amount = disposal.prfAmount,
        path = s"/disposals/$index/prfAmount"
      ),
      DecimalValueValidation.validateOptional(
        amount = disposal.otherReliefAmount,
        path = s"/disposals/$index/otherReliefAmount"
      ),
      DecimalValueValidation.validateOptional(
        amount = disposal.lossesFromThisYear,
        path = s"/disposals/$index/lossesFromThisYear"
      ),
      DecimalValueValidation.validateOptional(
        amount = disposal.lossesFromPreviousYear,
        path = s"/disposals/$index/lossesFromPreviousYear"
      ),
      DecimalValueValidation.validateOptional(
        amount = disposal.amountOfNetGain,
        path = s"/disposals/$index/amountOfNetGain"
      ),
      DecimalValueValidation.validateOptional(
        amount = disposal.amountOfNetLoss,
        path = s"/disposals/$index/amountOfNetLoss"
      ),
      DateFormatValidation.validateWithPath(
        date = disposal.disposalDate,
        path = s"/disposals/$index/disposalDate"
      ),
      DateFormatValidation.validateWithPath(
        date = disposal.completionDate,
        path = s"/disposals/$index/completionDate"
      ),
      DateFormatValidation.validateWithPath(
        date = disposal.acquisitionDate,
        path = s"/disposals/$index/acquisitionDate"
      ),
      CustomerRefValidation.validateOptional(
        customerRef = disposal.customerReference,
        path = Some(s"/disposals/$index")
      )
    ).flatten
  }

  private def bodyRuleValidator: CreateAmendCgtResidentialPropertyDisposalsRawData => List[List[MtdError]] = { data =>
    val requestBodyData = data.body.json.as[CreateAmendCgtResidentialPropertyDisposalsRequestBody]

    List(
      Validator.flattenErrors(
        List(
          requestBodyData.disposals.zipWithIndex.flatMap {
            case (disposal, index) => validateDisposalRule(disposal, index, data.taxYear)
          }
        ).map(_.toList)
      )
    )
  }

  private def validateDisposalRule(disposal: Disposal, index: Int, taxYear: String): List[MtdError] = {
    List(
      DateAfterDateValidation.validate(
        dateWhichShouldBeEarlier = disposal.disposalDate,
        dateWhichShouldBeLater = disposal.completionDate,
        path = s"/disposals/$index",
        error = RuleCompletionDateBeforeDisposalDateError
      ),
      DateAfterDateValidation.validate(
        dateWhichShouldBeEarlier = disposal.acquisitionDate,
        dateWhichShouldBeLater = disposal.disposalDate,
        path = s"/disposals/$index",
        error = RuleAcquisitionDateAfterDisposalDateError
      ),
      CompletionDateValidation.validate(
        date = disposal.completionDate,
        path = s"/disposals/$index",
        taxYear = taxYear
      ),
      DisposalDateValidation.validate(
        date = disposal.disposalDate,
        taxYear = taxYear,
        path = s"/disposals/$index",
        validateToday = false,
        errorMessage = IN_YEAR
      )
    ).flatten
  }

  private def lossOrGainsValidator: CreateAmendCgtResidentialPropertyDisposalsRawData => List[List[MtdError]] = { data =>
    val requestBodyData = data.body.json.as[CreateAmendCgtResidentialPropertyDisposalsRequestBody]

    requestBodyData.disposals.zipWithIndex.map {
      case (disposal, index) => validateLossOrGains(disposal, index)
    }.toList
  }

  private def validateLossOrGains(disposal: Disposal, index: Int): List[MtdError] = {
    List(
      Validator.flattenErrors(
        List(
          if (disposal.gainAndLossAreBothSupplied) List(RuleGainLossError.copy(paths = Some(Seq(s"/disposals/$index")))) else NoValidationErrors,
          ValueGreaterThanValueValidation.validateOptional(
            valueWhichShouldBeLowerOrEqualO = disposal.lossesFromThisYear,
            valueWhichShouldBeHigherOrEqualO = disposal.amountOfNetGain,
            path = s"/disposals/$index/lossesFromThisYear"
          ),
          ValueGreaterThanValueValidation.validateOptional(
            valueWhichShouldBeLowerOrEqualO = disposal.lossesFromPreviousYear,
            valueWhichShouldBeHigherOrEqualO = disposal.amountOfNetGain,
            path = s"/disposals/$index/lossesFromPreviousYear"
          )
        )
      )
    ).flatten
  }
}
