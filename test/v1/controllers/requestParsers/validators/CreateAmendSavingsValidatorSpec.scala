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

import api.controllers.requestParsers.validators.validations.ValueFormatErrorMessages
import api.models.errors._
import config.AppConfig
import mocks.MockAppConfig
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.AnyContentAsJson
import support.UnitSpec
import v1.models.request.amendSavings.CreateAmendSavingsRawData

class CreateAmendSavingsValidatorSpec extends UnitSpec with ValueFormatErrorMessages {

  private val validNino    = "AA123456A"
  private val validTaxYear = "2020-21"

  private val validRequestBodyJson: JsValue = Json.parse(
    """
      |{
      |   "securities": {
      |      "taxTakenOff": 100.11,
      |      "grossAmount": 100.12,
      |      "netAmount": 100.13
      |   },
      |   "foreignInterest": [
      |       {
      |          "amountBeforeTax": 200.11,
      |          "countryCode": "GBR",
      |          "taxTakenOff": 200.12,
      |          "specialWithholdingTax": 200.13,
      |          "taxableAmount": 200.14,
      |          "foreignTaxCreditRelief": false
      |       },
      |       {
      |          "amountBeforeTax": 45.11,
      |          "countryCode": "GBR",
      |          "taxTakenOff": 10.12,
      |          "specialWithholdingTax": 45.11,
      |          "taxableAmount": 36.14,
      |          "foreignTaxCreditRelief": true
      |       },
      |       {
      |          "amountBeforeTax": 300.11,
      |          "countryCode": "GBR",
      |          "taxTakenOff": 300.12,
      |          "specialWithholdingTax": 300.13,
      |          "taxableAmount": 300.14
      |       }
      |    ]
      |}
    """.stripMargin
  )

  private val emptyRequestBodyJson: JsValue = Json.parse("""{}""")

  private val nonsenseRequestBodyJson: JsValue = Json.parse("""{"field": "value"}""")

  private val nonValidRequestBodyJson: JsValue = Json.parse(
    """
      |{
      |   "securities": {
      |      "taxTakenOff": "no",
      |      "grossAmount": 100.12,
      |      "netAmount": 100.13
      |   }
      |}
    """.stripMargin
  )

  private val missingMandatoryFieldJson: JsValue = Json.parse(
    """
      |{
      |   "foreignInterest": [
      |       {
      |          "amountBeforeTax": 200.11
      |       },
      |       {
      |          "amountBeforeTax": 300.11,
      |          "countryCode": "GBR",
      |          "taxTakenOff": 300.12,
      |          "specialWithholdingTax": 300.13,
      |          "taxableAmount": 300.14
      |       }
      |    ]
      |}
    """.stripMargin
  )

  private val invalidCountryCodeRequestBodyJson: JsValue = Json.parse(
    """
      |{
      | "foreignInterest": [
      |    {
      |       "countryCode": "England",
      |       "taxableAmount": 200.11,
      |       "foreignTaxCreditRelief": true
      |    }
      | ]
      |}
    """.stripMargin
  )

  private val invalidCountryCodeRuleRequestBodyJson: JsValue = Json.parse(
    """
      |{
      | "foreignInterest": [
      |    {
      |       "countryCode": "FRE",
      |       "taxableAmount": 200.11,
      |       "foreignTaxCreditRelief": true
      |    }
      | ]
      |}
    """.stripMargin
  )

  private val invalidForeignInterestRequestBodyJson: JsValue = Json.parse(
    """
      |{
      | "foreignInterest": [
      |    {
      |       "countryCode": "GBR",
      |       "taxableAmount": 200.111,
      |       "foreignTaxCreditRelief": true
      |    }
      | ]
      |}
    """.stripMargin
  )

  private val invalidSecuritiesRequestBodyJson: JsValue = Json.parse(
    """
      |{
      |   "securities": {
      |      "taxTakenOff": 100.11,
      |      "grossAmount": 100.12,
      |      "netAmount": -100.13
      |   }
      |}
    """.stripMargin
  )

