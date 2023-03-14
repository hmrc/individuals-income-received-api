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

import api.models.errors.RuleTaxYearNotSupportedError
import api.models.utils.JsonErrorValidators
import mocks.MockAppConfig
import support.UnitSpec

class TaxYearNotSupportedValidationSpec extends UnitSpec with JsonErrorValidators {

  class Test extends MockAppConfig {
    val minimumTaxYear = 2021
  }

  "validate" should {
    "return no errors" when {
      "a tax year after 2020-21 is supplied" in new Test {
        private val validTaxYear     = "2021-22"
        private val validationResult = TaxYearNotSupportedValidation.validate(validTaxYear, minimumTaxYear)

        validationResult.isEmpty shouldBe true
      }

      "the minimum allowed tax year is supplied" in new Test {
        private val validTaxYear     = "2020-21"
        private val validationResult = TaxYearNotSupportedValidation.validate(validTaxYear, minimumTaxYear)

        validationResult.isEmpty shouldBe true
      }

    }

    "return the given error" when {
      "a tax year before 2020-21 is supplied" in new Test {
        private val invalidTaxYear   = "2019-20"
        private val validationResult = TaxYearNotSupportedValidation.validate(invalidTaxYear, minimumTaxYear)

        validationResult.isEmpty shouldBe false
        validationResult.length shouldBe 1
        validationResult.head shouldBe RuleTaxYearNotSupportedError
      }
    }
  }

}
