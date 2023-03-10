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

import api.models.errors.{MtdError, SourceFormatError}
import api.models.utils.JsonErrorValidators
import support.UnitSpec

class EmploymentSourceValidationSpec extends UnitSpec with JsonErrorValidators {

  case class SetUp(employmentId: String)

  "validate" should {
    "return no errors" when {
      "a valid employment source hmrcHeld is provided" in new SetUp("hmrcHeld") {

        EmploymentSourceValidation.validate(employmentId).isEmpty shouldBe true
      }
      "a valid employment source user is provided" in new SetUp("user") {

        EmploymentSourceValidation.validate(employmentId).isEmpty shouldBe true
      }

      "a valid employment source latest is provided" in new SetUp("latest") {

        EmploymentSourceValidation.validate(employmentId).isEmpty shouldBe true
      }
    }
    "return an error" when {
      "an invalid employment source is provided" in new SetUp("customer") {

        val validationResult: List[MtdError] = EmploymentSourceValidation.validate(employmentId)
        validationResult.length shouldBe 1
        validationResult.head shouldBe SourceFormatError
      }
    }
  }

}
