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

package v1.controllers.requestParsers.validators.validations

import api.controllers.requestParsers.validators.validations.NoValidationErrors
import api.models.errors.{DateFormatError, MtdError}
import play.api.http.Status.BAD_REQUEST
import support.UnitSpec

class DateFormatValidationSpec extends UnitSpec {

  "DateFormatValidation" when {
    "validateWithPath" must {
      "return an empty list for a valid date" in {
        DateFormatValidation.validateWithPath(
          date = "2019-04-20",
          path = "/path"
        ) shouldBe NoValidationErrors
      }

      "return a DateFormatError for an invalid date" in {
        DateFormatValidation.validateWithPath(
          date = "20-04-2017",
          path = "/path"
        ) shouldBe List(DateFormatError.copy(paths = Some(Seq("/path"))))
      }

      "return a DateFormatError for an date which is out of allowable minimum year" in {
        DateFormatValidation.validateWithPath(
          date = "0010-01-01",
          path = "/path"
        ) shouldBe List(DateFormatError.copy(paths = Some(Seq("/path"))))
      }

      "return a DateFormatError for an date which is out of allowable maximum year" in {
        DateFormatValidation.validateWithPath(
          date = "2101-01-01",
          path = "/path"
        ) shouldBe List(DateFormatError.copy(paths = Some(Seq("/path"))))
      }

      "return an empty list for a valid date within allowable years" in {
        DateFormatValidation.validateWithPath(
          date = "2099-04-20",
          path = "/path"
        ) shouldBe NoValidationErrors
      }
    }

    "validateOptionalWithPath" must {
      "return an empty list for a value of 'None'" in {
        DateFormatValidation.validateOptionalWithPath(
          date = None,
          path = "/path"
        ) shouldBe NoValidationErrors
      }

      "return an empty list for some valid date" in {
        DateFormatValidation.validateOptionalWithPath(
          date = Some("2019-04-20"),
          path = "/path"
        ) shouldBe NoValidationErrors
      }

      "return a DateFormatError for some invalid date" in {
        DateFormatValidation.validateOptionalWithPath(
          date = Some("20-04-2017"),
          path = "/path"
        ) shouldBe List(DateFormatError.copy(paths = Some(Seq("/path"))))
      }

      "return a DateFormatError for an date which is out of allowable minimum year" in {
        DateFormatValidation.validateOptionalWithPath(
          date = Some("0010-01-01"),
          path = "/path"
        ) shouldBe List(DateFormatError.copy(paths = Some(Seq("/path"))))
      }

      "return a DateFormatError for an date which is out of allowable maximum year" in {
        DateFormatValidation.validateOptionalWithPath(
          date = Some("2101-01-01"),
          path = "/path"
        ) shouldBe List(DateFormatError.copy(paths = Some(Seq("/path"))))
      }
    }

    "validate" must {

      object DummyError extends MtdError("ERROR_CODE", "Error message", BAD_REQUEST)

      "return an empty list for a valid date" in {
        DateFormatValidation.validate(
          date = "2019-04-20",
          error = DummyError
        ) shouldBe NoValidationErrors
      }

      "return a DateFormatError for an invalid date" in {
        DateFormatValidation.validate(
          date = "2019-04-40",
          error = DummyError
        ) shouldBe List(DummyError)
      }

      "return a DateFormatError for an date which is out of allowable minimum year" in {
        DateFormatValidation.validate(
          date = "0010-01-01",
          error = DummyError
        ) shouldBe List(DummyError)
      }

      "return a DateFormatError for an date which is out of allowable maximum year" in {
        DateFormatValidation.validate(
          date = "2101-01-01",
          error = DummyError
        ) shouldBe List(DummyError)
      }

    }
  }

}
