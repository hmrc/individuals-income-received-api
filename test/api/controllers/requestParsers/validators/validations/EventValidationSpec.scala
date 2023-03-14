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

import api.models.errors.EventFormatError
import support.UnitSpec

class EventValidationSpec extends UnitSpec {

  "EventValidation" when {
    "validate" must {
      "return an empty list for a valid event" in {
        EventValidation.validate(
          event = "Death of spouse",
          path = "/path"
        ) shouldBe NoValidationErrors
      }

      "return a EventFormatError for an invalid event" in {
        EventValidation.validate(
          event = s"${"This event string is over 90 characters long" * 10}",
          path = "/path"
        ) shouldBe List(EventFormatError.copy(paths = Some(Seq("/path"))))
      }
    }

    "validateOptional" must {
      "return an empty list for a value of 'None'" in {
        EventValidation.validateOptional(
          event = None,
          path = "/path"
        ) shouldBe NoValidationErrors
      }

      "validate correctly for some valid event" in {
        EventValidation.validateOptional(
          event = Some("Death of spouse"),
          path = "/path"
        ) shouldBe NoValidationErrors
      }

      "validate correctly for some invalid event" in {
        EventValidation.validateOptional(
          event = Some(s"${"This event string is over 90 characters long" * 10}"),
          path = "/path"
        ) shouldBe List(EventFormatError.copy(paths = Some(Seq("/path"))))
      }
    }
  }

}
