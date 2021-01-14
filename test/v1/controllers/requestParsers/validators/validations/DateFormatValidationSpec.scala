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
import v1.controllers.requestParsers.validators.validations.DateFormatValidation.ISO_DATE_FORMAT
import v1.models.errors.{DateFormatError, MtdError}

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
          path = "/path",
        ) shouldBe List(DateFormatError.copy(message =  ISO_DATE_FORMAT, paths = Some(Seq("/path"))))
      }
    }

    "validate" must {

      object DummyError extends MtdError("ERROR_CODE", "Error message")

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
    }
  }
}
