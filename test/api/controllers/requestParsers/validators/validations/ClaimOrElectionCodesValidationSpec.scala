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

import api.models.errors.ClaimOrElectionCodesFormatError
import support.UnitSpec

class ClaimOrElectionCodesValidationSpec extends UnitSpec {

  "validateOptional" should {
    "return an empty list" when {
      "None is supplied" in {
        ClaimOrElectionCodesValidation.validateOptional(None, 0) shouldBe NoValidationErrors
      }
      "a List is supplied with no invalid fields" in {
        ClaimOrElectionCodesValidation.validateOptional(
          Some(Seq("PRR", "LET", "GHO", "ROR", "PRO", "ESH", "NVC", "SIR", "OTH", "BAD", "INV")),
          0) shouldBe
          NoValidationErrors
      }
    }
    "return an error" when {
      "an empty List is supplied" in {
        ClaimOrElectionCodesValidation.validateOptional(Some(Seq()), 0) shouldBe
          List(ClaimOrElectionCodesFormatError.copy(paths = Some(Seq(s"/disposals/0/claimOrElectionCodes"))))
      }
      "a list only containing invalid fields is supplied" in {
        ClaimOrElectionCodesValidation.validateOptional(Some(Seq("beans", "eggs", "toast")), 0) shouldBe
          List(
            ClaimOrElectionCodesFormatError.copy(paths =
              Some(Seq(s"/disposals/0/claimOrElectionCodes/0", s"/disposals/0/claimOrElectionCodes/1", s"/disposals/0/claimOrElectionCodes/2"))))
      }
      "a list containing a mixture of valid and invalid fields is supplied" in {
        ClaimOrElectionCodesValidation.validateOptional(Some(Seq("PRR", "beans", "LET", "eggs", "GHO", "toast")), 0) shouldBe
          List(
            ClaimOrElectionCodesFormatError.copy(paths =
              Some(Seq(s"/disposals/0/claimOrElectionCodes/1", s"/disposals/0/claimOrElectionCodes/3", s"/disposals/0/claimOrElectionCodes/5"))))
      }
    }
  }

}