  private val allInvalidValueRequestBodyJson: JsValue = Json.parse(
    """
      |{
      |   "securities": {
      |      "taxTakenOff": 100.111,
      |      "grossAmount": -100.12,
      |      "netAmount": 999999999991.99
      |   },
      |   "foreignInterest": [
      |       {
      |          "amountBeforeTax": -200.11,
      |          "countryCode": "SkegVegas",
      |          "taxTakenOff": 200.121,
      |          "specialWithholdingTax": 999999999991.13,
      |          "taxableAmount": -200.14,
      |          "foreignTaxCreditRelief": false
      |       },
      |       {
      |          "amountBeforeTax": -300.11,
      |          "countryCode": "SunSeaAndSand",
      |          "taxTakenOff": -300.100,
      |          "specialWithholdingTax": -300.134,
      |          "taxableAmount": -300.14,
      |          "foreignTaxCreditRelief": true
      |       }
      |    ]
      |}
    """.stripMargin
  )

  private val validRawRequestBody                  = AnyContentAsJson(validRequestBodyJson)
  private val emptyRawRequestBody                  = AnyContentAsJson(emptyRequestBodyJson)
  private val nonsenseRawRequestBody               = AnyContentAsJson(nonsenseRequestBodyJson)
  private val nonValidRawRequestBody               = AnyContentAsJson(nonValidRequestBodyJson)
  private val missingMandatoryFieldRequestBody     = AnyContentAsJson(missingMandatoryFieldJson)
  private val invalidCountryCodeRawRequestBody     = AnyContentAsJson(invalidCountryCodeRequestBodyJson)
  private val invalidCountryCodeRuleRawRequestBody = AnyContentAsJson(invalidCountryCodeRuleRequestBodyJson)
  private val invalidForeignInterestRawRequestBody = AnyContentAsJson(invalidForeignInterestRequestBodyJson)
  private val invalidSecuritiesRawRequestBody      = AnyContentAsJson(invalidSecuritiesRequestBodyJson)
  private val allInvalidValueRawRequestBody        = AnyContentAsJson(allInvalidValueRequestBodyJson)

  class Test extends MockAppConfig {
    implicit val appConfig: AppConfig = mockAppConfig
    val validator                     = new CreateAmendSavingsValidator()

    MockedAppConfig.minimumPermittedTaxYear
      .returns(2021)
      .anyNumberOfTimes()

  }

