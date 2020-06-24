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
import v1.models.request.amendInsurancePolicies.{AmendCommonInsurancePoliciesItem, AmendForeignPoliciesItem, AmendInsurancePoliciesRawData, AmendInsurancePoliciesRequestBody, AmendVoidedIsaPoliciesItem}

class AmendInsurancePoliciesValidator extends Validator[AmendInsurancePoliciesRawData] with ValueFormatErrorMessages {

  private val validationSet = List(parameterFormatValidation, bodyFormatValidator, bodyValueValidator)

  override def validate(data: AmendInsurancePoliciesRawData): List[MtdError] = {
    run(validationSet, data).distinct
  }

  private def parameterFormatValidation: AmendInsurancePoliciesRawData => List[List[MtdError]] = (data: AmendInsurancePoliciesRawData) => {
    List(
      NinoValidation.validate(data.nino),
      TaxYearValidation.validate(data.taxYear)
    )
  }

  private def bodyFormatValidator: AmendInsurancePoliciesRawData => List[List[MtdError]] = { data =>
    List(
      JsonFormatValidation.validate[AmendInsurancePoliciesRequestBody](data.body.json, RuleIncorrectOrEmptyBodyError)
    )
  }

  private def bodyValueValidator: AmendInsurancePoliciesRawData => List[List[MtdError]] = { data =>

    val requestBodyData = data.body.json.as[AmendInsurancePoliciesRequestBody]

    List(flattenErrors(
      List(
        requestBodyData.lifeInsurance.map(_.zipWithIndex.flatMap {
          case (data, index) => validateCommonItem(data, itemName = "lifeInsurance", arrayIndex = index)
        }).getOrElse(NoValidationErrors).toList,
        requestBodyData.capitalRedemption.map(_.zipWithIndex.flatMap {
          case (data, index) => validateCommonItem(data, itemName = "capitalRedemption", arrayIndex = index)
        }).getOrElse(NoValidationErrors).toList,
        requestBodyData.lifeAnnuity.map(_.zipWithIndex.flatMap {
          case (data, index) => validateCommonItem(data, itemName = "lifeAnnuity", arrayIndex = index)
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

  private def validateCommonItem(commonItem: AmendCommonInsurancePoliciesItem, itemName: String, arrayIndex: Int): List[MtdError] = {
    List(
      CustomerRefInsuranceValidation.validateOptional(commonItem.customerReference).map(
        _.copy(paths = Some(Seq(s"/$itemName/$arrayIndex/customerReference")))
      ),
      EventValidation.validateOptional(
        event = commonItem.event,
        path = s"/$itemName/$arrayIndex/event"
      ),
      DecimalValueValidation.validate(
        amount = commonItem.gainAmount,
        minValue = 0.01,
        path = s"/$itemName/$arrayIndex/gainAmount",
        message = DECIMAL_MINIMUM_INCLUSIVE
      ),
      IntegerValueValidation.validateOptional(
        field = commonItem.yearsHeld,
        path = s"/$itemName/$arrayIndex/yearsHeld"
      ),
      IntegerValueValidation.validateOptional(
        field = commonItem.yearsHeldSinceLastGain,
        path = s"/$itemName/$arrayIndex/yearsHeldSinceLastGain"
      ),
      DecimalValueValidation.validateOptional(
        amount = commonItem.deficiencyRelief,
        minValue = 0.01,
        path = s"/$itemName/$arrayIndex/deficiencyRelief",
        message = DECIMAL_MINIMUM_INCLUSIVE
      )
    ).flatten
  }

  private def validateVoidedIsa(voidedIsa: AmendVoidedIsaPoliciesItem, arrayIndex: Int): List[MtdError] = {
    List(
      CustomerRefInsuranceValidation.validateOptional(voidedIsa.customerReference).map(
        _.copy(paths = Some(Seq(s"/voidedIsa/$arrayIndex/customerReference")))
      ),
      EventValidation.validateOptional(
        event = voidedIsa.event,
        path = s"/voidedIsa/$arrayIndex/event"
      ),
      DecimalValueValidation.validate(
        amount = voidedIsa.gainAmount,
        path = s"/voidedIsa/$arrayIndex/gainAmount",
        minValue = 0.01,
        message = DECIMAL_MINIMUM_INCLUSIVE
      ),
      DecimalValueValidation.validateOptional(
        amount = voidedIsa.taxPaidAmount,
        path = s"/voidedIsa/$arrayIndex/taxPaidAmount"
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

  private def validateForeign(foreign: AmendForeignPoliciesItem, arrayIndex: Int): List[MtdError] = {
    List(
      CustomerRefInsuranceValidation.validateOptional(foreign.customerReference).map(
        _.copy(paths = Some(Seq(s"/foreign/$arrayIndex/customerReference")))
      ),
      DecimalValueValidation.validate(
        amount = foreign.gainAmount,
        minValue = 0.01,
        path = s"/foreign/$arrayIndex/gainAmount",
        message = DECIMAL_MINIMUM_INCLUSIVE
      ),
      DecimalValueValidation.validateOptional(
        amount = foreign.taxPaidAmount,
        path = s"/foreign/$arrayIndex/taxPaidAmount"
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
