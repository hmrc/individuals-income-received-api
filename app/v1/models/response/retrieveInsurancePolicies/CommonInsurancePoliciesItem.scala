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

package v1.models.response.retrieveInsurancePolicies

import play.api.libs.functional.syntax._
import play.api.libs.json.{JsPath, Json, OWrites, Reads}

case class CommonInsurancePoliciesItem(customerReference: Option[String],
                                       event: Option[String],
                                       gainAmount: BigDecimal,
                                       taxPaid: Boolean,
                                       yearsHeld: Option[Int],
                                       yearsHeldSinceLastGain: Option[Int],
                                       deficiencyRelief: Option[BigDecimal])

object CommonInsurancePoliciesItem {

  implicit val reads: Reads[CommonInsurancePoliciesItem] = (
    (JsPath \ "customerReference").readNullable[String] and
      (JsPath \ "event").readNullable[String] and
      (JsPath \ "gainAmount").read[BigDecimal] and
      (JsPath \ "taxPaid").read[Boolean] and
      (JsPath \ "yearsHeld").readNullable[Int] and
      (JsPath \ "yearsHeldSinceLastGain").readNullable[Int] and
      (JsPath \ "deficiencyRelief").readNullable[BigDecimal]
  )(CommonInsurancePoliciesItem.apply _)

  implicit val writes: OWrites[CommonInsurancePoliciesItem] = Json.writes[CommonInsurancePoliciesItem]
}
