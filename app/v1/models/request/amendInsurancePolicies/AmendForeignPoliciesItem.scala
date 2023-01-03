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

package v1.models.request.amendInsurancePolicies

import play.api.libs.functional.syntax._
import play.api.libs.json.{JsPath, Json, OWrites, Reads}

case class AmendForeignPoliciesItem(customerReference: Option[String],
                                    gainAmount: BigDecimal,
                                    taxPaidAmount: Option[BigDecimal],
                                    yearsHeld: Option[Int])

object AmendForeignPoliciesItem {
  implicit val reads: Reads[AmendForeignPoliciesItem] = Json.reads[AmendForeignPoliciesItem]

  implicit val writes: OWrites[AmendForeignPoliciesItem] = (
    (JsPath \ "customerReference").writeNullable[String] and
      (JsPath \ "gainAmount").write[BigDecimal] and
      (JsPath \ "taxPaidAmount").writeNullable[BigDecimal] and
      (JsPath \ "yearsHeld").writeNullable[Int]
  )(unlift(AmendForeignPoliciesItem.unapply))

}
