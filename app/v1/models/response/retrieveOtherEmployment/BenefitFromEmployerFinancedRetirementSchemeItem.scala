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

case class BenefitFromEmployerFinancedRetirementSchemeItem(amount: BigDecimal,
                                                           exemptAmount: Option[BigDecimal],
                                                           taxPaid: Option[BigDecimal],
                                                           taxTakenOffInEmployment: Boolean)

object BenefitFromEmployerFinancedRetirementSchemeItem {

  implicit val reads: Reads[BenefitFromEmployerFinancedRetirementSchemeItem] = (
    (JsPath \ "amount").read[BigDecimal] and
      (JsPath \ "exemptAmount").readNullable[BigDecimal] and
      (JsPath \ "taxPaid").readNullable[BigDecimal] and
      (JsPath \ "taxTakenOffInEmployment").read[Boolean]
  )(BenefitFromEmployerFinancedRetirementSchemeItem.apply _)

  implicit val writes: OWrites[BenefitFromEmployerFinancedRetirementSchemeItem] = Json.writes[BenefitFromEmployerFinancedRetirementSchemeItem]
}
