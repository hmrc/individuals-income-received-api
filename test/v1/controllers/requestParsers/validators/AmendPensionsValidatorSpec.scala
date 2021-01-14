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
import mocks.MockAppConfig
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.AnyContentAsJson
import support.UnitSpec
import v1.controllers.requestParsers.validators.validations.ValueFormatErrorMessages
import v1.models.errors._
import v1.models.request.amendPensions.AmendPensionsRawData

class AmendPensionsValidatorSpec extends UnitSpec with ValueFormatErrorMessages {

  private val validNino = "AA123456A"
  private val validTaxYear = "2020-21"

  private val validRequestBodyJson: JsValue = Json.parse(
    """
      |{
      |   "foreignPensions": [
      |      {
      |         "countryCode": "DEU",
      |         "amountBeforeTax": 100.23,
      |         "taxTakenOff": 1.23,
      |         "specialWithholdingTax": 2.23,
      |         "foreignTaxCreditRelief": false,
      |         "taxableAmount": 3.23
      |      },
      |      {
      |         "countryCode": "FRA",
      |         "amountBeforeTax": 200.25,
      |         "taxTakenOff": 1.27,
      |         "specialWithholdingTax": 2.50,
      |         "foreignTaxCreditRelief": true,
      |         "taxableAmount": 3.50
      |      }
      |   ],
      |   "overseasPensionContributions": [
      |      {
      |         "customerReference": "PENSIONINCOME245",
      |         "exemptEmployersPensionContribs": 200.23,
      |         "migrantMemReliefQopsRefNo": "QOPS000000",
      |         "dblTaxationRelief": 4.23,
      |         "dblTaxationCountryCode": "FRA",
      |         "dblTaxationArticle": "AB3211-1",
      |         "dblTaxationTreaty": "Treaty",
      |         "sf74reference": "SF74-123456"
      |      },
      |      {
      |         "customerReference": "PENSIONINCOME275",
      |         "exemptEmployersPensionContribs": 270.50,
      |         "migrantMemReliefQopsRefNo": "QOPS000245",
      |         "dblTaxationRelief": 5.50,
      |         "dblTaxationCountryCode": "NGA",
      |         "dblTaxationArticle": "AB3477-5",
      |         "dblTaxationTreaty": "Treaty",
      |         "sf74reference": "SF74-1235"
      |      }
      |   ]
      |}
    """.stripMargin
  )

  private val emptyRequestBodyJson: JsValue = Json.parse("""{}""")

  private val nonsenseRequestBodyJson: JsValue = Json.parse("""{"field": "value"}""")

  private val nonValidRequestBodyJson: JsValue = Json.parse(
    """
      |{
      |   "foreignPensions": [
      |      {
      |         "countryCode": "DEU",
      |         "amountBeforeTax": 100.23,
      |         "taxTakenOff": "no",
      |         "specialWithholdingTax": 2.23,
      |         "foreignTaxCreditRelief": false,
      |         "taxableAmount": 3.23
      |      }
      |   ]
      |}
    """.stripMargin
  )

  private val missingMandatoryFieldJson: JsValue = Json.parse(
    """
      |{
      |   "foreignPensions": [{"amountBeforeTax": 100.23}]
      |}
    """.stripMargin
  )

  private val invalidCustomerRefRequestBodyJson: JsValue = Json.parse(
    """
      |{
      |   "overseasPensionContributions": [
      |      {
      |         "customerReference": "This customer ref string is 91 characters long ------------------------------------------91",
      |         "exemptEmployersPensionContribs": 200.23,
      |         "migrantMemReliefQopsRefNo": "QOPS000000",
      |         "dblTaxationRelief": 4.23,
      |         "dblTaxationCountryCode": "FRA",
      |         "dblTaxationArticle": "AB3211-1",
      |         "dblTaxationTreaty": "Treaty",
      |         "sf74reference": "SF74-123456"
      |      }
      |   ]
      |}
    """.stripMargin
  )

