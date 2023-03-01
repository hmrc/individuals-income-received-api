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

package v1.models.request.amendFinancialDetails.emploment

import play.api.libs.json.{JsObject, Json, Reads, Writes}

case class AmendEmployment(pay: AmendPay,
                           deductions: Option[AmendDeductions],
                           benefitsInKind: Option[AmendBenefitsInKind],
                           offPayrollWorker: Option[Boolean])

object AmendEmployment {

  implicit val reads: Reads[AmendEmployment] = Json.reads[AmendEmployment]

  implicit val amendEmploymentWrites = new Writes[AmendEmployment] {

    def writes(amendEmployment: AmendEmployment): JsObject = {

      val append = amendEmployment.offPayrollWorker match {
        case Some(true) => Json.obj("offPayrollWorker" -> Some(true))
        case _          => JsObject.empty
      }

      val result = Json.obj(
        "pay"            -> amendEmployment.pay,
        "deductions"     -> amendEmployment.deductions,
        "benefitsInKind" -> amendEmployment.benefitsInKind
      )

      result ++ append

    }

  }

}
