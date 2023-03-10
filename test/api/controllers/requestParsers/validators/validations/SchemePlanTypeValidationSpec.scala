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

import api.models.errors.SchemePlanTypeFormatError
import support.UnitSpec

class SchemePlanTypeValidationSpec extends UnitSpec {

  "SchemePlanTypeValidation" when {
    "validate" must {
      "return an empty list for a valid scheme plan type" in {
        SchemePlanTypeValidation.validate(
          schemePlanType = "EMI",
          awarded = false
        ) shouldBe NoValidationErrors

        SchemePlanTypeValidation.validate(
          schemePlanType = "CSOP",
          awarded = false
        ) shouldBe NoValidationErrors

        SchemePlanTypeValidation.validate(
          schemePlanType = "SAYE",
          awarded = false
        ) shouldBe NoValidationErrors

        SchemePlanTypeValidation.validate(
          schemePlanType = "SIP",
          awarded = true
        ) shouldBe NoValidationErrors

        SchemePlanTypeValidation.validate(
          schemePlanType = "Other",
          awarded = true
        ) shouldBe NoValidationErrors
      }

      "return a SchemePlanTypeFormatError for a invalid scheme plan type" in {
        SchemePlanTypeValidation.validate(
          schemePlanType = "Not a Scheme plan type",
          awarded = true
        ) shouldBe List(SchemePlanTypeFormatError)
      }
    }
  }

}
