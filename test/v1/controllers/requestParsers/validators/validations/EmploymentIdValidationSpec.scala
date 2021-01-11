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
import v1.models.errors.EmploymentIdFormatError

class EmploymentIdValidationSpec extends UnitSpec {

  "EmploymentIdValidation" when {
    "validate" should {
      "return an empty list for a valid employment ID" in {
        EmploymentIdValidation.validate("4557ecb5-fd32-48cc-81f5-e6acd1099f3c") shouldBe NoValidationErrors
      }

      "return an EmploymentIdFormatError error for an invalid employment ID" in {
        EmploymentIdValidation.validate("") shouldBe List(EmploymentIdFormatError)
      }
    }
  }
}
