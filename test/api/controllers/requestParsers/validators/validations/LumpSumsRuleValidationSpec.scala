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

import api.models.errors.RuleLumpSumsError
import support.UnitSpec
import v1.models.request.amendOtherEmployment.{AmendLumpSums, AmendTaxableLumpSumsAndCertainIncomeItem}

class LumpSumsRuleValidationSpec extends UnitSpec {

  "validate" when {
    val lumpSum: AmendLumpSums = AmendLumpSums(
      employerName = "employerName",
      employerRef = "456/AB456",
      taxableLumpSumsAndCertainIncome = Some(
        AmendTaxableLumpSumsAndCertainIncomeItem(
          amount = 100,
          taxPaid = None,
          taxTakenOffInEmployment = true
        )
      ),
      benefitFromEmployerFinancedRetirementScheme = None,
      redundancyCompensationPaymentsOverExemption = None,
      redundancyCompensationPaymentsUnderExemption = None
    )

    "supplied lump sum has at least one defined child object" should {
      "return no validation errors" in {
        LumpSumsRuleValidation.validate(lumpSum = lumpSum, index = 0) shouldBe NoValidationErrors
      }
    }

    "supplied lump sum has no defined child objects" should {
      "return RuleLumpSumsError with the correct path" in {
        LumpSumsRuleValidation.validate(
          lumpSum = lumpSum.copy(taxableLumpSumsAndCertainIncome = None),
          index = 0
        ) shouldBe List(RuleLumpSumsError.withExtraPath(newPath = "/lumpSums/0"))
      }
    }
  }

}
