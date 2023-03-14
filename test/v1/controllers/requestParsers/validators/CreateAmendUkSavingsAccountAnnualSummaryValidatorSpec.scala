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

import api.controllers.requestParsers.validators.validations.DecimalValueValidation.ZERO_MINIMUM_INCLUSIVE
import api.mocks.MockCurrentDateTime
import api.models.errors._
import config.AppConfig
import mocks.MockAppConfig
import org.joda.time.DateTime
import org.joda.time.format.{DateTimeFormat, DateTimeFormatter}
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.AnyContentAsJson
import support.UnitSpec
import utils.CurrentDateTime
import v1.models.request.createAmendUkSavingsAnnualSummary.CreateAmendUkSavingsAnnualSummaryRawData

class CreateAmendUkSavingsAccountAnnualSummaryValidatorSpec extends UnitSpec with MockAppConfig {

  object Data {
    val validNino              = "AA123456A"
    val validTaxYear           = "2017-18"
    val validTaxedUkInterest   = 31554452289.99
    val validUntaxedUkInterest = 91523009816.00
    val validSavingsAccountId  = "SAVKB2UVwUTBQGJ"

    private val validRequestBodyJson: JsValue = Json.parse(s"""
                                                              |{
                                                              | "taxedUkInterest": $validTaxedUkInterest,
                                                              | "validUntaxedUkInterest": $validUntaxedUkInterest
                                                              |}
                                                              |""".stripMargin)

    private val emptyRequestBodyJson: JsValue = Json.parse("""{}""")

    private val nonsenseRequestBodyJson: JsValue = Json.parse("""{"field": "value"}""")

    private val nonValidRequestBodyJson: JsValue = Json.parse(
      """
        |{
        |  "taxedUkInterest": true,
        |  "validUntaxedUkInterest": false
        |}
    """.stripMargin
    )

    private val invalidTaxedUkInterestJson: JsValue = Json.parse(s"""
                                                                |{
                                                                |  "taxedUkInterest": -1,
                                                                |  "untaxedUkInterest": $validUntaxedUkInterest
                                                                |}
                                                                |""".stripMargin)

    private val invalidUntaxedUkInterestJson: JsValue = Json.parse(s"""
                                                                     |{
                                                                     |  "taxedUkInterest": $validTaxedUkInterest,
                                                                     |  "untaxedUkInterest": -1
                                                                     |}
                                                                     |""".stripMargin)

    val validRawRequestBody: AnyContentAsJson                    = AnyContentAsJson(validRequestBodyJson)
    val emptyRawRequestBody: AnyContentAsJson                    = AnyContentAsJson(emptyRequestBodyJson)
    val nonsenseRawRequestBody: AnyContentAsJson                 = AnyContentAsJson(nonsenseRequestBodyJson)
    val nonValidRawRequestBody: AnyContentAsJson                 = AnyContentAsJson(nonValidRequestBodyJson)
    val invalidTaxedUkInterestRawRequestBody: AnyContentAsJson   = AnyContentAsJson(invalidTaxedUkInterestJson)
    val invalidUntaxedUkInterestRawRequestBody: AnyContentAsJson = AnyContentAsJson(invalidUntaxedUkInterestJson)
  }

  import Data._

  class Test extends MockAppConfig with MockCurrentDateTime {

    implicit val appConfig: AppConfig              = mockAppConfig
    implicit val dateTimeProvider: CurrentDateTime = mockCurrentDateTime
    val dateTimeFormatter: DateTimeFormatter       = DateTimeFormat.forPattern("yyyy-MM-dd")

    val validator = new CreateAmendUkSavingsAccountAnnualSummaryValidator(appConfig: AppConfig)

    MockedAppConfig.ukDividendsMinimumTaxYear
      .returns(2018)
      .anyNumberOfTimes()

    MockCurrentDateTime.getDateTime
      .returns(DateTime.parse("2021-07-29", dateTimeFormatter))
      .anyNumberOfTimes()

  }

