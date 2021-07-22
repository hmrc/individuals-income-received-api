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
import v1.models.errors._
import v1.models.request.createAmendNonPayeEmploymentIncome.CreateAmendNonPayeEmploymentIncomeRawData

class CreateAmendNonPayeEmploymentIncomeValidatorSpec extends UnitSpec with MockAppConfig {

  object Data {
    val validNino    = "AA123456A"
    val validTaxYear = "2019-20"

    val validTips = 100.23

    private val validRequestBodyJson: JsValue = Json.parse(s"""
        |{
        |  "tips": $validTips
        |}
        |""".stripMargin)

    private val emptyRequestBodyJson: JsValue = Json.parse("""{}""")

    private val nonsenseRequestBodyJson: JsValue = Json.parse("""{"field": "value"}""")

    private val nonValidRequestBodyJson: JsValue = Json.parse(
      """
        |{
        |  "tips": true
        |}
    """.stripMargin
    )

    private val invalidTipsJson: JsValue = Json.parse("""
        |{
        |  "tips": 100.234
        |}
        |""".stripMargin)

    val validRawRequestBody: AnyContentAsJson       = AnyContentAsJson(validRequestBodyJson)
    val emptyRawRequestBody: AnyContentAsJson       = AnyContentAsJson(emptyRequestBodyJson)
    val nonsenseRawRequestBody: AnyContentAsJson    = AnyContentAsJson(nonsenseRequestBodyJson)
    val nonValidRawRequestBody: AnyContentAsJson    = AnyContentAsJson(nonValidRequestBodyJson)
    val invalidTipsRawRequestBody: AnyContentAsJson = AnyContentAsJson(invalidTipsJson)
  }

  import Data._

  class Test extends MockAppConfig {

    implicit val appConfig: AppConfig = mockAppConfig

    val validator = new CreateAmendNonPayeEmploymentIncomeValidator(appConfig)

    private val MINIMUM_YEAR = 2020
    MockedAppConfig.minimumPermittedTaxYear returns MINIMUM_YEAR
  }

  "running validation" should {
    "return no errors" when {
      "passed a valid raw request model" in new Test {
        validator.validate(CreateAmendNonPayeEmploymentIncomeRawData(validNino, validTaxYear, validRawRequestBody)) shouldBe Nil
      }
    }

    "return NinoFormatError error" when {
      "passed an invalid nino" in new Test {
        validator.validate(CreateAmendNonPayeEmploymentIncomeRawData("A12344A", validTaxYear, validRawRequestBody)) shouldBe
          List(NinoFormatError)
      }
    }

    "return TaxYearFormatError error" when {
      "passed an invalid taxYear" in new Test {
        validator.validate(CreateAmendNonPayeEmploymentIncomeRawData(validNino, "201920", validRawRequestBody)) shouldBe
          List(TaxYearFormatError)
      }
    }

    "return RuleTaxYearNotSupportedError error" when {
      "an invalid tax year is supplied" in new Test {
        validator.validate(CreateAmendNonPayeEmploymentIncomeRawData(validNino, "2017-18", validRawRequestBody)) shouldBe
          List(RuleTaxYearNotSupportedError)
      }
    }

    "return RuleTaxYearRangeInvalidError error" when {
      "an invalid tax year is supplied" in new Test {
        validator.validate(CreateAmendNonPayeEmploymentIncomeRawData(validNino, "2019-23", validRawRequestBody)) shouldBe
          List(RuleTaxYearRangeInvalidError)
      }
    }

    "return RuleIncorrectOrEmptyBodyError error" when {
      "an empty JSON body is submitted" in new Test {
        validator.validate(CreateAmendNonPayeEmploymentIncomeRawData(validNino, validTaxYear, emptyRawRequestBody)) shouldBe
          List(RuleIncorrectOrEmptyBodyError)
      }

      "a non-empty JSON body is submitted without any expected fields" in new Test {
        validator.validate(CreateAmendNonPayeEmploymentIncomeRawData(validNino, validTaxYear, nonsenseRawRequestBody)) shouldBe
          List(
            RuleIncorrectOrEmptyBodyError.copy(
              paths = Some(Seq("/tips"))
            ))
      }

      "the submitted request body is not in the correct format" in new Test {
        validator.validate(CreateAmendNonPayeEmploymentIncomeRawData(validNino, validTaxYear, nonValidRawRequestBody)) shouldBe
          List(
            RuleIncorrectOrEmptyBodyError.copy(
              paths = Some(Seq(
                "/tips"
              ))
            ))
      }
    }

    "return TipsFormatError" when {
      "passed invalid tips" in new Test {
        validator.validate(CreateAmendNonPayeEmploymentIncomeRawData(validNino, validTaxYear, invalidTipsRawRequestBody)) shouldBe
          List(TipsFormatError)
      }
    }
  }
}
