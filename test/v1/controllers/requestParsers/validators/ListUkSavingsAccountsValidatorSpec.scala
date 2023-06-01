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

package v1.controllers.requestParsers.validators

import api.mocks.MockCurrentDateTime
import api.models.errors.{NinoFormatError, SavingsAccountIdFormatError}
import mocks.MockAppConfig
import support.UnitSpec
import v1.models.request.listUkSavingsAccounts.ListUkSavingsAccountsRawData

class ListUkSavingsAccountsValidatorSpec extends UnitSpec {

  private val validNino             = "AA123456A"
  private val validSavingsAccountId = "SAVKB2UVwUTBQGJ"

  class Test extends MockCurrentDateTime with MockAppConfig {
    val validator = new ListUkSavingsAccountsValidator
  }

  "running a validation" should {
    "return no errors" when {
      "a valid request without a savingsAccountId is supplied" in new Test {
        validator.validate(ListUkSavingsAccountsRawData(validNino, None)) shouldBe Nil
      }

      "a valid request with a savingsAccountId is supplied" in new Test {
        validator.validate(ListUkSavingsAccountsRawData(validNino, Some(validSavingsAccountId))) shouldBe Nil
      }
    }

    "return NinoFormatError error" when {
      "an invalid nino is supplied" in new Test {
        validator.validate(ListUkSavingsAccountsRawData("BAD_NINO", None)) shouldBe
          List(NinoFormatError)
      }
    }

    "return SavingsAccountIdFormatError errors" when {
      "an invalid savingsAccountId" in new Test {
        validator.validate(ListUkSavingsAccountsRawData(validNino, Some("BAD_SAVINGS_ACCT"))) shouldBe
          List(SavingsAccountIdFormatError)
      }
    }

    "return multiple errors" when {
      "the request has multiple errors" in new Test {
        validator.validate(ListUkSavingsAccountsRawData("BAD_NINO", Some("BAD_SAVINGS_ACCT"))) shouldBe
          List(NinoFormatError, SavingsAccountIdFormatError)
      }
    }
  }

}
