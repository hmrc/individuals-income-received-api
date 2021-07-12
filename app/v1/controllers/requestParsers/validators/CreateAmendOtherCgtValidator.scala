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
import v1.models.request.createAmendOtherCgt._

import javax.inject.{ Inject, Singleton }

@Singleton
class CreateAmendOtherCgtValidator @Inject()(implicit appConfig: AppConfig)
    extends Validator[CreateAmendOtherCgtRawData]
    with ValueFormatErrorMessages {

  private val validationSet = List(parameterFormatValidation, parameterRuleValidation, jsonFormatValidation, bodyFormatValidation, bodyRuleValidation, oneMinimumRequiredFieldValidator)

  override def validate(data: CreateAmendOtherCgtRawData): List[MtdError] = {
    run(validationSet, data).distinct
  }

  private def parameterFormatValidation: CreateAmendOtherCgtRawData => List[List[MtdError]] = (data: CreateAmendOtherCgtRawData) => {
    List(
      NinoValidation.validate(data.nino),
      TaxYearValidation.validate(data.taxYear)
    )
  }

  private def parameterRuleValidation: CreateAmendOtherCgtRawData => List[List[MtdError]] = { data =>
    List(
      TaxYearNotSupportedValidation.validate(data.taxYear, appConfig.minimumPermittedTaxYear)
    )
  }

  private def jsonFormatValidation: CreateAmendOtherCgtRawData => List[List[MtdError]] = { data =>
    val standardValidation = List(
      JsonFormatValidation.validate[CreateAmendOtherCgtRequestBody](data.body.json),
    )

    val requestBodyData = data.body.json.asOpt[CreateAmendOtherCgtRequestBody]
    val emptyValidation: List[List[MtdError]] = List(requestBodyData.map {
      body =>
      val disposalsMissingFields = if(body.disposals.exists(_.isEmpty)) List("/disposals") else List()
      val nonStandardGainsMissingFields = if(body.nonStandardGains.contains(NonStandardGains.empty)) List("/nonStandardGains") else List()
      val lossesMissingFields = if(body.losses.contains(Losses.empty)) List("/losses") else List()
      val allMissingFields = disposalsMissingFields ++ nonStandardGainsMissingFields ++ lossesMissingFields
      if(allMissingFields.isEmpty) NoValidationErrors else List(RuleIncorrectOrEmptyBodyError.copy(paths = Some(allMissingFields)))
    }.getOrElse(NoValidationErrors))

    standardValidation ++ emptyValidation
  }

  private def oneMinimumRequiredFieldValidator: CreateAmendOtherCgtRawData => List[List[MtdError]] = { data =>
    val requestBodyData = data.body.json.as[CreateAmendOtherCgtRequestBody]

    List(Validator.flattenErrors(
      List(
        requestBodyData.nonStandardGains.map {
          case data => oneOfThreeGainsSuppliedValidation(data)
        }.getOrElse(NoValidationErrors)
      )
    ))
  }

  private def oneOfThreeGainsSuppliedValidation(nonStandardGains: NonStandardGains): List[MtdError] = {

    if (nonStandardGains.isThreeFieldsEmpty) {
      List(RuleIncorrectOrEmptyBodyError.copy(paths = Some(Seq("/nonStandardGains"))))
    }
    else NoValidationErrors
  }

  private def bodyFormatValidation: CreateAmendOtherCgtRawData => List[List[MtdError]] = { data =>
    val requestBodyData = data.body.json.as[CreateAmendOtherCgtRequestBody]

    List(
      Validator.flattenErrors(
        List(
          requestBodyData.disposals
            .map(_.toList)
            .map(_.zipWithIndex.flatMap {
              case (disposal, index) => validateDisposalFormat(disposal, index)
            })
            .getOrElse(NoValidationErrors),
          requestBodyData.nonStandardGains
            .map(validateNonStandardGainsFormat)
            .getOrElse(NoValidationErrors),
          requestBodyData.losses
            .map(validateLossesFormat)
            .getOrElse(NoValidationErrors),
          DecimalValueValidation.validateOptional(
            amount = requestBodyData.adjustments,
            minValue = -99999999999.99,
            path = "/adjustments",
            message = BIG_DECIMAL_MINIMUM_INCLUSIVE
          )
        )
      )
    )
  }

  private def bodyRuleValidation: CreateAmendOtherCgtRawData => List[List[MtdError]] = { data =>
    val requestBodyData = data.body.json.as[CreateAmendOtherCgtRequestBody]

    List(
      Validator.flattenErrors(
        List(
          requestBodyData.disposals
            .map(_.toList)
            .map(_.zipWithIndex.flatMap {
              case (disposal, index) => validateDisposalRules(disposal, data.taxYear, index)
            })
            .getOrElse(NoValidationErrors)
        )
      )
    )
  }

  private def validateDisposalFormat(disposal: Disposal, arrayIndex: Int): List[MtdError] = {
    List(
      DecimalValueValidation.validate(
        amount = disposal.disposalProceeds,
        path = s"/disposals/$arrayIndex/disposalProceeds"
      ),
      DecimalValueValidation.validate(
        amount = disposal.allowableCosts,
        path = s"/disposals/$arrayIndex/allowableCosts"
      ),
      DecimalValueValidation.validateOptional(
        amount = disposal.gain,
        path = s"/disposals/$arrayIndex/gain"
      ),
      DecimalValueValidation.validateOptional(
        amount = disposal.loss,
        path = s"/disposals/$arrayIndex/loss"
      ),
      DecimalValueValidation.validateOptional(
        amount = disposal.gainAfterRelief,
        path = s"/disposals/$arrayIndex/gainAfterRelief"
      ),
      DecimalValueValidation.validateOptional(
        amount = disposal.lossAfterRelief,
        path = s"/disposals/$arrayIndex/lossAfterRelief"
      ),
      DecimalValueValidation.validateOptional(
        amount = disposal.rttTaxPaid,
        path = s"/disposals/$arrayIndex/rttTaxPaid"
      ),
      DateFormatValidation.validateWithPath(
        date = disposal.acquisitionDate,
        path = s"/disposals/$arrayIndex/acquisitionDate"
      ),
      DateFormatValidation.validateWithPath(
        date = disposal.disposalDate,
        path = s"/disposals/$arrayIndex/disposalDate"
      ),
      AssetDescriptionValidation.validate(
        description = disposal.assetDescription,
        path = s"/disposals/$arrayIndex/assetDescription"
      ),
      AssetTypeValidation.validate(
        assetType = disposal.assetType,
        path = s"/disposals/$arrayIndex/assetType"
      ),
      ClaimOrElectionCodesValidation.validateOptional(
        claimOrElectionCodesO = disposal.claimOrElectionCodes,
        index = arrayIndex
      )
    ).flatten
  }

  private def validateNonStandardGainsFormat(nonStandardGains: NonStandardGains): List[MtdError] = {
    List(
      DecimalValueValidation.validateOptional(
        amount = nonStandardGains.carriedInterestGain,
        path = "/nonStandardGains/carriedInterestGain"
      ),
      DecimalValueValidation.validateOptional(
        amount = nonStandardGains.carriedInterestRttTaxPaid,
        path = "/nonStandardGains/carriedInterestRttTaxPaid"
      ),
      DecimalValueValidation.validateOptional(
        amount = nonStandardGains.attributedGains,
        path = "/nonStandardGains/attributedGains"
      ),
      DecimalValueValidation.validateOptional(
        amount = nonStandardGains.attributedGainsRttTaxPaid,
        path = "/nonStandardGains/attributedGainsRttTaxPaid"
      ),
      DecimalValueValidation.validateOptional(
        amount = nonStandardGains.otherGains,
        path = "/nonStandardGains/otherGains"
      ),
      DecimalValueValidation.validateOptional(
        amount = nonStandardGains.otherGainsRttTaxPaid,
        path = "/nonStandardGains/otherGainsRttTaxPaid"
      )
    ).flatten
  }

  private def validateLossesFormat(losses: Losses): List[MtdError] = {
    List(
      DecimalValueValidation.validateOptional(
        amount = losses.broughtForwardLossesUsedInCurrentYear,
        path = "/losses/broughtForwardLossesUsedInCurrentYear"
      ),
      DecimalValueValidation.validateOptional(
        amount = losses.setAgainstInYearGains,
        path = "/losses/setAgainstInYearGains"
      ),
      DecimalValueValidation.validateOptional(
        amount = losses.setAgainstInYearGeneralIncome,
        path = "/losses/setAgainstInYearGeneralIncome"
      ),
      DecimalValueValidation.validateOptional(
        amount = losses.setAgainstEarlierYear,
        path = "/losses/setAgainstEarlierYear"
      ),
    ).flatten
  }

  private def validateDisposalRules(disposal: Disposal, taxYear: String, arrayIndex: Int): List[MtdError] = {
    List(
      GainLossValidation.validate(gain = disposal.gain, loss = disposal.loss, error = RuleGainLossError, path = s"/disposals/$arrayIndex"),
      GainLossValidation.validate(
        gain = disposal.gainAfterRelief,
        loss = disposal.lossAfterRelief,
        error = RuleGainAfterReliefLossAfterReliefError,
        path = s"/disposals/$arrayIndex"
      ),
      DisposalDateValidation.validate(
        date = disposal.disposalDate,
        taxYear = taxYear,
        path = s"/disposals/$arrayIndex"
      ),
      AcquisitionDateValidation.validate(
        disposalDate = disposal.disposalDate,
        acquisitionDate = disposal.acquisitionDate,
        path = s"/disposals/$arrayIndex"
      )
    ).flatten
  }
}
