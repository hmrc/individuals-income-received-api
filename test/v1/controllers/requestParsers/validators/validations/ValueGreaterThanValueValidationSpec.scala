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
import v1.models.errors.RuleLossesGreaterThanGainError

class ValueGreaterThanValueValidationSpec extends UnitSpec {
  "validateOptional" should {
    "return NoValidationErrors" when {
      "one or more values provided are None" in {
        ValueGreaterThanValueValidation.validateOptional(Some(1), None, "") shouldBe NoValidationErrors
        ValueGreaterThanValueValidation.validateOptional(None, Some(1), "") shouldBe NoValidationErrors
        ValueGreaterThanValueValidation.validateOptional(None, None, "") shouldBe NoValidationErrors
      }
      "values provided are equal" in {
        ValueGreaterThanValueValidation.validateOptional(Some(1), Some(1), "") shouldBe NoValidationErrors
      }
      "value 1 is less than value 2" in {
        ValueGreaterThanValueValidation.validateOptional(Some(1), Some(2), "") shouldBe NoValidationErrors
      }
    }
    "return an error" when {
      "value 1 is greater than value 2" in {
        ValueGreaterThanValueValidation.validateOptional(Some(2), Some(1), "path") shouldBe List(RuleLossesGreaterThanGainError.copy(
          paths = Some(Seq("path"))))
      }
    }
  }
}
