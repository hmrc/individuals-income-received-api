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

package v1r6.controllers.requestParsers.validators.validations

import support.UnitSpec
import v1r6.mocks.MockCurrentDateTime
import v1r6.models.errors.RuleCompletionDateError

import java.time.LocalDate

class CompletionDateValidationSpec extends UnitSpec with MockCurrentDateTime {
  "validate" should {
    "return NoValidationErrors" when {
      "date is after 7th March and before today" in {
        MockCurrentDateTime.getLocalDate.returns(LocalDate.parse("2022-01-01"))
        CompletionDateValidation.validate("2020-03-08", "path", "2019-20")(mockCurrentDateTime) shouldBe NoValidationErrors
      }
      "date is on 7th March and before today" in {
        MockCurrentDateTime.getLocalDate.returns(LocalDate.parse("2022-01-01"))
        CompletionDateValidation.validate("2020-03-07", "path", "2019-20")(mockCurrentDateTime) shouldBe NoValidationErrors
      }
    }
    "return an error" when {
      "date is before 7th March" in {
        MockCurrentDateTime.getLocalDate.returns(LocalDate.parse("2022-01-01"))
        CompletionDateValidation.validate("2020-03-06", "path", "2019-20")(mockCurrentDateTime) shouldBe List(
          RuleCompletionDateError.copy(paths = Some(Seq("path")))
        )
      }
      "date is after 7th March and after today" in {
        MockCurrentDateTime.getLocalDate.returns(LocalDate.parse("2020-01-01"))
        CompletionDateValidation.validate("2020-03-08", "path", "2019-20")(mockCurrentDateTime) shouldBe List(
          RuleCompletionDateError.copy(paths = Some(Seq("path")))
        )
      }
      "date is not in the given tax year (early)" in {
        MockCurrentDateTime.getLocalDate.returns(LocalDate.parse("2022-01-01"))
        CompletionDateValidation.validate("2019-03-08", "path", "2019-20")(mockCurrentDateTime) shouldBe List(
          RuleCompletionDateError.copy(paths = Some(Seq("path")))
        )
      }
      "date is not in the given tax year (late)" in {
        MockCurrentDateTime.getLocalDate.returns(LocalDate.parse("2022-01-01"))
        CompletionDateValidation.validate("2020-05-08", "path", "2019-20")(mockCurrentDateTime) shouldBe List(
          RuleCompletionDateError.copy(paths = Some(Seq("path")))
        )
      }
    }
  }
}
