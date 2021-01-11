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
import v1.models.request.amendForeign.AmendForeignRawData

class AmendForeignValidatorSpec extends UnitSpec with ValueFormatErrorMessages {

  private val validNino = "AA123456A"
  private val validTaxYear = "2018-19"

  private val validRequestBodyJson: JsValue = Json.parse(
    """
      |{
      |   "foreignEarnings": {
      |     "customerReference":"FOREIGNINCME123A",
      |     "earningsNotTaxableUK":"99999999999.99"
      |   },
      |   "unremittableForeignIncome": [
      |     {
      |       "countryCode":"FRA",
      |       "amountInForeignCurrency":"0",
      |       "amountTaxPaid":"0"
      |     },
      |     {
      |       "countryCode":"GBR",
      |       "amountInForeignCurrency":"99999999999.99",
      |       "amountTaxPaid":"99999999999.99"
      |     },
      |     {
      |       "countryCode":"ESP",
      |       "amountInForeignCurrency":"0.99",
      |       "amountTaxPaid":"100"
      |     }
      |   ]
      |}
      |""".stripMargin
  )

  private val allInvalidValueRawRequestBodyJson: JsValue = Json.parse(
    """
      |{
      |   "foreignEarnings": {
      |     "customerReference":"This customer ref string is 91 characters long ------------------------------------------91",
      |     "earningsNotTaxableUK":"99999999999.9999999"
      |   },
      |   "unremittableForeignIncome": [
      |     {
      |       "countryCode":"FRFFFA",
      |       "amountInForeignCurrency":"-1000",
      |       "amountTaxPaid":"99999999999999999999999"
      |     },
      |     {
      |       "countryCode":"FFF",
      |       "amountInForeignCurrency":"-1000",
      |       "amountTaxPaid":"99999999999999999999999"
      |     }
      |   ]
      |}
      |""".stripMargin
  )

  private val emptyRequestBodyJson: JsValue = Json.parse("""{}""")

  private val nonsenseRequestBodyJson: JsValue = Json.parse("""{"field": "value"}""")

  private val nonValidRequestBodyJson: JsValue = Json.parse(
    """
      |{
      |   "foreignEarnings": {
      |     "customerReference":"FOREIGNINCME123A",
      |     "earningsNotTaxableUK":"99999999999.99"
      |   },
      |   "unremittableForeignIncome": [
      |     {
      |       "countryCode":"FRA",
      |       "amountInForeignCurrency":"0",
      |       "amountTaxPaid":true
      |     },
      |     {
      |       "countryCode":"GBR",
      |       "amountInForeignCurrency":"99999999999.99",
      |       "amountTaxPaid":false
      |     },
      |     {
      |       "countryCode":"ESP",
      |       "amountInForeignCurrency":"0.99",
      |       "amountTaxPaid":"100"
      |     }
      |   ]
      |}
      |""".stripMargin
  )

  private val missingMandatoryFieldJson: JsValue = Json.parse(
    """
      |{
      |  "unremittableForeignIncome" : [
      |    {
      |      "amountInForeignCurrency":"0",
      |      "amountTaxPaid": 100
      |    }
      |  ]
      |}
    """.stripMargin
  )

  private val invalidEarningsNotTaxableUKRequestBodyJson: JsValue = Json.parse(
    """
      |{
      |   "foreignEarnings": {
      |     "earningsNotTaxableUK":"-1"
      |   },
      |   "unremittableForeignIncome": [
      |     {
      |       "countryCode":"FRA",
      |       "amountInForeignCurrency":"0",
      |       "amountTaxPaid":"0"
      |     }
      |   ]
      |}
      |""".stripMargin
  )

  private val invalidCustomerReferenceRequestBodyJson: JsValue = Json.parse(
    """
      |{
      |   "foreignEarnings": {
      |     "customerReference":"This customer ref string is 91 characters long ------------------------------------------91",
      |     "earningsNotTaxableUK":"99999999999.99"
      |   },
      |   "unremittableForeignIncome": [
      |     {
      |       "countryCode":"FRA",
      |       "amountInForeignCurrency":"0",
      |       "amountTaxPaid":"0"
      |     }
      |   ]
      |}
      |""".stripMargin
  )

  private val invalidCountryCodeRequestBodyJson: JsValue = Json.parse(
    """
      |{
      |   "foreignEarnings": {
      |     "customerReference":"FOREIGNINCME123A",
      |     "earningsNotTaxableUK":"99999999999.99"
      |   },
      |   "unremittableForeignIncome": [
      |     {
      |       "countryCode":"EEE",
      |       "amountInForeignCurrency":"0",
      |       "amountTaxPaid":"0"
      |     }
      |   ]
      |}
      |""".stripMargin
  )

