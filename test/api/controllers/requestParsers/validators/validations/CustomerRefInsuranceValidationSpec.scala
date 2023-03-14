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

class CustomerRefInsuranceValidationSpec extends UnitSpec {

  "CustomerRefInsuranceValidation" when {
    "validate" must {
      "return an empty list for a valid customerRef" in {
        CustomerRefInsuranceValidation.validateOptional(
          customerRef = Some("INPOLY123A")
        ) shouldBe NoValidationErrors
      }

      "return an empty list for no customerRef" in {
        CustomerRefInsuranceValidation.validateOptional(
          customerRef = None
        ) shouldBe NoValidationErrors
      }

      "return an empty list for a valid 90 char customerRef" in {
        CustomerRefInsuranceValidation.validateOptional(
          customerRef = Some("a" * 90)
        ) shouldBe NoValidationErrors
      }

      "return an empty list for a valid 1 char customerRef" in {
        CustomerRefInsuranceValidation.validateOptional(
          customerRef = Some("1")
        ) shouldBe NoValidationErrors
      }

      "return a CustomerRefFormatError for an invalid customerRef" in {
        CustomerRefInsuranceValidation.validateOptional(
          customerRef = Some("")
        ) shouldBe List(CustomerRefFormatError)
      }

      "return a CustomerRefFormatError for an invalid 91 char customerRef" in {
        CustomerRefInsuranceValidation.validateOptional(
          customerRef = Some("a" * 91)
        ) shouldBe List(CustomerRefFormatError)
      }

      "return a CustomerRefFormatError for an invalid bad regex customerRef" in {
        CustomerRefInsuranceValidation.validateOptional(
          customerRef = Some("123ABC[][][]()()()")
        ) shouldBe List(CustomerRefFormatError)
      }
    }
  }

}
