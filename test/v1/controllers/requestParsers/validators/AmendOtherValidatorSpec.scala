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
import v1.models.request.amendOther.AmendOtherRawData

class AmendOtherValidatorSpec extends UnitSpec with ValueFormatErrorMessages with MockAppConfig {

  private val validNino = "AA123456A"
  private val validTaxYear = "2019-20"

  private val validRequestBodyJson: JsValue = Json.parse(
    """
      |{
      |   "businessReceipts": [
      |      {
      |         "grossAmount": 5000.99,
      |         "taxYear": "2018-19"
      |      },
      |      {
      |         "grossAmount": 6000.99,
      |         "taxYear": "2019-20"
      |      }
      |   ],
      |   "allOtherIncomeReceivedWhilstAbroad": [
      |      {
      |         "countryCode": "FRA",
      |         "amountBeforeTax": 1999.99,
      |         "taxTakenOff": 2.23,
      |         "specialWithholdingTax": 3.23,
      |         "foreignTaxCreditRelief": false,
      |         "taxableAmount": 4.23,
      |         "residentialFinancialCostAmount": 2999.99,
      |         "broughtFwdResidentialFinancialCostAmount": 1999.99
      |      },
      |      {
      |         "countryCode": "IND",
      |         "amountBeforeTax": 2999.99,
      |         "taxTakenOff": 3.23,
      |         "specialWithholdingTax": 4.23,
      |         "foreignTaxCreditRelief": true,
      |         "taxableAmount": 5.23,
      |         "residentialFinancialCostAmount": 3999.99,
      |         "broughtFwdResidentialFinancialCostAmount": 2999.99
      |      }
      |   ],
      |   "overseasIncomeAndGains": {
      |      "gainAmount": 3000.99
      |   },
      |   "chargeableForeignBenefitsAndGifts": {
      |      "transactionBenefit": 1999.99,
      |      "protectedForeignIncomeSourceBenefit": 2999.99,
      |      "protectedForeignIncomeOnwardGift": 3999.99,
      |      "benefitReceivedAsASettler": 4999.99,
      |      "onwardGiftReceivedAsASettler": 5999.99
      |   },
      |   "omittedForeignIncome": {
      |      "amount": 4000.99
      |   }
      |}
    """.stripMargin
  )

  private val emptyRequestBodyJson: JsValue = Json.parse("""{}""")

  private val nonsenseRequestBodyJson: JsValue = Json.parse("""{"field": "value"}""")

  private val nonValidRequestBodyJson: JsValue = Json.parse(
    """
      |{
      |   "overseasIncomeAndGains": {
      |      "gainAmount": "no"
      |   }
      |}
    """.stripMargin
  )

  private val missingMandatoryFieldJson: JsValue = Json.parse(
    """
      |{
      |  "allOtherIncomeReceivedWhilstAbroad": [{}]
      |}
    """.stripMargin
  )

  private val invalidCountryCodeRequestBodyJson: JsValue = Json.parse(
    """
      |{
      |   "allOtherIncomeReceivedWhilstAbroad": [
      |      {
      |         "countryCode": "FRANCE",
      |         "amountBeforeTax": 1999.99,
      |         "taxTakenOff": 2.23,
      |         "specialWithholdingTax": 3.23,
      |         "foreignTaxCreditRelief": false,
      |         "taxableAmount": 4.23,
      |         "residentialFinancialCostAmount": 2999.99,
      |         "broughtFwdResidentialFinancialCostAmount": 1999.99
      |      }
      |   ]
      |}
    """.stripMargin
  )

  private val invalidCountryCodeRuleRequestBodyJson: JsValue = Json.parse(
    """
      |{
      |   "allOtherIncomeReceivedWhilstAbroad": [
      |      {
      |         "countryCode": "PUR",
      |         "amountBeforeTax": 1999.99,
      |         "taxTakenOff": 2.23,
      |         "specialWithholdingTax": 3.23,
      |         "foreignTaxCreditRelief": false,
      |         "taxableAmount": 4.23,
      |         "residentialFinancialCostAmount": 2999.99,
      |         "broughtFwdResidentialFinancialCostAmount": 1999.99
      |      }
      |   ]
      |}
    """.stripMargin
  )