  private val invalidCountryCodeFormatRequestBodyJson: JsValue = Json.parse(
    """
      |{
      |   "foreignEarnings": {
      |     "customerReference":"FOREIGNINCME123A",
      |     "earningsNotTaxableUK":"99999999999.99"
      |   },
      |   "unremittableForeignIncome": [
      |     {
      |       "countryCode":"EEEE",
      |       "amountInForeignCurrency":"0",
      |       "amountTaxPaid":"0"
      |     }
      |   ]
      |}
      |""".stripMargin
  )

  private val invalidAmountInForeignCurrencyRequestBodyJson: JsValue = Json.parse(
    """
      |{
      |   "foreignEarnings": {
      |     "customerReference":"FOREIGNINCME123A",
      |     "earningsNotTaxableUK":"99999999999.99"
      |   },
      |   "unremittableForeignIncome": [
      |     {
      |       "countryCode":"FRA",
      |       "amountInForeignCurrency":"-1",
      |       "amountTaxPaid":"0"
      |     }
      |   ]
      |}
      |""".stripMargin
  )

  private val invalidAmountTaxPaidRequestBodyJson: JsValue = Json.parse(
    """
      |{
      |   "foreignEarnings": {
      |     "customerReference":"FOREIGNINCME123A",
      |     "earningsNotTaxableUK":"99999999999.99"
      |   },
      |   "unremittableForeignIncome": [
      |     {
      |       "countryCode":"FRA",
      |       "amountInForeignCurrency":"0",
      |       "amountTaxPaid":"99999999999999999999999"
      |     }
      |   ]
      |}
      |""".stripMargin
  )

  private val validRawRequestBody = AnyContentAsJson(validRequestBodyJson)
  private val emptyRawRequestBody = AnyContentAsJson(emptyRequestBodyJson)
  private val nonsenseRawRequestBody = AnyContentAsJson(nonsenseRequestBodyJson)
  private val nonValidRawRequestBody = AnyContentAsJson(nonValidRequestBodyJson)
  private val missingMandatoryFieldRequestBody = AnyContentAsJson(missingMandatoryFieldJson)
  private val invalidEarningsNotTaxableUKRequestBody = AnyContentAsJson(invalidEarningsNotTaxableUKRequestBodyJson)
  private val invalidCustomerReferenceRequestBody = AnyContentAsJson(invalidCustomerReferenceRequestBodyJson)
  private val invalidCountryCodeRequestBody = AnyContentAsJson(invalidCountryCodeRequestBodyJson)
  private val invalidCountryCodeFormatRequestBody = AnyContentAsJson(invalidCountryCodeFormatRequestBodyJson)
  private val invalidAmountInForeignCurrencyRequestBody = AnyContentAsJson(invalidAmountInForeignCurrencyRequestBodyJson)
  private val invalidAmountTaxPaidRequestBody = AnyContentAsJson(invalidAmountTaxPaidRequestBodyJson)
  private val allInvalidValueRawRequestBody = AnyContentAsJson(allInvalidValueRawRequestBodyJson)

  class Test extends MockAppConfig {

    implicit val appConfig: AppConfig = mockAppConfig

    val validator = new AmendForeignValidator()

    MockedAppConfig.minimumPermittedTaxYear
      .returns(2019)
      .anyNumberOfTimes()
  }

