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

import api.models.domain.TaxYear
import api.models.errors.RuleDisposalDateError
import support.UnitSpec

import java.time.LocalDate

class DisposalDateValidationSpec extends UnitSpec with DisposalDateErrorMessages {

  private val now = LocalDate.now()

  private val validTaxYear = "2019-20"
  private val validDate    = "2020-01-01"

  private val dateAfterToday    = now.plusYears(2).format(dateFormat)
  private val dateBeforeTaxYear = "2018-01-01"
  private val dateAfterTaxYear  = "2021-01-01"

  "validate" should {
    "return an empty list" when {
      "the supplied date is within the supplied tax year and no later than today" in {
        DisposalDateValidation.validate(validDate, validTaxYear, "path", validateToday = true, IN_YEAR_NO_LATER_THAN_TODAY) shouldBe List()
      }
      "the supplied date is within the supplied tax year and validateToday is false" in {
        val year    = now.plusYears(1).getYear.toString
        val taxYear = TaxYear.fromDownstream(year).asMtd
        val date = LocalDate
          .parse(year, yearFormat)
          .withMonth(4)
          .withDayOfMonth(4)
          .format(dateFormat)
        DisposalDateValidation.validate(date, taxYear, "path", validateToday = false, IN_YEAR_NO_LATER_THAN_TODAY) shouldBe List()
      }
    }

    "return an error" when {
      "the supplied date is after today" in {
        DisposalDateValidation.validate(dateAfterToday, validTaxYear, "path", validateToday = true, IN_YEAR_NO_LATER_THAN_TODAY) shouldBe List(
          RuleDisposalDateError.copy(paths = Some(Seq("path")), message = IN_YEAR_NO_LATER_THAN_TODAY))
      }

      "the supplied date is before the supplied tax year" in {
        DisposalDateValidation.validate(dateBeforeTaxYear, validTaxYear, "path", validateToday = true, IN_YEAR_NO_LATER_THAN_TODAY) shouldBe List(
          RuleDisposalDateError.copy(paths = Some(Seq("path")), message = IN_YEAR_NO_LATER_THAN_TODAY))
      }

      "the supplied date is after the supplied tax year" in {
        DisposalDateValidation.validate(dateAfterTaxYear, validTaxYear, "path", validateToday = true, IN_YEAR_NO_LATER_THAN_TODAY) shouldBe List(
          RuleDisposalDateError.copy(paths = Some(Seq("path")), message = IN_YEAR_NO_LATER_THAN_TODAY))
      }
    }
  }

}
