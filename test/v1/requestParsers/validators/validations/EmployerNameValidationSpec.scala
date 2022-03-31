/*
 * Copyright 2022 HM Revenue & Customs
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

package v1.requestParsers.validators.validations

import support.UnitSpec
import api.models.errors.EmployerNameFormatError

class EmployerNameValidationSpec extends UnitSpec {

  "EmployerNameValidation" when {
    "validate" must {
      "return an empty list for a valid employer name" in {
        EmployerNameValidation.validate(
          employerName = "BPDTS Ltd",
          105
        ) shouldBe NoValidationErrors
      }

      "return an EmployerNameFormatError for an invalid employerName" in {
        EmployerNameValidation.validate(
          employerName = "This employerName string is 106 characters long--------------------------------------------------------106",
          105
        ) shouldBe List(EmployerNameFormatError)
      }
    }
  }

}
