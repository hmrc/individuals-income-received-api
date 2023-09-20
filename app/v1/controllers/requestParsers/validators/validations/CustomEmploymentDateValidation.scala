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

package v1.controllers.requestParsers.validators.validations

import api.controllers.requestParsers.validators.validations.{NoValidationErrors, dateFormat}
import api.models.domain.TaxYear
import api.models.errors._

import java.time.LocalDate

object CustomEmploymentDateValidation {

  def validate(startDate: String, cessationDate: Option[String], taxYear: String): List[MtdError] = {

    lazy val taxYearStartDate: LocalDate = LocalDate.parse(taxYear.take(4) + "-04-06", dateFormat)
    lazy val taxYearEndDate: LocalDate   = LocalDate.parse(TaxYear.fromMtd(taxYear).asDownstream + "-04-05", dateFormat)

    val formatErrors: List[MtdError] = List(
      Some(DateFormatValidation.validate(startDate, StartDateFormatError)),
      cessationDate.map(DateFormatValidation.validate(_, CessationDateFormatError))
    ).flatten.flatten

    formatErrors match {
      case Nil =>
        val employmentStartDate     = LocalDate.parse(startDate, dateFormat)
        val employmentCessationDate = cessationDate.map(LocalDate.parse(_, dateFormat))

        List(
          Some(checkDateOrder(employmentStartDate, taxYearEndDate, RuleStartDateAfterTaxYearEndError)),
          employmentCessationDate.map(checkDateOrder(taxYearStartDate, _, RuleCessationDateBeforeTaxYearStartError)),
          employmentCessationDate.map(checkDateOrder(employmentStartDate, _, RuleCessationDateBeforeStartDateError))
        ).flatten.flatten

      case List(StartDateFormatError) =>
        val end = cessationDate.map(LocalDate.parse(_, dateFormat))

        List(
          Some(List(StartDateFormatError)),
          end.map(checkDateOrder(taxYearStartDate, _, RuleCessationDateBeforeTaxYearStartError))
        ).flatten.flatten

      case List(CessationDateFormatError) =>
        val start = LocalDate.parse(startDate, dateFormat)

        List(CessationDateFormatError) ++ checkDateOrder(start, taxYearEndDate, RuleStartDateAfterTaxYearEndError)

      case other => other
    }
  }

  private def checkDateOrder(start: LocalDate, end: LocalDate, error: MtdError): List[MtdError] =
    if (start.isAfter(end)) List(error) else NoValidationErrors

}
