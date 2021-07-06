/*
 * Copyright 2021 HM Revenue & Customs
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

package v1.controllers.requestParsers.validators.validations

import v1.models.domain.DesTaxYear
import v1.models.errors.{MtdError, RuleDisposalDateError}

import java.time.LocalDate

object DisposalDateValidation {

  private val APRIL = 4
  private val SIX = 6
  private val FIVE = 5

  def validate(date: String, taxYear: String, path: String): List[MtdError] = {
    val now = LocalDate.now()
    val parsedDate = LocalDate.parse(date)
    if(parsedDate.isAfter(now)) {
      List(RuleDisposalDateError.copy(paths = Some(Seq(path))))
    } else {
      val year = LocalDate.parse(DesTaxYear.fromMtd(taxYear).value, yearFormat)
      val fromDate = year.minusYears(1).withMonth(APRIL).withDayOfMonth(SIX)
      val toDate = year.withMonth(APRIL).withDayOfMonth(FIVE)

      if(parsedDate.isBefore(fromDate) || parsedDate.isAfter(toDate)) {
        List(RuleDisposalDateError.copy(paths = Some(Seq(path))))
      } else {
        NoValidationErrors
      }
    }
  }
}
