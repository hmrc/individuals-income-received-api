/*
 * Copyright 2020 HM Revenue & Customs
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

package v1.models.response.retrieveFinancialDetails

import play.api.libs.json.{Json, OFormat}

case class Employment(employmentSequenceNumber: Option[String],
                      payrollId: Option[String],
                      companyDirector: Option[Boolean],
                      closeCompany: Option[Boolean],
                      directorshipCeasedDate: Option[String],
                      startDate: Option[String],
                      cessationDate: Option[String],
                      occupationalPension: Option[Boolean],
                      disguisedRemuneration: Option[Boolean],
                      employer: Employer,
                      pay: Pay,
                      customerEstimatedPay: Option[CustomerEstimatedPay],
                      deductions: Option[Deductions],
                      benefitsInKind: Option[BenefitsInKind])

object Employment {
  implicit val format: OFormat[Employment] = Json.format[Employment]
}