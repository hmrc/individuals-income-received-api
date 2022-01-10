/*
 * Copyright 2022 HM Revenue & Customs
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

package v1r7.controllers.requestParsers.validators.validations

import support.UnitSpec
import v1r7.models.errors.PpdSubmissionIdFormatError

class PpdSubmissionIdValidationSpec extends UnitSpec {

  "PpdSubmissionIdValidation" when {
    "validate" must {
      "return an empty list for a valid ppdSubmissionId" in {
        PpdSubmissionIdValidation.validate(
          ppdSubmissionId = "AB0000000098",
          PpdSubmissionIdFormatError
        ) shouldBe NoValidationErrors
      }

      "return a PpdSubmissionIdFormatError for an invalid ppdSubmissionId" in {
        PpdSubmissionIdValidation.validate(
          ppdSubmissionId = "NotAnID",
          PpdSubmissionIdFormatError
        ) shouldBe List(PpdSubmissionIdFormatError)
      }
    }
  }
}
