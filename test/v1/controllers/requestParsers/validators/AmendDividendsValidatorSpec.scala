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
import v1.models.request.amendDividends.AmendDividendsRawData

class AmendDividendsValidatorSpec extends UnitSpec with ValueFormatErrorMessages {

  private val validNino = "AA123456A"
  private val validTaxYear = "2020-21"

  private val validRequestBodyJson: JsValue = Json.parse(
    """
      |{
      |   "foreignDividend": [
      |      {
      |        "countryCode": "DEU",
      |        "amountBeforeTax": 1232.22,
      |        "taxTakenOff": 22.22,
      |        "specialWithholdingTax": 27.35,
      |        "foreignTaxCreditRelief": true,
      |        "taxableAmount": 2321.22
      |      },
      |      {
      |        "countryCode": "FRA",
      |        "amountBeforeTax": 1350.55,
      |        "taxTakenOff": 25.27,
      |        "specialWithholdingTax": 30.59,
      |        "foreignTaxCreditRelief": false,
      |        "taxableAmount": 2500.99
      |      }
      |   ],
      |   "dividendIncomeReceivedWhilstAbroad": [
      |      {
      |        "countryCode": "DEU",
      |        "amountBeforeTax": 1232.22,
      |        "taxTakenOff": 22.22,
      |        "specialWithholdingTax": 27.35,
      |        "foreignTaxCreditRelief": true,
      |        "taxableAmount": 2321.22
      |      },
      |      {
      |        "countryCode": "FRA",
      |        "amountBeforeTax": 1350.55,
      |        "taxTakenOff": 25.27,
      |        "specialWithholdingTax": 30.59,
      |        "foreignTaxCreditRelief": false,
      |        "taxableAmount": 2500.99
      |       }
      |   ],
      |   "stockDividend": {
      |      "customerReference": "my divs",
      |      "grossAmount": 12321.22
      |   },
      |   "redeemableShares": {
      |      "customerReference": "my shares",
      |      "grossAmount": 12345.75
      |   },
      |   "bonusIssuesOfSecurities": {
      |      "customerReference": "my secs",
      |      "grossAmount": 12500.89
      |   },
      |   "closeCompanyLoansWrittenOff": {
      |      "customerReference": "write off",
      |      "grossAmount": 13700.55
      |   }
      |}
    """.stripMargin
  )

  private val emptyRequestBodyJson: JsValue = Json.parse("""{}""")

  private val nonsenseRequestBodyJson: JsValue = Json.parse("""{"field": "value"}""")

  private val nonValidRequestBodyJson: JsValue = Json.parse(
    """
      |{
      |   "stockDividend": {
      |      "customerReference": "my divs",
      |      "grossAmount": "no"
      |   }
      |}
    """.stripMargin
  )

  private val missingMandatoryFieldJson: JsValue = Json.parse(
    """
      |{
      |   "foreignDividend": [
      |      {
      |        "amountBeforeTax": 1232.22,
      |        "taxTakenOff": 22.22,
      |        "specialWithholdingTax": 27.35
      |      },
      |      {
      |        "amountBeforeTax": 1350.55,
      |        "taxTakenOff": 25.27,
      |        "specialWithholdingTax": 30.59
      |      }
      |   ],
      |   "dividendIncomeReceivedWhilstAbroad": [
      |      {
      |        "amountBeforeTax": 1232.22,
      |        "taxTakenOff": 22.22,
      |        "specialWithholdingTax": 27.35
      |      },
      |      {
      |        "amountBeforeTax": 1350.55,
      |        "taxTakenOff": 25.27,
      |        "specialWithholdingTax": 30.59
      |       }
      |   ],
      |   "stockDividend": {
      |      "customerReference": "my divs"
      |   },
      |   "redeemableShares": {
      |      "customerReference": "my shares"
      |   },
      |   "bonusIssuesOfSecurities": {
      |      "customerReference": "my secs"
      |   },
      |   "closeCompanyLoansWrittenOff": {
      |      "customerReference": "write off"
      |   }
      |}
    """.stripMargin
  )

  private val invalidCountryCodeRequestBodyJson: JsValue = Json.parse(
    """
      |{
      |   "foreignDividend": [
      |      {
      |        "countryCode": "GERMANY",
      |        "amountBeforeTax": 1232.22,
      |        "taxTakenOff": 22.22,
      |        "specialWithholdingTax": 27.35,
      |        "foreignTaxCreditRelief": true,
      |        "taxableAmount": 2321.22
      |      }
      |   ]
      |}
    """.stripMargin
  )

