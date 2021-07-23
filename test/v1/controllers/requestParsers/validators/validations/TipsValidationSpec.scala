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
import v1.models.errors.TipsFormatError

class TipsValidationSpec extends UnitSpec {

  "validate" should {
    "return an empty list" when {
      "passed the maximum valid decimal value" in {
        TipsValidation.validate(
          amount = 99999999999.99
        ) shouldBe NoValidationErrors
      }

      "passed the minimum valid decimal value" in {
        TipsValidation.validate(
          amount = 0.00
        ) shouldBe NoValidationErrors
      }
    }

    "return a TipsFormatError" when {
      "passed a value too large" in {
        TipsValidation.validate(
          amount = 100000000000.00
        ) shouldBe List(TipsFormatError)
      }

      "passed a value too small" in {
        TipsValidation.validate(
          amount = -0.01
        ) shouldBe List(TipsFormatError)
      }

      "passed a value with the wrong scale" in {
        TipsValidation.validate(
          amount = 123.456
        ) shouldBe List(TipsFormatError)
      }
    }
  }
}
