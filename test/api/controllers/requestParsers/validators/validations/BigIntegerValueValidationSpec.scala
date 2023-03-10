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

import api.models.errors.ValueFormatError
import support.UnitSpec

class BigIntegerValueValidationSpec extends UnitSpec with ValueFormatErrorMessages {

  "BigIntegerValueValidation" when {
    "validate" must {
      "return an empty list for a valid integer value (default validation)" in {
        BigIntegerValueValidation.validate(
          field = 100,
          path = "/path"
        ) shouldBe NoValidationErrors
      }

      "return an empty list for a valid integer value (custom validation)" in {
        BigIntegerValueValidation.validate(
          field = 15,
          minValue = 10,
          path = "/path"
        ) shouldBe NoValidationErrors
      }

      "return a ValueFormatError for an invalid integer value (default validation)" in {
        BigIntegerValueValidation.validate(
          field = -2,
          path = "/path"
        ) shouldBe List(ValueFormatError.copy(message = ZERO_MINIMUM_BIG_INTEGER_INCLUSIVE, paths = Some(Seq("/path"))))
      }

      "return a ValueFormatError for an invalid integer value (custom validation)" in {
        BigIntegerValueValidation.validate(
          field = 9,
          minValue = 10,
          path = "/path",
          message = "error message"
        ) shouldBe List(ValueFormatError.copy(message = "error message", paths = Some(Seq("/path"))))
      }
    }

    "validateOptional" should {
      "return an empty list for a value of 'None'" in {
        BigIntegerValueValidation.validateOptional(
          field = None,
          path = "/path"
        ) shouldBe NoValidationErrors
      }

      "validate correctly for some valid field" in {
        BigIntegerValueValidation.validateOptional(
          field = Some(20),
          path = "/path"
        ) shouldBe NoValidationErrors
      }

      "validate correctly for some invalid field" in {
        BigIntegerValueValidation.validateOptional(
          field = Some(-10),
          path = "/path"
        ) shouldBe List(ValueFormatError.copy(message = ZERO_MINIMUM_BIG_INTEGER_INCLUSIVE, paths = Some(Seq("/path"))))
      }
    }
  }

}
