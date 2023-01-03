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

package v1.models.response.retrieveOtherEmployment

import play.api.libs.functional.syntax._
import play.api.libs.json.{JsPath, Json, OWrites, Reads}

case class LumpSums(employerName: String,
                    employerRef: String,
                    taxableLumpSumsAndCertainIncome: Option[TaxableLumpSumsAndCertainIncomeItem],
                    benefitFromEmployerFinancedRetirementScheme: Option[BenefitFromEmployerFinancedRetirementSchemeItem],
                    redundancyCompensationPaymentsOverExemption: Option[RedundancyCompensationPaymentsOverExemptionItem],
                    redundancyCompensationPaymentsUnderExemption: Option[RedundancyCompensationPaymentsUnderExemptionItem])

object LumpSums {

  implicit val reads: Reads[LumpSums] = (
    (JsPath \ "employerName").read[String] and
      (JsPath \ "employerRef").read[String] and
      (JsPath \ "taxableLumpSumsAndCertainIncome").readNullable[TaxableLumpSumsAndCertainIncomeItem] and
      (JsPath \ "benefitFromEmployerFinancedRetirementScheme").readNullable[BenefitFromEmployerFinancedRetirementSchemeItem] and
      (JsPath \ "redundancyCompensationPaymentsOverExemption").readNullable[RedundancyCompensationPaymentsOverExemptionItem] and
      (JsPath \ "redundancyCompensationPaymentsUnderExemption").readNullable[RedundancyCompensationPaymentsUnderExemptionItem]
  )(LumpSums.apply _)

  implicit val writes: OWrites[LumpSums] = Json.writes[LumpSums]
}