  private val invalidQOPSRefRequestBodyJson: JsValue = Json.parse(
    """
      |{
      |   "overseasPensionContributions": [
      |      {
      |         "customerReference": "PENSIONINCOME245",
      |         "exemptEmployersPensionContribs": 200.23,
      |         "migrantMemReliefQopsRefNo": "This qopsRef string is 91 characters long ---------------------------------------------- 91",
      |         "dblTaxationRelief": 4.23,
      |         "dblTaxationCountryCode": "FRA",
      |         "dblTaxationArticle": "AB3211-1",
      |         "dblTaxationTreaty": "Treaty",
      |         "sf74reference": "SF74-123456"
      |      }
      |   ]
      |}
    """.stripMargin
  )

  private val invalidSF74RefRequestBodyJson: JsValue = Json.parse(
    """
      |{
      |   "overseasPensionContributions": [
      |      {
      |         "customerReference": "PENSIONINCOME245",
      |         "exemptEmployersPensionContribs": 200.23,
      |         "migrantMemReliefQopsRefNo": "QOPS000000",
      |         "dblTaxationRelief": 4.23,
      |         "dblTaxationCountryCode": "FRA",
      |         "dblTaxationArticle": "AB3211-1",
      |         "dblTaxationTreaty": "Treaty",
      |         "sf74reference": "This sf74Ref string is 91 characters long ---------------------------------------------- 91"
      |      }
      |   ]
      |}
    """.stripMargin
  )

  private val invalidDoubleTaxationArticleRequestBodyJson: JsValue = Json.parse(
    """
      |{
      |   "overseasPensionContributions": [
      |      {
      |         "customerReference": "PENSIONINCOME245",
      |         "exemptEmployersPensionContribs": 200.23,
      |         "migrantMemReliefQopsRefNo": "QOPS000000",
      |         "dblTaxationRelief": 4.23,
      |         "dblTaxationCountryCode": "FRA",
      |         "dblTaxationArticle": "This dblTaxationArticle string is 91 characters long ------------------------------------91",
      |         "dblTaxationTreaty": "Treaty",
      |         "sf74reference": "SF74-123456"
      |      }
      |   ]
      |}
    """.stripMargin
  )

  private val invalidDoubleTaxationTreatyRequestBodyJson: JsValue = Json.parse(
    """
      |{
      |   "overseasPensionContributions": [
      |      {
      |         "customerReference": "PENSIONINCOME245",
      |         "exemptEmployersPensionContribs": 200.23,
      |         "migrantMemReliefQopsRefNo": "QOPS000000",
      |         "dblTaxationRelief": 4.23,
      |         "dblTaxationCountryCode": "FRA",
      |         "dblTaxationArticle": "AB3211-1",
      |         "dblTaxationTreaty": "This dblTaxationTreaty string is 91 characters long -------------------------------------91",
      |         "sf74reference": "SF74-123456"
      |      }
      |   ]
      |}
    """.stripMargin
  )

  private val invalidCountryCodeRuleRequestBodyJson: JsValue = Json.parse(
    """
      |{
      |   "foreignPensions": [
      |      {
      |         "countryCode": "PUR",
      |         "amountBeforeTax": 100.23,
      |         "taxTakenOff": 1.23,
      |         "specialWithholdingTax": 2.23,
      |         "foreignTaxCreditRelief": false,
      |         "taxableAmount": 3.23
      |      }
      |   ]
      |}
    """.stripMargin
  )

  private val invalidCountryCodeRequestBodyJson: JsValue = Json.parse(
    """
      |{
      |   "overseasPensionContributions": [
      |      {
      |         "customerReference": "PENSIONINCOME245",
      |         "exemptEmployersPensionContribs": 200.23,
      |         "migrantMemReliefQopsRefNo": "QOPS000000",
      |         "dblTaxationRelief": 4.23,
      |         "dblTaxationCountryCode": "FRANCE",
      |         "dblTaxationArticle": "AB3211-1",
      |         "dblTaxationTreaty": "Treaty",
      |         "sf74reference": "SF74-123456"
      |      }
      |   ]
      |}
    """.stripMargin
  )

