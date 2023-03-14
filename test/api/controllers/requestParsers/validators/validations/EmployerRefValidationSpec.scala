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

import api.models.errors.EmployerRefFormatError
import support.UnitSpec

class EmployerRefValidationSpec extends UnitSpec {

  "EmployerRefValidation" when {
    "validate" must {
      "return an empty list for a valid customerRef" in {
        EmployerRefValidation.validate(
          employerRef = "123/AB456"
        ) shouldBe NoValidationErrors
      }

      "return an EmployerRefFormatError for an invalid  employerRef" in {
        EmployerRefValidation.validate(
          employerRef = "This employerRef string is 91 characters long ------------------------------------------91"
        ) shouldBe List(EmployerRefFormatError)
      }
    }

    "validateOptional" must {
      "return an empty list for a value of 'None'" in {
        EmployerRefValidation.validateOptional(
          employerRef = None
        ) shouldBe NoValidationErrors
      }

      "return an empty list for a valid customerRef" in {
        EmployerRefValidation.validateOptional(
          employerRef = Some("123/AB456")
        ) shouldBe NoValidationErrors
      }

      "return an EmployerRefFormatError for an invalid  employerRef" in {
        EmployerRefValidation.validateOptional(
          employerRef = Some("This employerRef string is 91 characters long ------------------------------------------91")
        ) shouldBe List(EmployerRefFormatError)
      }
    }
  }

}
