/*
 * Copyright 2022 HM Revenue & Customs
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

package v1.requestParsers.validators

import api.mocks.MockCurrentDateTime
import api.models.errors.{
  NinoFormatError,
  RuleIncorrectOrEmptyBodyError,
  RuleTaxYearNotSupportedError,
  RuleTaxYearRangeInvalidError,
  TaxYearFormatError,
  ValueFormatError
}
import config.AppConfig
import mocks.MockAppConfig
import org.joda.time.DateTime
import org.joda.time.format.{DateTimeFormat, DateTimeFormatter}
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.AnyContentAsJson
import support.UnitSpec
import utils.CurrentDateTime
import v1.models.request.createAmendUkDividendsIncomeAnnualSummary.CreateAmendUkDividendsIncomeAnnualSummaryRawData

class CreateAmendUkDividendsIncomeAnnualSummaryValidatorSpec extends UnitSpec with MockAppConfig {

  object Data {
    val validNino             = "AA123456A"
    val validTaxYear          = "2019-20"
    val validUkDividends      = 55844806400.99
    val validOtherUkDividends = 60267421355.99

    private val validRequestBodyJson: JsValue = Json.parse(s"""
                                                              |{
                                                              | "ukDividends": $validUkDividends,
                                                              | "otherUkDividends": $validOtherUkDividends
                                                              |}
                                                              |""".stripMargin)

    private val emptyRequestBodyJson: JsValue = Json.parse("""{}""")

    private val nonsenseRequestBodyJson: JsValue = Json.parse("""{"field": "value"}""")

    private val nonValidRequestBodyJson: JsValue = Json.parse(
      """
        |{
        |  "ukDividends": true
        |}
    """.stripMargin
    )

    private val invalidUkDividendsJson: JsValue = Json.parse(s"""
                                                        |{
                                                        |  "ukDividends": -1,
                                                        |  "otherUkDividends": $validOtherUkDividends
                                                        |}
                                                        |""".stripMargin)

    private val invalidOtherUkDividendsJson: JsValue = Json.parse(s"""
                                                                     |{
                                                                     |  "ukDividends": $validUkDividends,
                                                                     |  "otherUkDividends": -1
                                                                     |}
                                                                     |""".stripMargin)

    val validRawRequestBody: AnyContentAsJson                   = AnyContentAsJson(validRequestBodyJson)
    val emptyRawRequestBody: AnyContentAsJson                   = AnyContentAsJson(emptyRequestBodyJson)
    val nonsenseRawRequestBody: AnyContentAsJson                = AnyContentAsJson(nonsenseRequestBodyJson)
    val nonValidRawRequestBody: AnyContentAsJson                = AnyContentAsJson(nonValidRequestBodyJson)
    val invalidUkDividendsRawRequestBody: AnyContentAsJson      = AnyContentAsJson(invalidUkDividendsJson)
    val invalidOtherUkDividendsRawRequestBody: AnyContentAsJson = AnyContentAsJson(invalidOtherUkDividendsJson)
  }

  import Data._

  class Test extends MockAppConfig with MockCurrentDateTime {

    implicit val appConfig: AppConfig              = mockAppConfig
    implicit val dateTimeProvider: CurrentDateTime = mockCurrentDateTime
    val dateTimeFormatter: DateTimeFormatter       = DateTimeFormat.forPattern("yyyy-MM-dd")

    val validator = new CreateAmendUkDividendsIncomeAnnualSummaryValidator(appConfig: AppConfig)

    MockedAppConfig.ukDividendsMinimumTaxYear
      .returns(2018)
      .anyNumberOfTimes()

    MockCurrentDateTime.getDateTime
      .returns(DateTime.parse("2021-07-29", dateTimeFormatter))
      .anyNumberOfTimes()

    private val MINIMUM_YEAR = 2018
    MockedAppConfig.minimumPermittedTaxYear returns MINIMUM_YEAR
  }

  "running validation" should {
    "return no errors" when {
      "passed a valid raw request model" in new Test {
        validator.validate(CreateAmendUkDividendsIncomeAnnualSummaryRawData(validNino, validTaxYear, validRawRequestBody)) shouldBe Nil
      }
    }

    "return NinoFormatError error" when {
      "passed an invalid nino" in new Test {
        validator.validate(CreateAmendUkDividendsIncomeAnnualSummaryRawData("A12344A", validTaxYear, validRawRequestBody)) shouldBe
          List(NinoFormatError)
      }
    }

    "return TaxYearFormatError error" when {
      "passed an invalid taxYear" in new Test {
        validator.validate(CreateAmendUkDividendsIncomeAnnualSummaryRawData(validNino, "201920", validRawRequestBody)) shouldBe
          List(TaxYearFormatError)
      }
    }

    "return RuleTaxYearNotSupportedError error" when {
      "an invalid tax year is supplied" in new Test {
        validator.validate(CreateAmendUkDividendsIncomeAnnualSummaryRawData(validNino, "2016-17", validRawRequestBody)) shouldBe
          List(RuleTaxYearNotSupportedError)
      }
    }

    "return RuleTaxYearRangeInvalidError error" when {
      "an invalid tax year is supplied" in new Test {
        validator.validate(CreateAmendUkDividendsIncomeAnnualSummaryRawData(validNino, "2019-23", validRawRequestBody)) shouldBe
          List(RuleTaxYearRangeInvalidError)
      }
    }

    "return RuleIncorrectOrEmptyBodyError error" when {
      "an empty JSON body is submitted" in new Test {
        validator.validate(CreateAmendUkDividendsIncomeAnnualSummaryRawData(validNino, validTaxYear, emptyRawRequestBody)) shouldBe
          List(RuleIncorrectOrEmptyBodyError)
      }

      "a non-empty JSON body is submitted without any expected fields" in new Test {
        validator.validate(CreateAmendUkDividendsIncomeAnnualSummaryRawData(validNino, validTaxYear, nonsenseRawRequestBody)) shouldBe
          List(RuleIncorrectOrEmptyBodyError)

      }

      "the submitted request body is not in the correct format" in new Test {
        validator.validate(CreateAmendUkDividendsIncomeAnnualSummaryRawData(validNino, validTaxYear, nonValidRawRequestBody)) shouldBe
          List(
            RuleIncorrectOrEmptyBodyError.copy(
              paths = Some(
                Seq(
                  "/ukDividends"
                ))
            ))
      }
    }

    "return ukDividendsFormatError" when {
      "passed invalid ukDividends" in new Test {
        validator.validate(CreateAmendUkDividendsIncomeAnnualSummaryRawData(validNino, validTaxYear, invalidUkDividendsRawRequestBody)) shouldBe
          List(
            ValueFormatError.copy(paths = Some(
              Seq(
                "/ukDividends"
              ))))
      }
    }
    "return otherUkDividendsFormatError" when {
      "passed invalid otherUkDividends" in new Test {
        validator.validate(CreateAmendUkDividendsIncomeAnnualSummaryRawData(validNino, validTaxYear, invalidOtherUkDividendsRawRequestBody)) shouldBe
          List(
            ValueFormatError.copy(paths = Some(
              Seq(
                "/otherUkDividends"
              ))))
      }
    }

  }

}
