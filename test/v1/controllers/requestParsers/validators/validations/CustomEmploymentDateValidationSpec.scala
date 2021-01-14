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

package v1.controllers.requestParsers.validators.validations

import support.UnitSpec
import v1.models.errors._

class CustomEmploymentDateValidationSpec extends UnitSpec {

  "CustomEmploymentDateValidation" when {
    "validate" should {

      // valid scenarios
      "return an empty list when only start date is supplied and it is valid" in {
        CustomEmploymentDateValidation.validate(
          startDate = "2020-07-11",
          cessationDate = None,
          taxYear = "2020-21"
        ) shouldBe NoValidationErrors
      }

      "return an empty list when both dates are supplied and are valid" in {
        CustomEmploymentDateValidation.validate(
          startDate = "2020-07-11",
          cessationDate = Some("2020-07-12"),
          taxYear = "2020-21"
        ) shouldBe NoValidationErrors
      }

      // only format error scenarios
      "return multiple format errors when both dates are supplied with the incorrect format" in {
        CustomEmploymentDateValidation.validate(
          startDate = "2020-07-111",
          cessationDate = Some("2020-07-121"),
          taxYear = "2020-21"
        ) shouldBe List(StartDateFormatError, CessationDateFormatError)
      }

      "return only a CessationDateFormatError when cessation date format is incorrect and start date is valid" in {
        CustomEmploymentDateValidation.validate(
          startDate = "2020-07-11",
          cessationDate = Some("2020-07-121"),
          taxYear = "2020-21"
        ) shouldBe List(CessationDateFormatError)
      }

      "return only a StartDateFormatError when start date format is incorrect and cessation date is valid" in {
        CustomEmploymentDateValidation.validate(
          startDate = "2020-07-111",
          cessationDate = Some("2020-07-12"),
          taxYear = "2020-21"
        ) shouldBe List(StartDateFormatError)
      }

      "return only a StartDateFormatError when start date format is incorrect and cessation date is not provided" in {
        CustomEmploymentDateValidation.validate(
          startDate = "2020-07-111",
          cessationDate = None,
          taxYear = "2020-21"
        ) shouldBe List(StartDateFormatError)
      }

      // mixed error scenarios
      "return multiple errors when cessation date format is invalid and start date exceeds the tax year" in {
        CustomEmploymentDateValidation.validate(
          startDate = "2022-07-11",
          cessationDate = Some("2022-07-121"),
          taxYear = "2020-21"
        ) shouldBe List(CessationDateFormatError, RuleStartDateAfterTaxYearEndError)
      }

      "return multiple errors when start date format is invalid and cessation date predates the tax year" in {
        CustomEmploymentDateValidation.validate(
          startDate = "2020-07-111",
          cessationDate = Some("2019-07-12"),
          taxYear = "2020-21"
        ) shouldBe List(StartDateFormatError, RuleCessationDateBeforeTaxYearStartError)
      }

      // non format error scenarios
      "return RuleStartDateAfterTaxYearEndError when cessation date is not provided and start date exceeds the tax year" in {
        CustomEmploymentDateValidation.validate(
          startDate = "2022-07-11",
          cessationDate = None,
          taxYear = "2020-21"
        ) shouldBe List(RuleStartDateAfterTaxYearEndError)
      }

      "return multiple errors when start date exceeds both tax year and cessation date" in {
        CustomEmploymentDateValidation.validate(
          startDate = "2022-07-11",
          cessationDate = Some("2022-07-10"),
          taxYear = "2020-21"
        ) shouldBe List(RuleStartDateAfterTaxYearEndError, RuleCessationDateBeforeStartDateError)
      }

      "return multiple errors when cessation date predates both tax year and start date" in {
        CustomEmploymentDateValidation.validate(
          startDate = "2019-07-11",
          cessationDate = Some("2019-07-10"),
          taxYear = "2020-21"
        ) shouldBe List(RuleCessationDateBeforeTaxYearStartError, RuleCessationDateBeforeStartDateError)
      }
    }
  }
}
