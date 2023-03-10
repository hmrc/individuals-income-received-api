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

package api.controllers.requestParsers.validators.validations

import api.mocks.MockCurrentDateTime
import api.models.errors.RuleCompletionDateError
import support.UnitSpec

import java.time.LocalDate

class CompletionDateValidationSpec extends UnitSpec with MockCurrentDateTime {

  def stubNow(date: String): Unit = {
    MockCurrentDateTime.getLocalDate.returns(LocalDate.parse(date)).anyNumberOfTimes()
  }

  "validate" should {
    "return NoValidationErrors" when {
      "date is after 7th March and before today in the current tax year" in {
        stubNow("2020-04-01")
        CompletionDateValidation.validate("2020-03-08", "path", "2019-20")(mockCurrentDateTime) shouldBe NoValidationErrors
      }
      "date is on 7th March and before today in the current tax year" in {
        stubNow("2020-04-01")
        CompletionDateValidation.validate("2020-03-07", "path", "2019-20")(mockCurrentDateTime) shouldBe NoValidationErrors
      }
      "date is on 5th April in the current tax year" in {
        stubNow("2020-04-05")
        CompletionDateValidation.validate("2020-04-05", "path", "2019-20")(mockCurrentDateTime) shouldBe NoValidationErrors
      }
      "date is in the given (ended) tax year" in {
        stubNow("2020-05-01")
        CompletionDateValidation.validate("2020-03-06", "path", "2019-20")(mockCurrentDateTime) shouldBe NoValidationErrors
      }
      "date is at the start of the given (ended) tax year" in {
        stubNow("2020-05-01")
        CompletionDateValidation.validate("2019-04-06", "path", "2019-20")(mockCurrentDateTime) shouldBe NoValidationErrors
      }
      "date is at the end of the given (ended) tax year" in {
        stubNow("2020-05-01")
        CompletionDateValidation.validate("2020-04-05", "path", "2019-20")(mockCurrentDateTime) shouldBe NoValidationErrors
      }
    }
    "return an error" when {
      "date is before 7th March in the current tax year" in {
        stubNow("2020-04-01")
        CompletionDateValidation.validate("2020-03-06", "path", "2019-20")(mockCurrentDateTime) shouldBe List(
          RuleCompletionDateError.copy(paths = Some(Seq("path")))
        )
      }
      "date is after 7th March and after today in the current tax year" in {
        stubNow("2020-03-01")
        CompletionDateValidation.validate("2020-03-08", "path", "2019-20")(mockCurrentDateTime) shouldBe List(
          RuleCompletionDateError.copy(paths = Some(Seq("path")))
        )
      }
      "date is not in the given tax year (early)" in {
        stubNow("2020-04-01")
        CompletionDateValidation.validate("2019-03-08", "path", "2019-20")(mockCurrentDateTime) shouldBe List(
          RuleCompletionDateError.copy(paths = Some(Seq("path")))
        )
      }
      "date is not in the given tax year (late)" in {
        stubNow("2020-04-01")
        CompletionDateValidation.validate("2020-05-08", "path", "2019-20")(mockCurrentDateTime) shouldBe List(
          RuleCompletionDateError.copy(paths = Some(Seq("path")))
        )
      }
      "date is before 7th March and in the next tax year" in {
        stubNow("2020-04-01")
        CompletionDateValidation.validate("2020-03-06", "path", "2019-20")(mockCurrentDateTime) shouldBe List(
          RuleCompletionDateError.copy(paths = Some(Seq("path")))
        )
      }
      "date is after 7th March and in the next tax year" in {
        stubNow("2019-04-01")
        CompletionDateValidation.validate("2020-03-08", "path", "2019-20")(mockCurrentDateTime) shouldBe List(
          RuleCompletionDateError.copy(paths = Some(Seq("path")))
        )
      }
    }
  }

}