  private val invalidCountryCodeRuleRequestBodyJson: JsValue = Json.parse(
    """
      |{
      |   "dividendIncomeReceivedWhilstAbroad": [
      |      {
      |        "countryCode": "SBT",
      |        "amountBeforeTax": 1232.22,
      |        "taxTakenOff": 22.22,
      |        "specialWithholdingTax": 27.35,
      |        "foreignTaxCreditRelief": true,
      |        "taxableAmount": 2321.22
      |      }
      |   ]
      |}
    """.stripMargin
  )

  private val invalidCustomerRefRequestBodyJson: JsValue = Json.parse(
    """
      |{
      |   "redeemableShares": {
      |      "customerReference": "This customer ref string is 91 characters long ------------------------------------------91",
      |      "grossAmount": 12345.75
      |   }
      |}
    """.stripMargin
  )

  private val invalidForeignDividendRequestBodyJson: JsValue = Json.parse(
    """
      |{
      |   "foreignDividend": [
      |      {
      |        "countryCode": "DEU",
      |        "amountBeforeTax": 1232.223,
      |        "taxTakenOff": 22.22,
      |        "specialWithholdingTax": 27.35,
      |        "foreignTaxCreditRelief": true,
      |        "taxableAmount": 2321.22
      |      }
      |   ]
      |}
    """.stripMargin
  )

  private val invalidDividendIncomeReceivedWhilstAbroadRequestBodyJson: JsValue = Json.parse(
    """
      |{
      |   "dividendIncomeReceivedWhilstAbroad": [
      |      {
      |        "countryCode": "DEU",
      |        "amountBeforeTax": 1232.22,
      |        "taxTakenOff": -22.22,
      |        "specialWithholdingTax": 27.35,
      |        "foreignTaxCreditRelief": true,
      |        "taxableAmount": 2321.22
      |      }
      |   ]
      |}
    """.stripMargin
  )

  private val invalidStockDividendRequestBodyJson: JsValue = Json.parse(
    """
      |{
      |   "stockDividend": {
      |      "customerReference": "my divs",
      |      "grossAmount": 12321.224
      |   }
      |}
    """.stripMargin
  )

  private val invalidRedeemableSharesRequestBodyJson: JsValue = Json.parse(
    """
      |{
      |   "redeemableShares": {
      |      "customerReference": "my shares",
      |      "grossAmount": -12345.75
      |   }
      |}
    """.stripMargin
  )

  private val invalidBonusIssuesOfSecuritiesRequestBodyJson: JsValue = Json.parse(
    """
      |{
      |   "bonusIssuesOfSecurities": {
      |      "customerReference": "my secs",
      |      "grossAmount": 12500.899
      |   }
      |}
    """.stripMargin
  )

  private val invalidCloseCompanyLoansWrittenOffRequestBodyJson: JsValue = Json.parse(
    """
      |{
      |   "closeCompanyLoansWrittenOff": {
      |      "customerReference": "write off",
      |      "grossAmount": -13700.55
      |   }
      |}
    """.stripMargin
  )

  private val allInvalidValueRequestBodyJson: JsValue = Json.parse(
    """
      |{
      |   "foreignDividend": [
      |      {
      |        "countryCode": "GERMANY",
      |        "amountBeforeTax": -1232.22,
      |        "taxTakenOff": 22.223,
      |        "specialWithholdingTax": 27.354,
      |        "foreignTaxCreditRelief": true,
      |        "taxableAmount": -2321.22
      |      },
      |      {
      |        "countryCode": "PUR",
      |        "amountBeforeTax": 1350.559,
      |        "taxTakenOff": 25.278,
      |        "specialWithholdingTax": -30.59,
      |        "foreignTaxCreditRelief": false,
      |        "taxableAmount": -2500.99
      |      }
      |   ],
      |   "dividendIncomeReceivedWhilstAbroad": [
      |      {
      |        "countryCode": "FRANCE",
      |        "amountBeforeTax": 1232.227,
      |        "taxTakenOff": 22.224,
      |        "specialWithholdingTax": 27.358,
      |        "foreignTaxCreditRelief": true,
      |        "taxableAmount": 2321.229
      |      },
      |      {
      |        "countryCode": "SBT",
      |        "amountBeforeTax": -1350.55,
      |        "taxTakenOff": -25.27,
      |        "specialWithholdingTax": -30.59,
      |        "foreignTaxCreditRelief": false,
      |        "taxableAmount": -2500.99
      |       }
      |   ],
      |   "stockDividend": {
      |      "customerReference": "This customer ref string is 91 characters long ------------------------------------------91",
      |      "grossAmount": -12321.22
      |   },
      |   "redeemableShares": {
      |      "customerReference": "This customer ref string is 91 characters long ------------------------------------------91",
      |      "grossAmount": 12345.758
      |   },
      |   "bonusIssuesOfSecurities": {
      |      "customerReference": "This customer ref string is 91 characters long ------------------------------------------91",
      |      "grossAmount": -12500.89
      |   },
      |   "closeCompanyLoansWrittenOff": {
      |      "customerReference": "This customer ref string is 91 characters long ------------------------------------------91",
      |      "grossAmount": 13700.557
      |   }
      |}
    """.stripMargin
  )