  private val invalidForeignPensionsRequestBodyJson: JsValue = Json.parse(
    """
      |{
      |   "foreignPensions": [
      |      {
      |         "countryCode": "NGA",
      |         "amountBeforeTax": 100.239,
      |         "taxTakenOff": 1.23,
      |         "specialWithholdingTax": 2.23,
      |         "foreignTaxCreditRelief": false,
      |         "taxableAmount": 3.23
      |      }
      |   ]
      |}
    """.stripMargin
  )

  private val invalidOverseasPensionContributionsRequestBodyJson: JsValue = Json.parse(
    """
      |{
      |   "overseasPensionContributions": [
      |      {
      |         "customerReference": "PENSIONINCOME245",
      |         "exemptEmployersPensionContribs": 200.234,
      |         "migrantMemReliefQopsRefNo": "QOPS000000",
      |         "dblTaxationRelief": 4.23,
      |         "dblTaxationCountryCode": "FRA",
      |         "dblTaxationArticle": "AB3211-1",
      |         "dblTaxationTreaty": "Treaty",
      |         "sf74reference": "SF74-123456"
      |      }
      |   ]
      |}
    """.stripMargin
  )

  private val allInvalidValueRequestBodyJson: JsValue = Json.parse(
    """
      |{
      |   "foreignPensions": [
      |      {
      |         "countryCode": "SBT",
      |         "amountBeforeTax": 100.234,
      |         "taxTakenOff": 1.235,
      |         "specialWithholdingTax": -2.23,
      |         "foreignTaxCreditRelief": false,
      |         "taxableAmount": -3.23
      |      },
      |      {
      |         "countryCode": "FRANCE",
      |         "amountBeforeTax": -200.25,
      |         "taxTakenOff": 1.273,
      |         "specialWithholdingTax": -2.50,
      |         "foreignTaxCreditRelief": true,
      |         "taxableAmount": 3.508
      |      }
      |   ],
      |   "overseasPensionContributions": [
      |      {
      |         "customerReference": "This customer ref string is 91 characters long ------------------------------------------91",
      |         "exemptEmployersPensionContribs": 200.237,
      |         "migrantMemReliefQopsRefNo": "This qopsRef string is 91 characters long ---------------------------------------------- 91",
      |         "dblTaxationRelief": -4.238,
      |         "dblTaxationCountryCode": "PUR",
      |         "dblTaxationArticle": "This dblTaxationArticle string is 91 characters long ------------------------------------91",
      |         "dblTaxationTreaty": "This dblTaxationTreaty string is 91 characters long -------------------------------------91",
      |         "sf74reference": "This sf74Ref string is 91 characters long ---------------------------------------------- 91"
      |      },
      |      {
      |         "customerReference": "This customer ref string is 91 characters long ------------------------------------------91",
      |         "exemptEmployersPensionContribs": -270.509,
      |         "migrantMemReliefQopsRefNo": "This qopsRef string is 91 characters long ---------------------------------------------- 91",
      |         "dblTaxationRelief": 5.501,
      |         "dblTaxationCountryCode": "GERMANY",
      |         "dblTaxationArticle": "This dblTaxationArticle string is 91 characters long ------------------------------------91",
      |         "dblTaxationTreaty": "This dblTaxationTreaty string is 91 characters long -------------------------------------91",
      |         "sf74reference": "This sf74Ref string is 91 characters long ---------------------------------------------- 91"
      |      }
      |   ]
      |}
    """.stripMargin
  )

