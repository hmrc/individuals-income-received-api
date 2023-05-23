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

package v1andv2.controllers.requestParsers.validators

import api.controllers.requestParsers.validators.Validator
import api.controllers.requestParsers.validators.validations._
import api.models.errors.MtdError
import config.AppConfig
import v1andv2.models.request.createAmendPensions._

import javax.inject.{Inject, Singleton}

@Singleton
class CreateAmendPensionsValidator @Inject() (implicit appConfig: AppConfig)
    extends Validator[CreateAmendPensionsRawData]
    with ValueFormatErrorMessages {

  private val validationSet = List(parameterFormatValidation, parameterRuleValidation, bodyFormatValidator, bodyValueValidator)

  override def validate(data: CreateAmendPensionsRawData): List[MtdError] = {
    run(validationSet, data).distinct
  }

  private def parameterFormatValidation: CreateAmendPensionsRawData => List[List[MtdError]] = (data: CreateAmendPensionsRawData) => {
    List(
      NinoValidation.validate(data.nino),
      TaxYearValidation.validate(data.taxYear)
    )
  }

  private def parameterRuleValidation: CreateAmendPensionsRawData => List[List[MtdError]] = (data: CreateAmendPensionsRawData) => {
    List(
      TaxYearNotSupportedValidation.validate(data.taxYear, appConfig.minimumPermittedTaxYear)
    )
  }

  private def bodyFormatValidator: CreateAmendPensionsRawData => List[List[MtdError]] = { data =>
    List(
      JsonFormatValidation.validate[CreateAmendPensionsRequestBody](data.body.json)
    )
  }

  private def bodyValueValidator: CreateAmendPensionsRawData => List[List[MtdError]] = { data =>
    val requestBodyData = data.body.json.as[CreateAmendPensionsRequestBody]

    List(
      flattenErrors(
        List(
          requestBodyData.foreignPensions
            .map(_.zipWithIndex.flatMap { case (data, index) =>
              validateForeignPensions(data, index)
            })
            .getOrElse(NoValidationErrors)
            .toList,
          requestBodyData.overseasPensionContributions
            .map(_.zipWithIndex.flatMap { case (data, index) =>
              validateOverseasPensionContributions(data, index)
            })
            .getOrElse(NoValidationErrors)
            .toList
        )
      ))
  }

  private def validateForeignPensions(foreignPensions: CreateAmendForeignPensionsItem, arrayIndex: Int): List[MtdError] = {
    List(
      CountryCodeValidation
        .validate(foreignPensions.countryCode)
        .map(
          _.copy(paths = Some(Seq(s"/foreignPensions/$arrayIndex/countryCode")))
        ),
      DecimalValueValidation.validateOptional(
        amount = foreignPensions.amountBeforeTax,
        path = s"/foreignPensions/$arrayIndex/amountBeforeTax"
      ),
      DecimalValueValidation.validateOptional(
        amount = foreignPensions.taxTakenOff,
        path = s"/foreignPensions/$arrayIndex/taxTakenOff"
      ),
      DecimalValueValidation.validateOptional(
        amount = foreignPensions.specialWithholdingTax,
        path = s"/foreignPensions/$arrayIndex/specialWithholdingTax"
      ),
      DecimalValueValidation.validate(
        amount = foreignPensions.taxableAmount,
        path = s"/foreignPensions/$arrayIndex/taxableAmount"
      )
    ).flatten
  }

  private def validateOverseasPensionContributions(overseasPensionContributions: CreateAmendOverseasPensionContributions,
                                                   arrayIndex: Int): List[MtdError] = {
    List(
      CustomerRefValidation
        .validateOptional(overseasPensionContributions.customerReference)
        .map(
          _.copy(paths = Some(Seq(s"/overseasPensionContributions/$arrayIndex/customerReference")))
        ),
      DecimalValueValidation.validate(
        amount = overseasPensionContributions.exemptEmployersPensionContribs,
        path = s"/overseasPensionContributions/$arrayIndex/exemptEmployersPensionContribs"
      ),
      QOPSRefValidation.validateOptional(
        qopsRef = overseasPensionContributions.migrantMemReliefQopsRefNo,
        path = s"/overseasPensionContributions/$arrayIndex/migrantMemReliefQopsRefNo"
      ),
      DecimalValueValidation.validateOptional(
        amount = overseasPensionContributions.dblTaxationRelief,
        path = s"/overseasPensionContributions/$arrayIndex/dblTaxationRelief"
      ),
      CountryCodeValidation
        .validateOptional(overseasPensionContributions.dblTaxationCountryCode)
        .map(
          _.copy(paths = Some(Seq(s"/overseasPensionContributions/$arrayIndex/dblTaxationCountryCode")))
        ),
      DoubleTaxationArticleValidation.validateOptional(
        dblTaxationArticle = overseasPensionContributions.dblTaxationArticle,
        path = s"/overseasPensionContributions/$arrayIndex/dblTaxationArticle"
      ),
      DoubleTaxationTreatyValidation.validateOptional(
        dblTaxationTreaty = overseasPensionContributions.dblTaxationTreaty,
        path = s"/overseasPensionContributions/$arrayIndex/dblTaxationTreaty"
      ),
      SF74RefValidation.validateOptional(
        sf74Ref = overseasPensionContributions.sf74reference,
        path = s"/overseasPensionContributions/$arrayIndex/sf74reference"
      )
    ).flatten
  }

}
