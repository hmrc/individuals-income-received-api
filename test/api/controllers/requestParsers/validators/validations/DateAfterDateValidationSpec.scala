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

import api.models.errors.MtdError
import play.api.http.Status.BAD_REQUEST
import support.UnitSpec

class DateAfterDateValidationSpec extends UnitSpec {
  private val earlierDate = "2020-01-01"
  private val laterDate   = "2020-01-02"

  private val error = MtdError("test code", "test message", BAD_REQUEST)

  "validate" should {
    "return NoValidationErrors" when {
      "date 1 is before date 2" in {
        DateAfterDateValidation.validate(
          dateWhichShouldBeEarlier = earlierDate,
          dateWhichShouldBeLater = laterDate,
          path = "path",
          error = error
        ) shouldBe NoValidationErrors
      }
    }
    "return an error" when {
      "date 1 is after date 2" in {
        DateAfterDateValidation.validate(
          dateWhichShouldBeEarlier = laterDate,
          dateWhichShouldBeLater = earlierDate,
          path = "path",
          error = error
        ) shouldBe List(error.copy(paths = Some(Seq("path"))))
      }
    }
  }

}
