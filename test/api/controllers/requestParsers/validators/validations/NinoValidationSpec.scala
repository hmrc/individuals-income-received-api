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

import api.models.errors.NinoFormatError
import api.models.utils.JsonErrorValidators
import support.UnitSpec

class NinoValidationSpec extends UnitSpec with JsonErrorValidators {

  "NinoValidation" when {
    "a valid NINO is supplied" must {
      val nino = "AA123456A"

      "be valid" in {
        NinoValidation.isValid(nino) shouldBe true
      }

      "return no errors" in {
        NinoValidation.validate(nino) shouldBe empty
      }
    }

    "a invalid NINO is supplied" must {
      val nino = "AA123456ABCBBCBCBC"

      "not be valid" in {
        NinoValidation.isValid(nino) shouldBe false
      }

      "return a NinoFormatError" in {
        NinoValidation.validate(nino) shouldBe List(NinoFormatError)
      }
    }
  }

}
