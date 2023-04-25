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
import api.models.errors.MtdError
import config.AppConfig
import v1.models.request.amendSavings.{AmendForeignInterestItem, AmendSecurities, CreateAmendSavingsRawData, CreateAmendSavingsRequestBody}

import javax.inject.{Inject, Singleton}

@Singleton
class CreateAmendSavingsValidator @Inject() (implicit appConfig: AppConfig)
    extends Validator[CreateAmendSavingsRawData]
    with ValueFormatErrorMessages {

  private val validationSet = List(parameterFormatValidation, parameterRuleValidation, bodyFormatValidator, bodyValueValidator)

  override def validate(data: CreateAmendSavingsRawData): List[MtdError] = {
    run(validationSet, data).distinct
  }

  private def parameterFormatValidation: CreateAmendSavingsRawData => List[List[MtdError]] = (data: CreateAmendSavingsRawData) => {
    List(
      NinoValidation.validate(data.nino),
      TaxYearValidation.validate(data.taxYear)
    )
  }

  private def parameterRuleValidation: CreateAmendSavingsRawData => List[List[MtdError]] = { data =>
    List(
      TaxYearNotSupportedValidation.validate(data.taxYear, appConfig.minimumPermittedTaxYear)
    )
  }

  private def bodyFormatValidator: CreateAmendSavingsRawData => List[List[MtdError]] = { data =>
    List(
      JsonFormatValidation.validate[CreateAmendSavingsRequestBody](data.body.json)
    )
  }

  private def bodyValueValidator: CreateAmendSavingsRawData => List[List[MtdError]] = { data =>
    val requestBodyData = data.body.json.as[CreateAmendSavingsRequestBody]

    List(
      flattenErrors(
        List(
          requestBodyData.securities.map(validateSecurity).getOrElse(NoValidationErrors),
          requestBodyData.foreignInterest
            .map(_.zipWithIndex.flatMap { case (data, index) =>
              validateForeignInterest(data, index)
            })
            .getOrElse(NoValidationErrors)
            .toList
        )
      ))
  }

  private def validateSecurity(securities: AmendSecurities): List[MtdError] = {
    List(
      DecimalValueValidation.validateOptional(
        amount = securities.taxTakenOff,
        path = "/securities/taxTakenOff"
      ),
      DecimalValueValidation.validate(
        amount = securities.grossAmount,
        path = "/securities/grossAmount"
      ),
      DecimalValueValidation.validateOptional(
        amount = securities.netAmount,
        path = "/securities/netAmount"
      )
    ).flatten
  }

  private def validateForeignInterest(foreignInterest: AmendForeignInterestItem, arrayIndex: Int): List[MtdError] = {
    List(
      DecimalValueValidation.validateOptional(
        amount = foreignInterest.amountBeforeTax,
        path = s"/foreignInterest/$arrayIndex/amountBeforeTax"
      ),
      CountryCodeValidation
        .validate(foreignInterest.countryCode)
        .map(
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

}