  private val validRawRequestBody = AnyContentAsJson(validRequestBodyJson)
  private val emptyRawRequestBody = AnyContentAsJson(emptyRequestBodyJson)
  private val nonsenseRawRequestBody = AnyContentAsJson(nonsenseRequestBodyJson)
  private val nonValidRawRequestBody = AnyContentAsJson(nonValidRequestBodyJson)
  private val missingMandatoryFieldRequestBody = AnyContentAsJson(missingMandatoryFieldJson)
  private val invalidCountryCodeRawRequestBody = AnyContentAsJson(invalidCountryCodeRequestBodyJson)
  private val invalidCountryCodeRuleRawRequestBody = AnyContentAsJson(invalidCountryCodeRuleRequestBodyJson)
  private val invalidCustomerRefRawRequestBody = AnyContentAsJson(invalidCustomerRefRequestBodyJson)
  private val invalidForeignDividendRawRequestBody = AnyContentAsJson(invalidForeignDividendRequestBodyJson)
  private val invalidDividendIncomeReceivedWhilstAbroadRawRequestBody = AnyContentAsJson(invalidDividendIncomeReceivedWhilstAbroadRequestBodyJson)
  private val invalidStockDividendRawRequestBody = AnyContentAsJson(invalidStockDividendRequestBodyJson)
  private val invalidRedeemableSharesRawRequestBody = AnyContentAsJson(invalidRedeemableSharesRequestBodyJson)
  private val invalidBonusIssuesOfSecuritiesRawRequestBody = AnyContentAsJson(invalidBonusIssuesOfSecuritiesRequestBodyJson)
  private val invalidCloseCompanyLoansWrittenOffRawRequestBody = AnyContentAsJson(invalidCloseCompanyLoansWrittenOffRequestBodyJson)
  private val allInvalidValueRawRequestBody = AnyContentAsJson(allInvalidValueRequestBodyJson)

  class Test extends MockAppConfig {
    implicit val appConfig: AppConfig = mockAppConfig
    val validator = new AmendDividendsValidator()

    MockedAppConfig.minimumPermittedTaxYear
      .returns(2021)
      .anyNumberOfTimes()
  }

