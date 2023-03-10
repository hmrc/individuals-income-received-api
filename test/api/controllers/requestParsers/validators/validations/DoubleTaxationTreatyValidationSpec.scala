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

import api.models.errors.DoubleTaxationTreatyFormatError
import support.UnitSpec

class DoubleTaxationTreatyValidationSpec extends UnitSpec {

  "DoubleTaxationTreatyValidation" when {
    "validateOptional" must {
      "return an empty list for a value of 'None'" in {
        DoubleTaxationTreatyValidation.validateOptional(
          dblTaxationTreaty = None,
          path = "/path"
        ) shouldBe NoValidationErrors
      }

      "validate correctly for some valid dblTaxationTreaty" in {
        DoubleTaxationTreatyValidation.validateOptional(
          dblTaxationTreaty = Some("MUNICH"),
          path = "/path"
        ) shouldBe NoValidationErrors
      }

      "validate correctly for some invalid dblTaxationTreaty" in {
        DoubleTaxationTreatyValidation.validateOptional(
          dblTaxationTreaty = Some("This dblTaxationTreaty string is 91 characters long -------------------------------------91"),
          path = "/path"
        ) shouldBe List(DoubleTaxationTreatyFormatError.copy(paths = Some(Seq("/path"))))
      }
    }
  }

}