  "running a validation" should {
    "return no errors" when {
      "a valid request is supplied" in new Test {
        validator.validate(CreateAmendSavingsRawData(validNino, validTaxYear, validRawRequestBody)) shouldBe Nil
      }
    }

    "return NinoFormatError error" when {
      "an invalid nino is supplied" in new Test {
        validator.validate(CreateAmendSavingsRawData("A12344A", validTaxYear, validRawRequestBody)) shouldBe
          List(NinoFormatError)
      }
    }

    "return TaxYearFormatError error" when {
      "an invalid tax year is supplied" in new Test {
        validator.validate(CreateAmendSavingsRawData(validNino, "20178", validRawRequestBody)) shouldBe
          List(TaxYearFormatError)
      }
    }

    "return RuleTaxYearRangeInvalidError error" when {
      "an invalid tax year range is supplied" in new Test {
        validator.validate(CreateAmendSavingsRawData(validNino, "2019-21", validRawRequestBody)) shouldBe
          List(RuleTaxYearRangeInvalidError)
      }
    }

    "return RuleTaxYearNotSupportedError error" when {
      "an invalid tax year is supplied" in new Test {
        validator.validate(CreateAmendSavingsRawData(validNino, "2018-19", validRawRequestBody)) shouldBe
          List(RuleTaxYearNotSupportedError)
      }
    }

    "return RuleIncorrectOrEmptyBodyError error" when {
      "an empty JSON body is submitted" in new Test {
        validator.validate(CreateAmendSavingsRawData(validNino, validTaxYear, emptyRawRequestBody)) shouldBe
          List(RuleIncorrectOrEmptyBodyError)
      }

      "a non-empty JSON body is submitted without any expected fields" in new Test {
        validator.validate(CreateAmendSavingsRawData(validNino, validTaxYear, nonsenseRawRequestBody)) shouldBe
          List(RuleIncorrectOrEmptyBodyError)
      }

      "an incorrectly formatted country code is submitted" in new Test {
        validator.validate(CreateAmendSavingsRawData(validNino, validTaxYear, invalidCountryCodeRawRequestBody)) shouldBe
          List(CountryCodeFormatError.copy(paths = Some(List("/foreignInterest/0/countryCode"))))
      }

      "the submitted request body has missing mandatory fields" in new Test {
        validator.validate(CreateAmendSavingsRawData(validNino, validTaxYear, missingMandatoryFieldRequestBody)) shouldBe
          List(
            RuleIncorrectOrEmptyBodyError.copy(paths = Some(
              Seq(
                "/foreignInterest/0/countryCode",
                "/foreignInterest/0/taxableAmount"
              ))))
      }
    }

    "return WrongFieldTypeError error" when {
      "the submitted request body is not in the correct format" in new Test {
        validator.validate(CreateAmendSavingsRawData(validNino, validTaxYear, nonValidRawRequestBody)) shouldBe
          List(RuleIncorrectOrEmptyBodyError.copy(paths = Some(Seq("/securities/taxTakenOff"))))
      }
    }

    "return CountryCodeRuleError error" when {
      "an invalid country code is submitted" in new Test {
        validator.validate(CreateAmendSavingsRawData(validNino, validTaxYear, invalidCountryCodeRuleRawRequestBody)) shouldBe
          List(CountryCodeRuleError.copy(paths = Some(List("/foreignInterest/0/countryCode"))))
      }
    }

    "return ValueFormatError error (single failure)" when {
      "one field fails value validation (foreign interest)" in new Test {
        validator.validate(CreateAmendSavingsRawData(validNino, validTaxYear, invalidForeignInterestRawRequestBody)) shouldBe
          List(
            ValueFormatError.copy(
              message = ZERO_MINIMUM_INCLUSIVE,
              paths = Some(Seq("/foreignInterest/0/taxableAmount"))
            ))
      }

      "one field fails value validation (securities)" in new Test {
        validator.validate(CreateAmendSavingsRawData(validNino, validTaxYear, invalidSecuritiesRawRequestBody)) shouldBe
          List(
            ValueFormatError.copy(
              message = ZERO_MINIMUM_INCLUSIVE,
              paths = Some(Seq("/securities/netAmount"))
            ))
      }
    }

    "return ValueFormatError error (multiple failures)" when {
      "multiple fields fail value validation" in new Test {
        validator.validate(CreateAmendSavingsRawData(validNino, validTaxYear, allInvalidValueRawRequestBody)) shouldBe
          List(
            CountryCodeFormatError.copy(
              paths = Some(
                List(
                  "/foreignInterest/0/countryCode",
                  "/foreignInterest/1/countryCode"
                ))
            ),
            ValueFormatError.copy(
              message = ZERO_MINIMUM_INCLUSIVE,
              paths = Some(List(
                "/securities/taxTakenOff",
                "/securities/grossAmount",
                "/securities/netAmount",
                "/foreignInterest/0/amountBeforeTax",
                "/foreignInterest/0/taxTakenOff",
                "/foreignInterest/0/specialWithholdingTax",
                "/foreignInterest/0/taxableAmount",
                "/foreignInterest/1/amountBeforeTax",
                "/foreignInterest/1/taxTakenOff",
                "/foreignInterest/1/specialWithholdingTax",
                "/foreignInterest/1/taxableAmount"
              ))
            )
          )
      }
    }

    "return multiple errors" when {
      "request supplied has multiple errors (path parameters)" in new Test {
        validator.validate(CreateAmendSavingsRawData("A12344A", "20178", emptyRawRequestBody)) shouldBe
          List(NinoFormatError, TaxYearFormatError)
      }
    }
  }

}