  private val validRawRequestBody = AnyContentAsJson(validRequestBodyJson)
  private val emptyRawRequestBody = AnyContentAsJson(emptyRequestBodyJson)
  private val nonsenseRawRequestBody = AnyContentAsJson(nonsenseRequestBodyJson)
  private val nonValidRawRequestBody = AnyContentAsJson(nonValidRequestBodyJson)
  private val missingMandatoryFieldRequestBody = AnyContentAsJson(missingMandatoryFieldJson)
  private val invalidCustomerRefRawRequestBody = AnyContentAsJson(invalidCustomerRefRequestBodyJson)
  private val invalidQOPSRefRawRequestBody = AnyContentAsJson(invalidQOPSRefRequestBodyJson)
  private val invalidSF74RefRawRequestBody = AnyContentAsJson(invalidSF74RefRequestBodyJson)
  private val invalidDoubleTaxationArticleRawRequestBody = AnyContentAsJson(invalidDoubleTaxationArticleRequestBodyJson)
  private val invalidDoubleTaxationTreatyRawRequestBody = AnyContentAsJson(invalidDoubleTaxationTreatyRequestBodyJson)
  private val invalidCountryCodeRawRequestBody = AnyContentAsJson(invalidCountryCodeRequestBodyJson)
  private val invalidCountryCodeRuleRawRequestBody = AnyContentAsJson(invalidCountryCodeRuleRequestBodyJson)
  private val invalidForeignPensionsRawRequestBody = AnyContentAsJson(invalidForeignPensionsRequestBodyJson)
  private val invalidOverseasPensionContributionsRawRequestBody = AnyContentAsJson(invalidOverseasPensionContributionsRequestBodyJson)
  private val allInvalidValueRawRequestBody = AnyContentAsJson(allInvalidValueRequestBodyJson)

  class Test extends MockAppConfig {

    implicit val appConfig: AppConfig = mockAppConfig

    val validator = new AmendPensionsValidator()

    MockedAppConfig.minimumPermittedTaxYear
      .returns(2021)
      .anyNumberOfTimes()
  }

