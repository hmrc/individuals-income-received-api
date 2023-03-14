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

import api.models.errors.AssetDescriptionFormatError
import support.UnitSpec

class AssetDescriptionValidationSpec extends UnitSpec {

  "validate" should {
    "return an empty List" when {
      "the provided description is valid" in {
        AssetDescriptionValidation.validate("A description 123", "path") shouldBe NoValidationErrors
      }
    }

    "return an error" when {
      "the provided description is invalid" in {
        AssetDescriptionValidation.validate("", "path") shouldBe List(AssetDescriptionFormatError.copy(paths = Some(Seq("path"))))
      }
    }
  }

}
