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

import api.models.errors.RuleGainLossError
import support.UnitSpec

class GainLossValidationSpec extends UnitSpec {
  val res: BigDecimal = 1000.23

  "validate" should {
    Seq(
      (Some(res), Some(res), List(RuleGainLossError.copy(paths = Some(Seq("path"))))),
      (Some(res), None, NoValidationErrors),
      (None, Some(res), NoValidationErrors),
      (None, None, NoValidationErrors)
    ).foreach { case (gain, loss, result) =>
      s"return ${if (result.isEmpty) "an empty List" else "an error"} when passed in gain: $gain and loss: $loss" in {
        GainLossValidation.validate(gain, loss, RuleGainLossError, "path") shouldBe result
      }
    }
  }

}
