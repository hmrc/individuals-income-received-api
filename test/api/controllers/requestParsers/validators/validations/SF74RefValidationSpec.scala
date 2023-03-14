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

import api.models.errors.SF74RefFormatError
import support.UnitSpec

class SF74RefValidationSpec extends UnitSpec {

  "SF74RefValidation" when {
    "validateOptional" must {
      "return an empty list for a value of 'None'" in {
        SF74RefValidation.validateOptional(
          sf74Ref = None,
          path = "/path"
        ) shouldBe NoValidationErrors
      }

      "validate correctly for some valid sf74Ref" in {
        SF74RefValidation.validateOptional(
          sf74Ref = Some("SF74-123456"),
          path = "/path"
        ) shouldBe NoValidationErrors
      }

      "validate correctly for some invalid sf74Ref" in {
        SF74RefValidation.validateOptional(
          sf74Ref = Some("This sf74Ref string is 91 characters long ---------------------------------------------- 91"),
          path = "/path"
        ) shouldBe List(SF74RefFormatError.copy(paths = Some(Seq("/path"))))
      }
    }
  }

}
