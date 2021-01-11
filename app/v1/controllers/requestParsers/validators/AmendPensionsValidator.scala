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
import javax.inject.Inject
import v1.controllers.requestParsers.validators.validations._
import v1.models.errors.MtdError
import v1.models.request.amendPensions._

class AmendPensionsValidator @Inject()(implicit appConfig: AppConfig)
  extends Validator[AmendPensionsRawData] with ValueFormatErrorMessages {

  private val validationSet = List(parameterFormatValidation, parameterRuleValidation, bodyFormatValidator, bodyValueValidator)

  override def validate(data: AmendPensionsRawData): List[MtdError] = {
    run(validationSet, data).distinct
  }

  private def parameterFormatValidation: AmendPensionsRawData => List[List[MtdError]] = (data: AmendPensionsRawData) => {
    List(
      NinoValidation.validate(data.nino),
      TaxYearValidation.validate(data.taxYear)
    )
  }

  private def parameterRuleValidation: AmendPensionsRawData => List[List[MtdError]] = (data: AmendPensionsRawData) => {
    List(
      TaxYearNotSupportedValidation.validate(data.taxYear)
    )
  }

  private def bodyFormatValidator: AmendPensionsRawData => List[List[MtdError]] = { data =>
    List(
      JsonFormatValidation.validate[AmendPensionsRequestBody](data.body.json)
    )
  }

  private def bodyValueValidator: AmendPensionsRawData => List[List[MtdError]] = { data =>

    val requestBodyData = data.body.json.as[AmendPensionsRequestBody]

    List(Validator.flattenErrors(
      List(
        requestBodyData.foreignPensions.map(_.zipWithIndex.flatMap {
          case (data, index) => validateForeignPensions(data, index)
        }).getOrElse(NoValidationErrors).toList,
        requestBodyData.overseasPensionContributions.map(_.zipWithIndex.flatMap {
          case (data, index) => validateOverseasPensionContributions(data, index)
        }).getOrElse(NoValidationErrors).toList
      )
    ))
  }

  private def validateForeignPensions(foreignPensions: AmendForeignPensionsItem, arrayIndex: Int): List[MtdError] = {
    List(
      CountryCodeValidation.validate(foreignPensions.countryCode).map(
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

  private def validateOverseasPensionContributions(overseasPensionContributions: AmendOverseasPensionContributionsItem, arrayIndex: Int): List[MtdError] = {
    List(
      CustomerRefValidation.validateOptional(overseasPensionContributions.customerReference).map(
        _.copy(paths = Some(Seq(s"/overseasPensionContributions/$arrayIndex/customerReference")))
      ),
      DecimalValueValidation.validate(
        amount = overseasPensionContributions.exemptEmployersPensionContribs,
        path = s"/overseasPensionContributions/$arrayIndex/exemptEmployersPensionContribs",
      ),
      QOPSRefValidation.validateOptional(
        qopsRef = overseasPensionContributions.migrantMemReliefQopsRefNo,
        path = s"/overseasPensionContributions/$arrayIndex/migrantMemReliefQopsRefNo"
      ),
      DecimalValueValidation.validateOptional(
        amount = overseasPensionContributions.dblTaxationRelief,
        path = s"/overseasPensionContributions/$arrayIndex/dblTaxationRelief"
      ),
      CountryCodeValidation.validateOptional(overseasPensionContributions.dblTaxationCountryCode).map(
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