  "running a validation" should {
    "return no errors" when {
      "a valid request is supplied" in new Test {
        validator.validate(AmendForeignRawData(validNino, validTaxYear, validRawRequestBody)) shouldBe Nil
      }
    }

    "return NinoFormatError error" when {
      "an invalid nino is supplied" in new Test {
        validator.validate(AmendForeignRawData("A12344A", validTaxYear, validRawRequestBody)) shouldBe
          List(NinoFormatError)
      }
    }

    "return TaxYearFormatError error" when {
      "an invalid tax year is supplied" in new Test {
        validator.validate(AmendForeignRawData(validNino, "20178", validRawRequestBody)) shouldBe
          List(TaxYearFormatError)
      }
    }

    "return RuleTaxYearNotSupported error" when {
      "an invalid tax year is supplied" in new Test {
        validator.validate(AmendForeignRawData(validNino, "2017-18", validRawRequestBody)) shouldBe
          List(RuleTaxYearNotSupportedError)
      }
    }

    "return RuleIncorrectOrEmptyBodyError error" when {
      "an empty JSON body is submitted" in new Test {
        validator.validate(AmendForeignRawData(validNino, validTaxYear, emptyRawRequestBody)) shouldBe
          List(RuleIncorrectOrEmptyBodyError)
      }


      "a non-empty JSON body is submitted without any expected fields" in new Test {
        validator.validate(AmendForeignRawData(validNino, validTaxYear, nonsenseRawRequestBody)) shouldBe
          List(RuleIncorrectOrEmptyBodyError)
      }

      "the submitted request body is not in the correct format" in new Test {
        validator.validate(AmendForeignRawData(validNino, validTaxYear, nonValidRawRequestBody)) shouldBe
          List(RuleIncorrectOrEmptyBodyError.copy(paths = Some(Seq(
            "/unremittableForeignIncome/0/amountTaxPaid",
            "/unremittableForeignIncome/1/amountTaxPaid"
          ))))
      }

      "the submitted request body has missing mandatory fields" in new Test {
        validator.validate(AmendForeignRawData(validNino, validTaxYear, missingMandatoryFieldRequestBody)) shouldBe
          List(RuleIncorrectOrEmptyBodyError.copy(paths = Some(Seq(
            "/unremittableForeignIncome/0/countryCode"
          ))))
      }
    }

    "return CustomerRefFormatError error" when {
      "an incorrectly formatted customer reference is submitted" in new Test {
        validator.validate(AmendForeignRawData(validNino, validTaxYear, invalidCustomerReferenceRequestBody)) shouldBe
          List(CustomerRefFormatError.copy(paths = Some(List("/foreignEarnings/customerReference"))))
      }
    }

    "return ValueFormatError error" when {
      "an incorrectly formatted earningsNotTaxableUK is submitted" in new Test {
        validator.validate(AmendForeignRawData(validNino, validTaxYear, invalidEarningsNotTaxableUKRequestBody)) shouldBe
          List(ValueFormatError.copy(
            paths = Some(List("/foreignEarnings/earningsNotTaxableUK")),
            message = ZERO_MINIMUM_INCLUSIVE
          ))
      }
    }

    "return ValueFormatError error (single failure)" when {
      "one field fails value validation (countryCode 3 digit)" in new Test {
        validator.validate(AmendForeignRawData(validNino, validTaxYear, invalidCountryCodeRequestBody)) shouldBe
          List(CountryCodeRuleError.copy(
            paths = Some(Seq("/unremittableForeignIncome/0/countryCode"))
          ))
      }

      "one field fails value validation (countryCode 4 digit)" in new Test {
        validator.validate(AmendForeignRawData(validNino, validTaxYear, invalidCountryCodeFormatRequestBody)) shouldBe
          List(CountryCodeFormatError.copy(
            paths = Some(Seq("/unremittableForeignIncome/0/countryCode"))
          ))
      }

      "one field fails value validation (amountInForeignCurrency)" in new Test {
        validator.validate(AmendForeignRawData(validNino, validTaxYear, invalidAmountInForeignCurrencyRequestBody)) shouldBe
          List(ValueFormatError.copy(
            message = ZERO_MINIMUM_INCLUSIVE,
            paths = Some(Seq("/unremittableForeignIncome/0/amountInForeignCurrency"))
          ))
      }

      "one field fails value validation (AmountTaxPaid)" in new Test {
        validator.validate(AmendForeignRawData(validNino, validTaxYear, invalidAmountTaxPaidRequestBody)) shouldBe
          List(ValueFormatError.copy(
            message = ZERO_MINIMUM_INCLUSIVE,
            paths = Some(Seq("/unremittableForeignIncome/0/amountTaxPaid"))
          ))
      }
    }

    "return ValueFormatError error (multiple failures)" when {
      "multiple fields fail value validation" in new Test {
        validator.validate(AmendForeignRawData(validNino, validTaxYear, allInvalidValueRawRequestBody)) shouldBe
          List(CountryCodeRuleError.copy(
            paths = Some(Seq("/unremittableForeignIncome/1/countryCode"))
          ),
            ValueFormatError.copy(
              paths = Some(List(
                "/foreignEarnings/earningsNotTaxableUK",
                "/unremittableForeignIncome/0/amountInForeignCurrency",
                "/unremittableForeignIncome/0/amountTaxPaid",
                "/unremittableForeignIncome/1/amountInForeignCurrency",
                "/unremittableForeignIncome/1/amountTaxPaid"
              )),
              message = ZERO_MINIMUM_INCLUSIVE
            ),
            CustomerRefFormatError.copy(
              paths = Some(List("/foreignEarnings/customerReference"))
            ),
            CountryCodeFormatError.copy(
              paths = Some(Seq("/unremittableForeignIncome/0/countryCode"))
            )
          )
      }
    }

    "return multiple errors" when {
      "request supplied has multiple errors (path parameters)" in new Test {
        validator.validate(AmendForeignRawData("A12344A", "20178", emptyRawRequestBody)) shouldBe
          List(NinoFormatError, TaxYearFormatError)
      }
    }
  }
}