  private val invalidTaxYearRequestBodyJson: JsValue = Json.parse(
    """
      |{
      |   "businessReceipts": [
      |      {
      |         "grossAmount": 5000.99,
      |         "taxYear": "2019"
      |      }
      |   ]
      |}
    """.stripMargin
  )

  private val invalidTaxYearRangeRuleRequestBodyJson: JsValue = Json.parse(
    """
      |{
      |   "businessReceipts": [
      |      {
      |         "grossAmount": 5000.99,
      |         "taxYear": "2019-21"
      |      }
      |   ]
      |}
    """.stripMargin
  )

  private val invalidBusinessReceiptsRequestBodyJson: JsValue = Json.parse(
    """
      |{
      |   "businessReceipts": [
      |      {
      |         "grossAmount": 5000.999,
      |         "taxYear": "2019-20"
      |      }
      |   ]
      |}
    """.stripMargin
  )


  private val invalidAllOtherIncomeReceivedWhilstAbroadRequestBodyJson: JsValue = Json.parse(
    """
      |{
      |   "allOtherIncomeReceivedWhilstAbroad": [
      |      {
      |         "countryCode": "FRA",
      |         "amountBeforeTax": 1999.999,
      |         "taxTakenOff": 2.23,
      |         "specialWithholdingTax": 3.23,
      |         "foreignTaxCreditRelief": false,
      |         "taxableAmount": 4.23,
      |         "residentialFinancialCostAmount": 2999.99,
      |         "broughtFwdResidentialFinancialCostAmount": 1999.99
      |      }
      |   ]
      |}
    """.stripMargin
  )

  private val invalidOverseasIncomeAndGainsRequestBodyJson: JsValue = Json.parse(
    """
      |{
      |   "overseasIncomeAndGains": {
      |      "gainAmount": 3000.999
      |   }
      |}
    """.stripMargin
  )

  private val invalidChargeableForeignBenefitsAndGiftsRequestBodyJson: JsValue = Json.parse(
    """
      |{
      |   "chargeableForeignBenefitsAndGifts": {
      |      "transactionBenefit": 1999.999,
      |      "protectedForeignIncomeSourceBenefit": 2999.99,
      |      "protectedForeignIncomeOnwardGift": 3999.99,
      |      "benefitReceivedAsASettler": 4999.99,
      |      "onwardGiftReceivedAsASettler": 5999.99
      |   }
      |}
    """.stripMargin
  )

  private val invalidOmittedForeignIncomeRequestBodyJson: JsValue = Json.parse(
    """
      |{
      |   "omittedForeignIncome": {
      |      "amount": -4000.99
      |   }
      |}
    """.stripMargin
  )

