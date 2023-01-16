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

package v1.models.response.retrieveFinancialDetails

import play.api.libs.functional.syntax._
import play.api.libs.json.{JsPath, Json, OWrites, Reads}

case class Employment(employmentSequenceNumber: Option[String],
                      payrollId: Option[String],
                      companyDirector: Option[Boolean],
                      closeCompany: Option[Boolean],
                      directorshipCeasedDate: Option[String],
                      startDate: Option[String],
                      cessationDate: Option[String],
                      occupationalPension: Option[Boolean],
                      disguisedRemuneration: Option[Boolean],
                      offPayrollWorker: Option[Boolean],
                      employer: Employer,
                      pay: Option[Pay],
                      customerEstimatedPay: Option[CustomerEstimatedPay],
                      deductions: Option[Deductions],
                      benefitsInKind: Option[BenefitsInKind])

object Employment {
  implicit val writes: OWrites[Employment] = Json.writes[Employment]

  implicit val reads: Reads[Employment] = (
    (JsPath \ "employmentSequenceNumber").readNullable[String] and
      (JsPath \ "payrollId").readNullable[String] and
      (JsPath \ "companyDirector").readNullable[Boolean] and
      (JsPath \ "closeCompany").readNullable[Boolean] and
      (JsPath \ "directorshipCeasedDate").readNullable[String] and
      (JsPath \ "startDate").readNullable[String] and
      (JsPath \ "cessationDate").readNullable[String] and
      (JsPath \ "occPen").readNullable[Boolean] and
      (JsPath \ "disguisedRemuneration").readNullable[Boolean] and
      (JsPath \ "offPayrollWorker").readNullable[Boolean] and
      (JsPath \ "employer").read[Employer] and
      (JsPath \ "pay").readNullable[Pay] and
      (JsPath \ "estimatedPay").readNullable[CustomerEstimatedPay].map {
        case Some(CustomerEstimatedPay.empty) => None
        case other                            => other
      } and
      (JsPath \ "deductions").readNullable[Deductions].map {
        case Some(Deductions.empty) => None
        case other                  => other
      } and
      (JsPath \ "benefitsInKind").readNullable[BenefitsInKind].map {
        case Some(BenefitsInKind.empty) => None
        case other                      => other
      }
  )(Employment.apply _)

}
