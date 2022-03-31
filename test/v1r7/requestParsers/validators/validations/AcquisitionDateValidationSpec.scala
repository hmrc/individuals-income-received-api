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

package v1r7.requestParsers.validators.validations

import api.models.errors.RuleAcquisitionDateError
import support.UnitSpec

class AcquisitionDateValidationSpec extends UnitSpec {

  private val earlierDate = "2019-01-01"
  private val laterDate   = "2020-01-01"

  "validate" should {
    "return no errors" when {
      "passed disposal date after acquisition date" in {
        AcquisitionDateValidation.validate(disposalDate = laterDate, acquisitionDate = earlierDate, path = "path") shouldBe NoValidationErrors
      }
    }
    "return an error" when {
      "passed disposal date before acquisition date" in {
        AcquisitionDateValidation.validate(disposalDate = earlierDate, acquisitionDate = laterDate, path = "path") shouldBe
          List(RuleAcquisitionDateError.copy(paths = Some(Seq("path"))))
      }
    }
  }

}