  private val allInvalidValueRequestBodyJson: JsValue = Json.parse(
    """
      |{
      |   "businessReceipts": [
      |      {
      |         "grossAmount": 5000.999,
      |         "taxYear": "2019"
      |      },
      |      {
      |         "grossAmount": 6000.999,
      |         "taxYear": "2019-21"
      |      }
      |   ],
      |   "allOtherIncomeReceivedWhilstAbroad": [
      |      {
      |         "countryCode": "FRANCE",
      |         "amountBeforeTax": -1999.99,
      |         "taxTakenOff": -2.23,
      |         "specialWithholdingTax": 3.233,
      |         "foreignTaxCreditRelief": false,
      |         "taxableAmount": 4.233,
      |         "residentialFinancialCostAmount": -2999.99,
      |         "broughtFwdResidentialFinancialCostAmount": 1999.995
      |      },
      |      {
      |         "countryCode": "SBT",
      |         "amountBeforeTax": -2999.99,
      |         "taxTakenOff": -3.23,
      |         "specialWithholdingTax": 4.235,
      |         "foreignTaxCreditRelief": true,
      |         "taxableAmount": 5.253,
      |         "residentialFinancialCostAmount": 3999.959,
      |         "broughtFwdResidentialFinancialCostAmount": -2999.99
      |      }
      |   ],
      |   "overseasIncomeAndGains": {
      |      "gainAmount": 3000.993
      |   },
      |   "chargeableForeignBenefitsAndGifts": {
      |      "transactionBenefit": 1999.992,
      |      "protectedForeignIncomeSourceBenefit": 2999.999,
      |      "protectedForeignIncomeOnwardGift": -3999.99,
      |      "benefitReceivedAsASettler": -4999.99,
      |      "onwardGiftReceivedAsASettler": 5999.996
      |   },
      |   "omittedForeignIncome": {
      |      "amount": -4000.99
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
  private val invalidTaxYearRawRequestBody = AnyContentAsJson(invalidTaxYearRequestBodyJson)
  private val invalidTaxYearRangeRuleRawRequestBody = AnyContentAsJson(invalidTaxYearRangeRuleRequestBodyJson)
  private val invalidBusinessReceiptsRawRequestBody = AnyContentAsJson(invalidBusinessReceiptsRequestBodyJson)
  private val invalidAllOtherIncomeReceivedWhilstAbroadRawRequestBody = AnyContentAsJson(invalidAllOtherIncomeReceivedWhilstAbroadRequestBodyJson)
  private val invalidOverseasIncomeAndGainsRawRequestBody = AnyContentAsJson(invalidOverseasIncomeAndGainsRequestBodyJson)
  private val invalidChargeableForeignBenefitsAndGiftsRawRequestBody = AnyContentAsJson(invalidChargeableForeignBenefitsAndGiftsRequestBodyJson)
  private val invalidOmittedForeignIncomeRawRequestBody = AnyContentAsJson(invalidOmittedForeignIncomeRequestBodyJson)
  private val allInvalidValueRawRequestBody = AnyContentAsJson(allInvalidValueRequestBodyJson)

  implicit val appConfig: AppConfig = mockAppConfig
  val validator = new AmendOtherValidator()

  class Test extends MockAppConfig {

    implicit val appConfig: AppConfig = mockAppConfig

    val validator = new AmendOtherValidator()

    MockedAppConfig.minimumPermittedTaxYear
      .returns(2020)
  }

  "running a validation" should {
    "return no errors" when {
      "a valid request is supplied" in new Test {
        validator.validate(AmendOtherRawData(validNino, validTaxYear, validRawRequestBody)) shouldBe Nil
      }
    }

    "return NinoFormatError error" when {
      "an invalid nino is supplied" in new Test {
        validator.validate(AmendOtherRawData("A12344A", validTaxYear, validRawRequestBody)) shouldBe
          List(NinoFormatError)
      }
    }

    "return TaxYearFormatError error" when {
      "an invalid tax year is supplied" in new Test {
        validator.validate(AmendOtherRawData(validNino, "20178", validRawRequestBody)) shouldBe
          List(TaxYearFormatError)
      }
    }

    "return RuleTaxYearNotSupportedError error" when {
      "an invalid tax year is supplied" in new Test {
        validator.validate(AmendOtherRawData(validNino, "2017-18", validRawRequestBody)) shouldBe
          List(RuleTaxYearNotSupportedError)
      }
    }

    "return RuleIncorrectOrEmptyBodyError error" when {
      "an empty JSON body is submitted" in new Test {
        validator.validate(AmendOtherRawData(validNino, validTaxYear, emptyRawRequestBody)) shouldBe
          List(RuleIncorrectOrEmptyBodyError)
      }

      "a non-empty JSON body is submitted without any expected fields" in new Test {
        validator.validate(AmendOtherRawData(validNino, validTaxYear, nonsenseRawRequestBody)) shouldBe
          List(RuleIncorrectOrEmptyBodyError)
      }

      "the submitted request body is not in the correct format" in new Test {
        validator.validate(AmendOtherRawData(validNino, validTaxYear, nonValidRawRequestBody)) shouldBe
          List(RuleIncorrectOrEmptyBodyError.copy(paths = Some(Seq("/overseasIncomeAndGains/gainAmount"))))
      }

      "the submitted request body has missing mandatory fields" in new Test {
        validator.validate(AmendOtherRawData(validNino, validTaxYear, missingMandatoryFieldRequestBody)) shouldBe
          List(RuleIncorrectOrEmptyBodyError.copy(paths = Some(Seq(
            "/allOtherIncomeReceivedWhilstAbroad/0/taxableAmount",
            "/allOtherIncomeReceivedWhilstAbroad/0/foreignTaxCreditRelief",
            "/allOtherIncomeReceivedWhilstAbroad/0/countryCode"
          ))))
      }
    }

    "return CountryCodeFormatError error" when {
      "an incorrectly formatted country code is submitted" in new Test {
        validator.validate(AmendOtherRawData(validNino, validTaxYear, invalidCountryCodeRawRequestBody)) shouldBe
          List(CountryCodeFormatError.copy(paths = Some(List("/allOtherIncomeReceivedWhilstAbroad/0/countryCode"))))
      }
    }

    "return CountryCodeRuleError error" when {
      "an invalid country code is submitted" in new Test {
        validator.validate(AmendOtherRawData(validNino, validTaxYear, invalidCountryCodeRuleRawRequestBody)) shouldBe
          List(CountryCodeRuleError.copy(paths = Some(List("/allOtherIncomeReceivedWhilstAbroad/0/countryCode"))))
      }
    }

    "return TaxYearFormatError error" when {
      "an incorrectly formatted tax year is submitted in the request body" in new Test {
        validator.validate(AmendOtherRawData(validNino, validTaxYear, invalidTaxYearRawRequestBody)) shouldBe
          List(TaxYearFormatError.copy(paths = Some(List("/businessReceipts/0/taxYear"))))
      }
    }

    "return RuleTaxYearRangeInvalidError error" when {
      "an invalid tax year range is submitted in the request body" in new Test {
        validator.validate(AmendOtherRawData(validNino, validTaxYear, invalidTaxYearRangeRuleRawRequestBody)) shouldBe
          List(RuleTaxYearRangeInvalidError.copy(paths = Some(List("/businessReceipts/0/taxYear"))))
      }
    }

    "return ValueFormatError error (single failure)" when {
      "one field fails value validation (business receipts)" in new Test {
        validator.validate(AmendOtherRawData(validNino, validTaxYear, invalidBusinessReceiptsRawRequestBody)) shouldBe
          List(ValueFormatError.copy(
            message = ZERO_MINIMUM_INCLUSIVE,
            paths = Some(Seq("/businessReceipts/0/grossAmount"))
          ))
      }

      "one field fails value validation (all other income received whilst abroad)" in new Test {
        validator.validate(AmendOtherRawData(validNino, validTaxYear, invalidAllOtherIncomeReceivedWhilstAbroadRawRequestBody)) shouldBe
          List(ValueFormatError.copy(
            message = ZERO_MINIMUM_INCLUSIVE,
            paths = Some(Seq("/allOtherIncomeReceivedWhilstAbroad/0/amountBeforeTax"))
          ))
      }

      "one field fails value validation (overseas income and gains)" in new Test {
        validator.validate(AmendOtherRawData(validNino, validTaxYear, invalidOverseasIncomeAndGainsRawRequestBody)) shouldBe
          List(ValueFormatError.copy(
            message = ZERO_MINIMUM_INCLUSIVE,
            paths = Some(Seq("/overseasIncomeAndGains/gainAmount"))
          ))
      }

      "one field fails value validation (chargeable foreign benefits and gifts)" in new Test {
        validator.validate(AmendOtherRawData(validNino, validTaxYear, invalidChargeableForeignBenefitsAndGiftsRawRequestBody)) shouldBe
          List(ValueFormatError.copy(
            message = ZERO_MINIMUM_INCLUSIVE,
            paths = Some(Seq("/chargeableForeignBenefitsAndGifts/transactionBenefit"))
          ))
      }


      "one field fails value validation (omitted foreign income)" in new Test {
        validator.validate(AmendOtherRawData(validNino, validTaxYear, invalidOmittedForeignIncomeRawRequestBody)) shouldBe
          List(ValueFormatError.copy(
            message = ZERO_MINIMUM_INCLUSIVE,
            paths = Some(Seq("/omittedForeignIncome/amount"))
          ))
      }
    }

    "return ValueFormatError error (multiple failures)" when {
      "multiple fields fail value validation" in new Test {
        validator.validate(AmendOtherRawData(validNino, validTaxYear, allInvalidValueRawRequestBody)) shouldBe
          List(
            TaxYearFormatError.copy(
              paths = Some(List(
                "/businessReceipts/0/taxYear"
              ))
            ),
            CountryCodeRuleError.copy(
              paths = Some(List(
                "/allOtherIncomeReceivedWhilstAbroad/1/countryCode"
              ))
            ),
            RuleTaxYearRangeInvalidError.copy(
              paths = Some(List(
                "/businessReceipts/1/taxYear"
              ))
            ),
            CountryCodeFormatError.copy(
              paths = Some(List(
                "/allOtherIncomeReceivedWhilstAbroad/0/countryCode"
              ))
            ),
            ValueFormatError.copy(
              message = ZERO_MINIMUM_INCLUSIVE,
              paths = Some(List(
                "/businessReceipts/0/grossAmount",
                "/businessReceipts/1/grossAmount",
                "/allOtherIncomeReceivedWhilstAbroad/0/amountBeforeTax",
                "/allOtherIncomeReceivedWhilstAbroad/0/taxTakenOff",
                "/allOtherIncomeReceivedWhilstAbroad/0/specialWithholdingTax",
                "/allOtherIncomeReceivedWhilstAbroad/0/taxableAmount",
                "/allOtherIncomeReceivedWhilstAbroad/0/residentialFinancialCostAmount",
                "/allOtherIncomeReceivedWhilstAbroad/0/broughtFwdResidentialFinancialCostAmount",
                "/allOtherIncomeReceivedWhilstAbroad/1/amountBeforeTax",
                "/allOtherIncomeReceivedWhilstAbroad/1/taxTakenOff",
                "/allOtherIncomeReceivedWhilstAbroad/1/specialWithholdingTax",
                "/allOtherIncomeReceivedWhilstAbroad/1/taxableAmount",
                "/allOtherIncomeReceivedWhilstAbroad/1/residentialFinancialCostAmount",
                "/allOtherIncomeReceivedWhilstAbroad/1/broughtFwdResidentialFinancialCostAmount",
                "/overseasIncomeAndGains/gainAmount",
                "/chargeableForeignBenefitsAndGifts/transactionBenefit",
                "/chargeableForeignBenefitsAndGifts/protectedForeignIncomeSourceBenefit",
                "/chargeableForeignBenefitsAndGifts/protectedForeignIncomeOnwardGift",
                "/chargeableForeignBenefitsAndGifts/benefitReceivedAsASettler",
                "/chargeableForeignBenefitsAndGifts/onwardGiftReceivedAsASettler",
                "/omittedForeignIncome/amount"
              ))
            )
          )
      }
    }

    "return multiple errors" when {
      "request supplied has multiple errors (path parameters)" in new Test {
        validator.validate(AmendOtherRawData("A12344A", "20178", emptyRawRequestBody)) shouldBe
          List(NinoFormatError, TaxYearFormatError)
      }
    }
  }
}