  "running validation" should {
    "return no errors" when {
      "passed a valid raw request model" in new Test {
        validator.validate(CreateAmendUkSavingsAnnualSummaryRawData(validNino, validTaxYear, validSavingsAccountId, validRawRequestBody)) shouldBe Nil
      }
    }

    "return NinoFormatError error" when {
      "passed an invalid nino" in new Test {
        validator.validate(CreateAmendUkSavingsAnnualSummaryRawData("A12344A", validTaxYear, validSavingsAccountId, validRawRequestBody)) shouldBe
          List(NinoFormatError)
      }
    }

    "return TaxYearFormatError error" when {
      "passed an invalid taxYear" in new Test {
        validator.validate(CreateAmendUkSavingsAnnualSummaryRawData(validNino, "201920", validSavingsAccountId, validRawRequestBody)) shouldBe
          List(TaxYearFormatError)
      }
    }

    "return SavingsAccountIdFormatError error" when {
      "passed an invalid savingsAccountId" in new Test {
        validator.validate(CreateAmendUkSavingsAnnualSummaryRawData(validNino, validTaxYear, "INVALID-ID", validRawRequestBody)) shouldBe
          List(SavingsAccountIdFormatError)
      }
    }

    "return RuleTaxYearNotSupportedError error" when {
      "an invalid tax year is supplied" in new Test {
        validator.validate(CreateAmendUkSavingsAnnualSummaryRawData(validNino, "2016-17", validSavingsAccountId, validRawRequestBody)) shouldBe
          List(RuleTaxYearNotSupportedError)
      }
    }

    "return RuleTaxYearRangeInvalidError error" when {
      "an invalid tax year is supplied" in new Test {
        validator.validate(CreateAmendUkSavingsAnnualSummaryRawData(validNino, "2019-23", validSavingsAccountId, validRawRequestBody)) shouldBe
          List(RuleTaxYearRangeInvalidError)
      }
    }

    "return RuleIncorrectOrEmptyBodyError error" when {
      "an empty JSON body is submitted" in new Test {
        validator.validate(CreateAmendUkSavingsAnnualSummaryRawData(validNino, validTaxYear, validSavingsAccountId, emptyRawRequestBody)) shouldBe
          List(RuleIncorrectOrEmptyBodyError)
      }

      "a non-empty JSON body is submitted without any expected fields" in new Test {
        validator.validate(CreateAmendUkSavingsAnnualSummaryRawData(validNino, validTaxYear, validSavingsAccountId, nonsenseRawRequestBody)) shouldBe
          List(RuleIncorrectOrEmptyBodyError)

      }

      "the submitted request body is not in the correct format" in new Test {
        validator.validate(CreateAmendUkSavingsAnnualSummaryRawData(validNino, validTaxYear, validSavingsAccountId, nonValidRawRequestBody)) shouldBe
          List(
            RuleIncorrectOrEmptyBodyError.copy(
              paths = Some(
                Seq(
                  "/taxedUkInterest"
                ))
            ))
      }
    }

    "return taxedUkInterestFormatError" when {
      "passed invalid taxedUkInterest" in new Test {
        validator.validate(
          CreateAmendUkSavingsAnnualSummaryRawData(validNino, validTaxYear, validSavingsAccountId, invalidTaxedUkInterestRawRequestBody)) shouldBe
          List(
            ValueFormatError.copy(
              message = ZERO_MINIMUM_INCLUSIVE,
              paths = Some(
                Seq(
                  "/taxedUkInterest"
                ))))
      }
    }
    "return untaxedUkInterestFormatError" when {
      "passed invalid untaxedUkInterest" in new Test {
        validator.validate(
          CreateAmendUkSavingsAnnualSummaryRawData(validNino, validTaxYear, validSavingsAccountId, invalidUntaxedUkInterestRawRequestBody)) shouldBe
          List(
            ValueFormatError.copy(
              message = ZERO_MINIMUM_INCLUSIVE,
              paths = Some(
                Seq(
                  "/untaxedUkInterest"
                ))))
      }
    }

  }

}
