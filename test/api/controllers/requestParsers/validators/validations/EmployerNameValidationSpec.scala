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

import api.models.errors.EmployerNameFormatError
import support.UnitSpec

class EmployerNameValidationSpec extends UnitSpec {

  "EmployerNameValidation" when {
    "validate" must {
      "return an empty list for a valid employer name for other employment" in {
        EmployerNameValidation.validateOtherEmployment(
          employerName = "BPDTS Ltd"
        ) shouldBe NoValidationErrors
      }

      "return an empty list for a valid employer name for custom employment" in {
        EmployerNameValidation.validateCustomEmployment(
          employerName = "BPDTS Ltd"
        ) shouldBe NoValidationErrors
      }

      "return an EmployerNameFormatError for an invalid employerName in other employment" in {
        EmployerNameValidation.validateOtherEmployment(
          employerName = "This employerName string is 106 characters long--------------------------------------------------------106"
        ) shouldBe List(EmployerNameFormatError)
      }

      "return an EmployerNameFormatError for an invalid employerName in custom employment" in {
        EmployerNameValidation.validateCustomEmployment(
          employerName = "This employerName string is 75 characters long---------------------------75"
        ) shouldBe List(EmployerNameFormatError)
      }
      "return an EmployerNameFormatError for an an employerName that starts with a whitespace in custom employment" in {
        EmployerNameValidation.validateCustomEmployment(
          employerName = " BPDTS Ltd"
        ) shouldBe List(EmployerNameFormatError)
      }
    }
  }

}
