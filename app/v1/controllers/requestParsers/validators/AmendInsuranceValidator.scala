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
import v1.models.request.insurancePolicies.amend.AmendRawData
import v1.models.request.savings.amend.{AmendForeignInterest, AmendSavingsRawData, AmendSavingsRequestBody, AmendSecurities}

class AmendInsuranceValidator extends Validator[AmendRawData] with ValueFormatErrorMessages {

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
      JsonFormatValidation.validate[AmendSavingsRequestBody](data.body.json, RuleIncorrectOrEmptyBodyError)
    )
  }

  private def bodyValueValidator: AmendRawData => List[List[MtdError]] = { data =>

    val requestBodyData = data.body.json.as[AmendSavingsRequestBody]

    List(flattenErrors(
        List(
          requestBodyData.securities.map(validateSecurity).getOrElse(NoValidationErrors),  //Needs changing
          requestBodyData.foreignInterest.map(_.zipWithIndex.flatMap {
              case (data, index) => validateForeignInterest(data, index)
            }).getOrElse(NoValidationErrors).toList
        )
    ))
  }

  private def validateSecurity(securities: AmendSecurities): List[MtdError] = {   //Needs changing
    List(
      DecimalValueValidation.validateOptional(
        amount = securities.taxTakenOff,
        path = "/securities/taxTakenOff",
      ),
      DecimalValueValidation.validateOptional(
        amount = securities.grossAmount,
        path = "/securities/grossAmount"
      ),
      DecimalValueValidation.validateOptional(
        amount = securities.netAmount,
        path = "/securities/netAmount"
      )
    ).flatten
  }

  private def validateForeignInterest(foreignInterest: AmendForeignInterest, arrayIndex: Int): List[MtdError] = {  //Needs changing
    List(
      DecimalValueValidation.validateOptional(
        amount = foreignInterest.amountBeforeTax,
        path = s"/foreignInterest/$arrayIndex/amountBeforeTax"
      ),
      CountryCodeValidation.validate(foreignInterest.countryCode).map(
        _.copy(paths = Some(Seq(s"/foreignInterest/$arrayIndex/countryCode")))
      ),
      DecimalValueValidation.validateOptional(
        amount = foreignInterest.taxTakenOff,
        path = s"/foreignInterest/$arrayIndex/taxTakenOff"
      ),
      DecimalValueValidation.validateOptional(
        amount = foreignInterest.specialWithholdingTax,
        path = s"/foreignInterest/$arrayIndex/specialWithholdingTax"
      ),
      DecimalValueValidation.validate(
        amount = foreignInterest.taxableAmount,
        path = s"/foreignInterest/$arrayIndex/taxableAmount"
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
