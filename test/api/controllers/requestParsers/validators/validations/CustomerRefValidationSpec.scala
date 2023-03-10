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

import api.models.errors.CustomerRefFormatError
import support.UnitSpec

class CustomerRefValidationSpec extends UnitSpec {

  "CustomerRefValidation" when {
    "validate" must {
      "return an empty list for a valid customerRef" in {
        CustomerRefValidation.validate(
          customerRef = "INPOLY123A"
        ) shouldBe NoValidationErrors
      }

      "return a CustomerRefFormatError for an invalid customerRef" in {
        CustomerRefValidation.validate(
          customerRef = "This customer ref string is 91 characters long ------------------------------------------91"
        ) shouldBe List(CustomerRefFormatError)
      }
    }

    "validateOptional" must {
      "return an empty list for a value of 'None'" in {
        CustomerRefValidation.validateOptional(
          customerRef = None
        ) shouldBe NoValidationErrors
      }

      "validate correctly for some valid customerRef" in {
        CustomerRefValidation.validateOptional(
          customerRef = Some("PENSIONINCOME245")
        ) shouldBe NoValidationErrors
      }

      "validate correctly for some invalid customerRef" in {
        CustomerRefValidation.validateOptional(
          customerRef = Some("This customer ref string is 91 characters long ------------------------------------------91")
        ) shouldBe List(CustomerRefFormatError)
      }
    }
  }

}
