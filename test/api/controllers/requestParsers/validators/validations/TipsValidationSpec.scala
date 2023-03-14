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

class TipsValidationSpec extends UnitSpec {

  "validate" should {
    "return an empty list" when {
      "passed the maximum valid decimal value" in {
        TipsValidation.validateWithPath(
          amount = 99999999999.99,
          path = s"/tips"
        ) shouldBe NoValidationErrors
      }

      "passed the minimum valid decimal value" in {
        TipsValidation.validateWithPath(
          amount = 0.00,
          path = s"/tips"
        ) shouldBe NoValidationErrors
      }
    }

    "return a TipsFormatError" when {
      "passed a value too large" in {
        TipsValidation.validateWithPath(
          amount = 100000000000.00,
          path = s"/tips"
        ) shouldBe List(
          ValueFormatError.copy(
            paths = Some(Seq("/tips"))
          ))
      }

      "passed a value too small" in {
        TipsValidation.validateWithPath(
          amount = -0.01,
          path = s"/tips"
        ) shouldBe List(
          ValueFormatError.copy(
            paths = Some(Seq("/tips"))
          ))
      }

      "passed a value with the wrong scale" in {
        TipsValidation.validateWithPath(
          amount = 123.456,
          path = s"/tips"
        ) shouldBe List(
          ValueFormatError.copy(
            paths = Some(Seq("/tips"))
          ))
      }
    }
  }

}
