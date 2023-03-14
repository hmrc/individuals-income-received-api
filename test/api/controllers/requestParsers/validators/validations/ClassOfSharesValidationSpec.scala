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

import api.models.errors.{ClassOfSharesAcquiredFormatError, ClassOfSharesAwardedFormatError}
import support.UnitSpec

class ClassOfSharesValidationSpec extends UnitSpec {

  "ClassOfSharesValidation" when {
    "validate" must {
      "return an empty list for a valid ClassOfShares Acquired" in {
        ClassOfSharesValidation.validate(
          classOfShares = "Ordinary shares",
          acquired = true
        ) shouldBe NoValidationErrors
      }

      "return an empty list for a valid ClassOfShares Awarded" in {
        ClassOfSharesValidation.validate(
          classOfShares = "FIRST",
          acquired = false
        ) shouldBe NoValidationErrors
      }

      "return a ClassOfSharesAcquiredFormatError for invalid ClassOfShares Acquired" in {
        ClassOfSharesValidation.validate(
          classOfShares = "This ClassOfShares Acquired string is 91 characters long --------------------------------91",
          acquired = true
        ) shouldBe List(ClassOfSharesAcquiredFormatError)
      }

      "return a ClassOfSharesAwardedFormatError for invalid ClassOfShares Awarded" in {
        ClassOfSharesValidation.validate(
          classOfShares = "This ClassOfShares Awarded string is 91 characters long ---------------------------------91",
          acquired = false
        ) shouldBe List(ClassOfSharesAwardedFormatError)
      }
    }
  }

}
