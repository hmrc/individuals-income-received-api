/*
 * Copyright 2020 HM Revenue & Customs
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

import v1.controllers.requestParsers.validators.validations._
import v1.models.errors.{MtdError, RuleIncorrectOrEmptyBodyError}
import v1.models.request.insurancePolicies.amend._

class AmendInsurancePoliciesValidator extends Validator[AmendRawData] with ValueFormatErrorMessages {

  private val validationSet = List(parameterFormatValidation, bodyFormatValidator, bodyValueValidator)

  override def validate(data: AmendRawData): List[MtdError] = {
    run(validationSet, data).distinct
  }

  private def parameterFormatValidation: AmendRawData => List[List[MtdError]] = (data: AmendRawData) => {
    List(
      NinoValidation.validate(data.nino),
      TaxYearValidation.validate(data.taxYear)
    )
  }

  private def bodyFormatValidator: AmendRawData => List[List[MtdError]] = { data =>
    List(
      JsonFormatValidation.validate[AmendRequestBody](data.body.json, RuleIncorrectOrEmptyBodyError)
    )
  }

  private def bodyValueValidator: AmendRawData => List[List[MtdError]] = { data =>

    val requestBodyData = data.body.json.as[AmendRequestBody]

    List(flattenErrors(
      List(
        requestBodyData.lifeInsurance.map(_.zipWithIndex.flatMap {
          case (data, index) => validateLifeInsurance(data, index)
        }).getOrElse(NoValidationErrors).toList,
        requestBodyData.capitalRedemption.map(_.zipWithIndex.flatMap {
          case (data, index) => validateCapitalRedemption(data, index)
        }).getOrElse(NoValidationErrors).toList,
        requestBodyData.lifeAnnuity.map(_.zipWithIndex.flatMap {
          case (data, index) => validateLifeAnnuity(data, index)
        }).getOrElse(NoValidationErrors).toList,
        requestBodyData.voidedIsa.map(_.zipWithIndex.flatMap {
          case (data, index) => validateVoidedIsa(data, index)
        }).getOrElse(NoValidationErrors).toList,
        requestBodyData.foreign.map(_.zipWithIndex.flatMap {
          case (data, index) => validateForeign(data, index)
        }).getOrElse(NoValidationErrors).toList
      )
    ))
  }

  private def validateLifeInsurance(lifeInsurance: LifeInsurance, arrayIndex: Int): List[MtdError] = {
    List(
      CustomerRefValidation.validate(lifeInsurance.customerReference).map(
        _.copy(paths = Some(Seq(s"/lifeInsurance/$arrayIndex/customerReference")))
      ),
      EventValidation.validateOptional(
        event = lifeInsurance.event,
        path = s"/lifeInsurance/$arrayIndex/event"
      ),
      DecimalValueValidation.validateOptional(
        amount = lifeInsurance.gainAmount,
        minValue = 0.01,
        path = s"/lifeInsurance/$arrayIndex/gainAmount",
        message = DECIMAL_MINIMUM_INCLUSIVE
      ),
      DecimalValueValidation.validateOptional(
        amount = lifeInsurance.taxPaid,
        path = s"/lifeInsurance/$arrayIndex/taxPaid"
      ),
      IntegerValueValidation.validateOptional(
        field = lifeInsurance.yearsHeld,
        path = s"/lifeInsurance/$arrayIndex/yearsHeld"
      ),
      IntegerValueValidation.validateOptional(
        field = lifeInsurance.yearsHeldSinceLastGain,
        path = s"/lifeInsurance/$arrayIndex/yearsHeldSinceLastGain"
      ),
      DecimalValueValidation.validateOptional(
        amount = lifeInsurance.deficiencyRelief,
        minValue = 0.01,
        path = s"/lifeInsurance/$arrayIndex/deficiencyRelief",
        message = DECIMAL_MINIMUM_INCLUSIVE
      )
    ).flatten
  }

  private def validateCapitalRedemption(capitalRedemption: CapitalRedemption, arrayIndex: Int): List[MtdError] = {
    List(
      CustomerRefValidation.validate(capitalRedemption.customerReference).map(
        _.copy(paths = Some(Seq(s"/capitalRedemption/$arrayIndex/customerReference")))
      ),
      EventValidation.validateOptional(
        event = capitalRedemption.event,
        path = s"/capitalRedemption/$arrayIndex/event"
      ),
      DecimalValueValidation.validateOptional(
        amount = capitalRedemption.gainAmount,
        minValue = 0.01,
        path = s"/capitalRedemption/$arrayIndex/gainAmount",
        message = DECIMAL_MINIMUM_INCLUSIVE
      ),
      DecimalValueValidation.validateOptional(
        amount = capitalRedemption.taxPaid,
        path = s"/capitalRedemption/$arrayIndex/taxPaid"
      ),
      IntegerValueValidation.validateOptional(
        field = capitalRedemption.yearsHeld,
        path = s"/capitalRedemption/$arrayIndex/yearsHeld"
      ),
      IntegerValueValidation.validateOptional(
        field = capitalRedemption.yearsHeldSinceLastGain,
        path = s"/capitalRedemption/$arrayIndex/yearsHeldSinceLastGain"
      ),
      DecimalValueValidation.validateOptional(
        amount = capitalRedemption.deficiencyRelief,
        minValue = 0.01,
        path = s"/capitalRedemption/$arrayIndex/deficiencyRelief",
        message = DECIMAL_MINIMUM_INCLUSIVE
      )
    ).flatten
  }

  private def validateLifeAnnuity(lifeAnnuity: LifeAnnuity, arrayIndex: Int): List[MtdError] = {
    List(
      CustomerRefValidation.validate(lifeAnnuity.customerReference).map(
        _.copy(paths = Some(Seq(s"/lifeAnnuity/$arrayIndex/customerReference")))
      ),
      EventValidation.validateOptional(
        event = lifeAnnuity.event,
        path = s"/lifeAnnuity/$arrayIndex/event"
      ),
      DecimalValueValidation.validateOptional(
        amount = lifeAnnuity.gainAmount,
        minValue = 0.01,
        path = s"/lifeAnnuity/$arrayIndex/gainAmount",
        message = DECIMAL_MINIMUM_INCLUSIVE
      ),
      DecimalValueValidation.validateOptional(
        amount = lifeAnnuity.taxPaid,
        path = s"/lifeAnnuity/$arrayIndex/taxPaid"
      ),
      IntegerValueValidation.validateOptional(
        field = lifeAnnuity.yearsHeld,
        path = s"/lifeAnnuity/$arrayIndex/yearsHeld"
      ),
      IntegerValueValidation.validateOptional(
        field = lifeAnnuity.yearsHeldSinceLastGain,
        path = s"/lifeAnnuity/$arrayIndex/yearsHeldSinceLastGain"
      ),
      DecimalValueValidation.validateOptional(
        amount = lifeAnnuity.deficiencyRelief,
        minValue = 0.01,
        path = s"/lifeAnnuity/$arrayIndex/deficiencyRelief",
        message = DECIMAL_MINIMUM_INCLUSIVE
      )
    ).flatten
  }

  private def validateVoidedIsa(voidedIsa: VoidedIsa, arrayIndex: Int): List[MtdError] = {
    List(
      CustomerRefValidation.validate(voidedIsa.customerReference).map(
        _.copy(paths = Some(Seq(s"/voidedIsa/$arrayIndex/customerReference")))
      ),
      EventValidation.validateOptional(
        event = voidedIsa.event,
        path = s"/voidedIsa/$arrayIndex/event"
      ),
      DecimalValueValidation.validateOptional(
        amount = voidedIsa.gainAmount,
        path = s"/voidedIsa/$arrayIndex/gainAmount"
      ),
      DecimalValueValidation.validateOptional(
        amount = voidedIsa.taxPaid,
        path = s"/voidedIsa/$arrayIndex/taxPaid"
      ),
      IntegerValueValidation.validateOptional(
        field = voidedIsa.yearsHeld,
        path = s"/voidedIsa/$arrayIndex/yearsHeld"
      ),
      IntegerValueValidation.validateOptional(
        field = voidedIsa.yearsHeldSinceLastGain,
        path = s"/voidedIsa/$arrayIndex/yearsHeldSinceLastGain"
      )
    ).flatten
  }

  private def validateForeign(foreign: Foreign, arrayIndex: Int): List[MtdError] = {
    List(
      CustomerRefValidation.validate(foreign.customerReference).map(
        _.copy(paths = Some(Seq(s"/foreign/$arrayIndex/customerReference")))
      ),
      DecimalValueValidation.validateOptional(
        amount = foreign.gainAmount,
        minValue = 0.01,
        path = s"/foreign/$arrayIndex/gainAmount",
        message = DECIMAL_MINIMUM_INCLUSIVE
      ),
      DecimalValueValidation.validateOptional(
        amount = foreign.taxPaid,
        path = s"/foreign/$arrayIndex/taxPaid"
      ),
      IntegerValueValidation.validateOptional(
        field = foreign.yearsHeld,
        path = s"/foreign/$arrayIndex/yearsHeld"
      )
    ).flatten
  }

  private def flattenErrors(errors: List[List[MtdError]]): List[MtdError] = {
    errors.flatten.groupBy(_.message).map {case (_, errors) =>

      val baseError = errors.head.copy(paths = Some(Seq.empty[String]))

      errors.fold(baseError)(
        (error1, error2) =>
          error1.copy(paths = Some(error1.paths.getOrElse(Seq.empty[String]) ++ error2.paths.getOrElse(Seq.empty[String])))
      )
    }.toList
  }
}