  "running a validation" should {
    "return no errors" when {
      "a valid request is supplied" in new Test {
        validator.validate(AmendDividendsRawData(validNino, validTaxYear, validRawRequestBody)) shouldBe Nil
      }
    }

    "return NinoFormatError error" when {
      "an invalid nino is supplied" in new Test {
        validator.validate(AmendDividendsRawData("A12344A", validTaxYear, validRawRequestBody)) shouldBe
          List(NinoFormatError)
      }
    }

    "return TaxYearFormatError error" when {
      "an invalid tax year is supplied" in new Test {
        validator.validate(AmendDividendsRawData(validNino, "20178", validRawRequestBody)) shouldBe
          List(TaxYearFormatError)
      }
    }

    "return RuleIncorrectOrEmptyBodyError error" when {
      "an empty JSON body is submitted" in new Test {
        validator.validate(AmendDividendsRawData(validNino, validTaxYear, emptyRawRequestBody)) shouldBe
          List(RuleIncorrectOrEmptyBodyError)
      }


      "a non-empty JSON body is submitted without any expected fields" in new Test {
        validator.validate(AmendDividendsRawData(validNino, validTaxYear, nonsenseRawRequestBody)) shouldBe
          List(RuleIncorrectOrEmptyBodyError)
      }

      "the submitted request body is not in the correct format" in new Test {
        validator.validate(AmendDividendsRawData(validNino, validTaxYear, nonValidRawRequestBody)) shouldBe
          List(RuleIncorrectOrEmptyBodyError.copy(paths = Some(Seq("/stockDividend/grossAmount"))))
      }

      "return RuleTaxYearRangeInvalidError error" when {
        "an invalid tax year range is supplied" in new Test {
          validator.validate(AmendDividendsRawData(validNino, "2019-21", validRawRequestBody)) shouldBe
            List(RuleTaxYearRangeInvalidError)
        }
      }

      "return RuleTaxYearNotSupportedError error" when {
        "an invalid tax year is supplied" in new Test {
          validator.validate(AmendDividendsRawData(validNino, "2018-19", validRawRequestBody)) shouldBe
            List(RuleTaxYearNotSupportedError)
        }
      }

      "mandatory fields are not provided" in new Test {
        val paths = Seq(
          "/foreignDividend/0/countryCode",
          "/dividendIncomeReceivedWhilstAbroad/1/foreignTaxCreditRelief",
          "/foreignDividend/0/foreignTaxCreditRelief",
          "/dividendIncomeReceivedWhilstAbroad/1/countryCode",
          "/foreignDividend/1/foreignTaxCreditRelief",
          "/dividendIncomeReceivedWhilstAbroad/0/countryCode",
          "/stockDividend/grossAmount",
          "/closeCompanyLoansWrittenOff/grossAmount",
          "/dividendIncomeReceivedWhilstAbroad/0/taxableAmount",
          "/bonusIssuesOfSecurities/grossAmount",
          "/redeemableShares/grossAmount",
          "/dividendIncomeReceivedWhilstAbroad/1/taxableAmount",
          "/dividendIncomeReceivedWhilstAbroad/0/foreignTaxCreditRelief",
          "/foreignDividend/1/countryCode",
          "/foreignDividend/1/taxableAmount",
          "/foreignDividend/0/taxableAmount"
        )

        validator.validate(AmendDividendsRawData(validNino, validTaxYear, missingMandatoryFieldRequestBody)) shouldBe
          List(RuleIncorrectOrEmptyBodyError.copy(paths = Some(paths)))
      }
    }

    "return CountryCodeFormatError error" when {
      "an incorrectly formatted country code is submitted" in new Test {
        validator.validate(AmendDividendsRawData(validNino, validTaxYear, invalidCountryCodeRawRequestBody)) shouldBe
          List(CountryCodeFormatError.copy(paths = Some(List("/foreignDividend/0/countryCode"))))
      }
    }

    "return CountryCodeRuleError error" when {
      "an invalid country code is submitted" in new Test {
        validator.validate(AmendDividendsRawData(validNino, validTaxYear, invalidCountryCodeRuleRawRequestBody)) shouldBe
          List(CountryCodeRuleError.copy(paths = Some(List("/dividendIncomeReceivedWhilstAbroad/0/countryCode"))))
      }
    }

    "return CustomerRefFormatError error" when {
      "an incorrectly formatted customer reference is submitted" in new Test {
        validator.validate(AmendDividendsRawData(validNino, validTaxYear, invalidCustomerRefRawRequestBody)) shouldBe
          List(CustomerRefFormatError.copy(paths = Some(List("/redeemableShares/customerReference"))))
      }
    }

    "return ValueFormatError error (single failure)" when {
      "one field fails value validation (foreign dividend)" in new Test {
        validator.validate(AmendDividendsRawData(validNino, validTaxYear, invalidForeignDividendRawRequestBody)) shouldBe
          List(ValueFormatError.copy(
            message = ZERO_MINIMUM_INCLUSIVE,
            paths = Some(Seq("/foreignDividend/0/amountBeforeTax"))
          ))
      }

      "one field fails value validation (dividend income received whilst abroad)" in new Test {
        validator.validate(AmendDividendsRawData(validNino, validTaxYear, invalidDividendIncomeReceivedWhilstAbroadRawRequestBody)) shouldBe
          List(ValueFormatError.copy(
            message = ZERO_MINIMUM_INCLUSIVE,
            paths = Some(Seq("/dividendIncomeReceivedWhilstAbroad/0/taxTakenOff"))
          ))
      }

      "one field fails value validation (stock dividend)" in new Test {
        validator.validate(AmendDividendsRawData(validNino, validTaxYear, invalidStockDividendRawRequestBody)) shouldBe
          List(ValueFormatError.copy(
            message = ZERO_MINIMUM_INCLUSIVE,
            paths = Some(Seq("/stockDividend/grossAmount"))
          ))
      }

      "one field fails value validation (redeemable shares)" in new Test {
        validator.validate(AmendDividendsRawData(validNino, validTaxYear, invalidRedeemableSharesRawRequestBody)) shouldBe
          List(ValueFormatError.copy(
            message = ZERO_MINIMUM_INCLUSIVE,
            paths = Some(Seq("/redeemableShares/grossAmount"))
          ))
      }

      "one field fails value validation (bonus issues of securities)" in new Test {
        validator.validate(AmendDividendsRawData(validNino, validTaxYear, invalidBonusIssuesOfSecuritiesRawRequestBody)) shouldBe
          List(ValueFormatError.copy(
            message = ZERO_MINIMUM_INCLUSIVE,
            paths = Some(Seq("/bonusIssuesOfSecurities/grossAmount"))
          ))
      }

      "one field fails value validation (close company loans written off)" in new Test {
        validator.validate(AmendDividendsRawData(validNino, validTaxYear, invalidCloseCompanyLoansWrittenOffRawRequestBody)) shouldBe
          List(ValueFormatError.copy(
            message = ZERO_MINIMUM_INCLUSIVE,
            paths = Some(Seq("/closeCompanyLoansWrittenOff/grossAmount"))
          ))
      }
    }

    "return ValueFormatError error (multiple failures)" when {
      "multiple fields fail value validation" in new Test {
        validator.validate(AmendDividendsRawData(validNino, validTaxYear, allInvalidValueRawRequestBody)) shouldBe
          List(
            CountryCodeRuleError.copy(
              paths = Some(List(
                "/foreignDividend/1/countryCode",
                "/dividendIncomeReceivedWhilstAbroad/1/countryCode"
              ))
            ),
            ValueFormatError.copy(
              message = ZERO_MINIMUM_INCLUSIVE,
              paths = Some(List(
                "/foreignDividend/0/amountBeforeTax",
                "/foreignDividend/0/taxTakenOff",
                "/foreignDividend/0/specialWithholdingTax",
                "/foreignDividend/0/taxableAmount",
                "/foreignDividend/1/amountBeforeTax",
                "/foreignDividend/1/taxTakenOff",
                "/foreignDividend/1/specialWithholdingTax",
                "/foreignDividend/1/taxableAmount",
                "/dividendIncomeReceivedWhilstAbroad/0/amountBeforeTax",
                "/dividendIncomeReceivedWhilstAbroad/0/taxTakenOff",
                "/dividendIncomeReceivedWhilstAbroad/0/specialWithholdingTax",
                "/dividendIncomeReceivedWhilstAbroad/0/taxableAmount",
                "/dividendIncomeReceivedWhilstAbroad/1/amountBeforeTax",
                "/dividendIncomeReceivedWhilstAbroad/1/taxTakenOff",
                "/dividendIncomeReceivedWhilstAbroad/1/specialWithholdingTax",
                "/dividendIncomeReceivedWhilstAbroad/1/taxableAmount",
                "/stockDividend/grossAmount",
                "/redeemableShares/grossAmount",
                "/bonusIssuesOfSecurities/grossAmount",
                "/closeCompanyLoansWrittenOff/grossAmount"
              ))
            ),
            CustomerRefFormatError.copy(
              paths = Some(List(
                "/stockDividend/customerReference",
                "/redeemableShares/customerReference",
                "/bonusIssuesOfSecurities/customerReference",
                "/closeCompanyLoansWrittenOff/customerReference"
              ))
            ),
            CountryCodeFormatError.copy(
              paths = Some(List(
                "/foreignDividend/0/countryCode",
                "/dividendIncomeReceivedWhilstAbroad/0/countryCode"
              ))
            )
          )
      }
    }

    "return multiple errors" when {
      "request supplied has multiple errors (path parameters)" in new Test {
        validator.validate(AmendDividendsRawData("A12344A", "20178", emptyRawRequestBody)) shouldBe
          List(NinoFormatError, TaxYearFormatError)
      }
    }
  }
}