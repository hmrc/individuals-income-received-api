/*
 * Copyright 2020 HM Revenue & Customs
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

import org.joda.time.DateTime
import org.joda.time.format.{DateTimeFormat, DateTimeFormatter}
import support.UnitSpec
import utils.CurrentDateTime
import v1.mocks.MockCurrentDateTime
import v1.models.errors.{RuleTaxYearNotEndedError, RuleTaxYearNotSupportedError}
import v1.models.utils.JsonErrorValidators

class MtdTaxYearValidationSpec extends UnitSpec with JsonErrorValidators with MockCurrentDateTime {

  private implicit val dateTimeProvider: CurrentDateTime = mockCurrentDateTime
  private val dateTimeFormatter: DateTimeFormatter = DateTimeFormat.forPattern("yyyy-MM-dd")

  "validate" should {
    "return no errors" when {
      "a tax year after 2020-21 is supplied" in {

        MockCurrentDateTime.getCurrentDate.returns(DateTime.parse("2022-04-06", dateTimeFormatter))

        val validTaxYear = "2021-22"
        val validationResult = MtdTaxYearValidation.validate(validTaxYear)
        validationResult.isEmpty shouldBe true
      }

      "the minimum allowed tax year is supplied" in {

        MockCurrentDateTime.getCurrentDate.returns(DateTime.parse("2022-04-06", dateTimeFormatter))

        val validTaxYear = "2020-21"
        val validationResult = MtdTaxYearValidation.validate(validTaxYear)
        validationResult.isEmpty shouldBe true
      }

    }

    "return the given error" when {
      "a tax year before 2020-21 is supplied" in {

        MockCurrentDateTime.getCurrentDate.returns(DateTime.parse("2022-04-06", dateTimeFormatter))

        val invalidTaxYear = "2019-20"
        val validationResult = MtdTaxYearValidation.validate(invalidTaxYear)
        validationResult.isEmpty shouldBe false
        validationResult.length shouldBe 1
        validationResult.head shouldBe RuleTaxYearNotSupportedError
      }

      "the supplied tax year has not yet ended" in {

        MockCurrentDateTime.getCurrentDate.returns(DateTime.parse("2022-04-04", dateTimeFormatter))

        val invalidTaxYear = "2021-22"
        val validationResult = MtdTaxYearValidation.validate(invalidTaxYear)
        validationResult.isEmpty shouldBe false
        validationResult.length shouldBe 1
        validationResult.head shouldBe RuleTaxYearNotEndedError
      }
    }
  }
}