  "running a validation" should {
    "return no errors" when {
      "a valid request is supplied" in new Test {
        validator.validate(AmendPensionsRawData(validNino, validTaxYear, validRawRequestBody)) shouldBe Nil
      }
    }

    // parameter format error scenarios
    "return NinoFormatError error" when {
      "an invalid nino is supplied" in new Test {
        validator.validate(AmendPensionsRawData("A12344A", validTaxYear, validRawRequestBody)) shouldBe
          List(NinoFormatError)
      }
    }

    "return TaxYearFormatError error" when {
      "an invalid tax year is supplied" in new Test {
        validator.validate(AmendPensionsRawData(validNino, "20178", validRawRequestBody)) shouldBe
          List(TaxYearFormatError)
      }
    }

    "return RuleTaxYearRangeInvalidError error for an invalid tax year range" in new Test {
      validator.validate(AmendPensionsRawData(validNino, "2020-22", validRawRequestBody)) shouldBe
        List(RuleTaxYearRangeInvalidError)
    }

    "return multiple errors for multiple invalid request parameters" in new Test {
      validator.validate(AmendPensionsRawData("notValid", "2020-22", validRawRequestBody)) shouldBe
        List(NinoFormatError, RuleTaxYearRangeInvalidError)
    }

    // parameter rule error scenarios
    "return RuleTaxYearNotSupportedError error for an unsupported tax year" in new Test {
      validator.validate(AmendPensionsRawData(validNino, "2019-20", validRawRequestBody)) shouldBe
        List(RuleTaxYearNotSupportedError)
    }

    // body format error scenarios
    "return RuleIncorrectOrEmptyBodyError error" when {
      "an empty JSON body is submitted" in new Test {
        validator.validate(AmendPensionsRawData(validNino, validTaxYear, emptyRawRequestBody)) shouldBe
          List(RuleIncorrectOrEmptyBodyError)
      }

      "a non-empty JSON body is submitted without any expected fields" in new Test {
        validator.validate(AmendPensionsRawData(validNino, validTaxYear, nonsenseRawRequestBody)) shouldBe
          List(RuleIncorrectOrEmptyBodyError)
      }

      "the submitted request body is not in the correct format" in new Test {
        validator.validate(AmendPensionsRawData(validNino, validTaxYear, nonValidRawRequestBody)) shouldBe
          List(RuleIncorrectOrEmptyBodyError.copy(paths = Some(Seq("/foreignPensions/0/taxTakenOff"))))
      }

      "the submitted request body has missing mandatory fields" in new Test {
        validator.validate(AmendPensionsRawData(validNino, validTaxYear, missingMandatoryFieldRequestBody)) shouldBe
          List(RuleIncorrectOrEmptyBodyError.copy(paths = Some(Seq(
            "/foreignPensions/0/taxableAmount",
            "/foreignPensions/0/foreignTaxCreditRelief",
            "/foreignPensions/0/countryCode"
          ))))
      }
    }

    // body value error scenarios
    "return CustomerRefFormatError error" when {
      "an incorrectly formatted customer reference is submitted" in new Test {
        validator.validate(AmendPensionsRawData(validNino, validTaxYear, invalidCustomerRefRawRequestBody)) shouldBe
          List(CustomerRefFormatError.copy(paths = Some(List("/overseasPensionContributions/0/customerReference"))))
      }
    }

    "return QOPSRefFormatError error" when {
      "an incorrectly formatted migrantMemReliefQopsRefNo is submitted" in new Test {
        validator.validate(AmendPensionsRawData(validNino, validTaxYear, invalidQOPSRefRawRequestBody)) shouldBe
          List(QOPSRefFormatError.copy(paths = Some(List("/overseasPensionContributions/0/migrantMemReliefQopsRefNo"))))
      }
    }

    "return SF74RefFormatError error" when {
      "an incorrectly formatted sf74reference is submitted" in new Test {
        validator.validate(AmendPensionsRawData(validNino, validTaxYear, invalidSF74RefRawRequestBody)) shouldBe
          List(SF74RefFormatError.copy(paths = Some(List("/overseasPensionContributions/0/sf74reference"))))
      }
    }

    "return DoubleTaxationArticleFormatError error" when {
      "an incorrectly formatted dblTaxationArticle is submitted" in new Test {
        validator.validate(AmendPensionsRawData(validNino, validTaxYear, invalidDoubleTaxationArticleRawRequestBody)) shouldBe
          List(DoubleTaxationArticleFormatError.copy(paths = Some(List("/overseasPensionContributions/0/dblTaxationArticle"))))
      }
    }

    "return DoubleTaxationTreatyFormatError error" when {
      "an incorrectly formatted dblTaxationTreaty is submitted" in new Test {
        validator.validate(AmendPensionsRawData(validNino, validTaxYear, invalidDoubleTaxationTreatyRawRequestBody)) shouldBe
          List(DoubleTaxationTreatyFormatError.copy(paths = Some(List("/overseasPensionContributions/0/dblTaxationTreaty"))))
      }
    }

    "return CountryCodeFormatError error" when {
      "an incorrectly formatted dblTaxationCountryCode is submitted" in new Test {
        validator.validate(AmendPensionsRawData(validNino, validTaxYear, invalidCountryCodeRawRequestBody)) shouldBe
          List(CountryCodeFormatError.copy(paths = Some(List("/overseasPensionContributions/0/dblTaxationCountryCode"))))
      }
    }

    "return CountryCodeRuleError error" when {
      "an invalid country code is submitted" in new Test {
        validator.validate(AmendPensionsRawData(validNino, validTaxYear, invalidCountryCodeRuleRawRequestBody)) shouldBe
          List(CountryCodeRuleError.copy(paths = Some(List("/foreignPensions/0/countryCode"))))
      }
    }

    "return ValueFormatError error (single failure)" when {
      "one field fails value validation (foreign pensions)" in new Test {
        validator.validate(AmendPensionsRawData(validNino, validTaxYear, invalidForeignPensionsRawRequestBody)) shouldBe
          List(ValueFormatError.copy(
            message = ZERO_MINIMUM_INCLUSIVE,
            paths = Some(Seq("/foreignPensions/0/amountBeforeTax"))
          ))
      }

      "one field fails value validation (Overseas Pension Contributions)" in new Test {
        validator.validate(AmendPensionsRawData(validNino, validTaxYear, invalidOverseasPensionContributionsRawRequestBody)) shouldBe
          List(ValueFormatError.copy(
            message = ZERO_MINIMUM_INCLUSIVE,
            paths = Some(Seq("/overseasPensionContributions/0/exemptEmployersPensionContribs"))
          ))
      }
    }

    "return ValueFormatError error (multiple failures)" when {
      "multiple fields fail value validation" in new Test {
        validator.validate(AmendPensionsRawData(validNino, validTaxYear, allInvalidValueRawRequestBody)) shouldBe
          List(
            CustomerRefFormatError.copy(
              paths = Some(List(
                "/overseasPensionContributions/0/customerReference",
                "/overseasPensionContributions/1/customerReference"
              ))
            ),
            QOPSRefFormatError.copy(
              paths = Some(List(
                "/overseasPensionContributions/0/migrantMemReliefQopsRefNo",
                "/overseasPensionContributions/1/migrantMemReliefQopsRefNo"
              ))
            ),
            SF74RefFormatError.copy(
              paths = Some(List(
                "/overseasPensionContributions/0/sf74reference",
                "/overseasPensionContributions/1/sf74reference"
              ))
            ),
            DoubleTaxationTreatyFormatError.copy(
              paths = Some(List(
                "/overseasPensionContributions/0/dblTaxationTreaty",
                "/overseasPensionContributions/1/dblTaxationTreaty"
              ))
            ),
            CountryCodeRuleError.copy(
              paths = Some(List(
                "/foreignPensions/0/countryCode",
                "/overseasPensionContributions/0/dblTaxationCountryCode"
              ))
            ),
            DoubleTaxationArticleFormatError.copy(
              paths = Some(List(
                "/overseasPensionContributions/0/dblTaxationArticle",
                "/overseasPensionContributions/1/dblTaxationArticle"
              ))
            ),
            CountryCodeFormatError.copy(
              paths = Some(List(
                "/foreignPensions/1/countryCode",
                "/overseasPensionContributions/1/dblTaxationCountryCode"
              ))
            ),
            ValueFormatError.copy(
              message = ZERO_MINIMUM_INCLUSIVE,
              paths = Some(List(
                "/foreignPensions/0/amountBeforeTax",
                "/foreignPensions/0/taxTakenOff",
                "/foreignPensions/0/specialWithholdingTax",
                "/foreignPensions/0/taxableAmount",
                "/foreignPensions/1/amountBeforeTax",
                "/foreignPensions/1/taxTakenOff",
                "/foreignPensions/1/specialWithholdingTax",
                "/foreignPensions/1/taxableAmount",
                "/overseasPensionContributions/0/exemptEmployersPensionContribs",
                "/overseasPensionContributions/0/dblTaxationRelief",
                "/overseasPensionContributions/1/exemptEmployersPensionContribs",
                "/overseasPensionContributions/1/dblTaxationRelief"
              ))
            )
          )
      }
    }

    "return multiple errors" when {
      "request supplied has multiple errors (path parameters)" in new Test {
        validator.validate(AmendPensionsRawData("A12344A", "20178", emptyRawRequestBody)) shouldBe
          List(NinoFormatError, TaxYearFormatError)
      }
    }
  }
}