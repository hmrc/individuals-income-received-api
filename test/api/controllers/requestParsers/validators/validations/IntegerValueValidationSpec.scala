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

class IntegerValueValidationSpec extends UnitSpec with ValueFormatErrorMessages {

  "IntegerValueValidation" when {
    "validate" must {
      "return an empty list for a valid integer value (default validation)" in {
        IntegerValueValidation.validate(
          field = 10,
          path = "/path"
        ) shouldBe NoValidationErrors
      }

      "return an empty list for a valid integer value (custom validation)" in {
        IntegerValueValidation.validate(
          field = 15,
          minValue = 10,
          maxValue = 20,
          path = "/path"
        ) shouldBe NoValidationErrors
      }

      "return a ValueFormatError for an invalid integer value (default validation)" in {
        IntegerValueValidation.validate(
          field = -2,
          path = "/path"
        ) shouldBe List(ValueFormatError.copy(message = ZERO_MINIMUM_INTEGER_INCLUSIVE, paths = Some(Seq("/path"))))
      }

      "return a ValueFormatError for an invalid integer value (custom validation)" in {
        IntegerValueValidation.validate(
          field = 9,
          minValue = 10,
          maxValue = 20,
          path = "/path",
          message = "error message"
        ) shouldBe List(ValueFormatError.copy(message = "error message", paths = Some(Seq("/path"))))
      }
    }

    "validateOptional" should {
      "return an empty list for a value of 'None'" in {
        IntegerValueValidation.validateOptional(
          field = None,
          path = "/path"
        ) shouldBe NoValidationErrors
      }

      "validate correctly for some valid field" in {
        IntegerValueValidation.validateOptional(
          field = Some(20),
          path = "/path"
        ) shouldBe NoValidationErrors
      }

      "validate correctly for some invalid field" in {
        IntegerValueValidation.validateOptional(
          field = Some(-10),
          path = "/path"
        ) shouldBe List(ValueFormatError.copy(message = ZERO_MINIMUM_INTEGER_INCLUSIVE, paths = Some(Seq("/path"))))
      }
    }
  }

}
