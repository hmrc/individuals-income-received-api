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
import api.models.errors.{MtdError, RuleCompletionDateError}
import utils.CurrentDateTime

import java.time.LocalDate

object CompletionDateValidation {
  private val MARCH = 3
  private val SEVEN = 7

  def validate(date: String, path: String, taxYear: String)(implicit currentDateTime: CurrentDateTime): List[MtdError] = {
    val formattedDate = LocalDate.parse(date)
    val march7th      = LocalDate.parse(TaxYear.fromMtd(taxYear).asDownstream, yearFormat).withMonth(MARCH).withDayOfMonth(SEVEN)

    val (fromDate, toDate) = getToDateAndFromDate(taxYear)

    val currentTaxYear: Int = {
      val date                             = currentDateTime.getLocalDate
      lazy val taxYearStartDate: LocalDate = LocalDate.of(date.getYear, 4, 6)

      if (date.isBefore(taxYearStartDate)) date.getYear else date.getYear + 1
    }

    val dateIsBefore7thMarch    = formattedDate.isBefore(march7th)
    val dateIsAfterToday        = formattedDate.isAfter(currentDateTime.getLocalDate)
    val dateIsInTaxYear         = !formattedDate.isBefore(fromDate) && !formattedDate.isAfter(toDate)
    val taxYearIsCurrentTaxYear = TaxYear.fromMtd(taxYear).asDownstream.toInt == currentTaxYear

    if (dateIsBefore7thMarch && taxYearIsCurrentTaxYear || dateIsAfterToday || !dateIsInTaxYear) {
      List(RuleCompletionDateError.copy(paths = Some(Seq(path))))
    } else {
      NoValidationErrors
    }
  }

}
