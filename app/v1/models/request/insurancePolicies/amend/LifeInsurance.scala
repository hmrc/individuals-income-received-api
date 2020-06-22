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

package v1.models.request.insurancePolicies.amend

import play.api.libs.functional.syntax._
import play.api.libs.json.{JsPath, Json, OWrites, Reads}

case class LifeInsurance(customerReference: Option[String],
                         event: Option[String],
                         gainAmount: BigDecimal,
                         taxPaid: Option[BigDecimal],
                         yearsHeld: Option[Int],
                         yearsHeldSinceLastGain: Option[Int],
                         deficiencyRelief: Option[BigDecimal])


object LifeInsurance {

  implicit val reads: Reads[LifeInsurance] = Json.reads[LifeInsurance]

  implicit val writes: OWrites[LifeInsurance] = (
    (JsPath \ "customerReference").writeNullable[String] and
      (JsPath \ "event").writeNullable[String] and
      (JsPath \ "gainAmount").write[BigDecimal] and
      (JsPath \ "taxPaid").writeNullable[BigDecimal] and
      (JsPath \ "yearsHeld").writeNullable[Int] and
      (JsPath \ "yearsHeldSinceLastGain").writeNullable[Int] and
      (JsPath \ "deficiencyRelief").writeNullable[BigDecimal]
    ) (unlift(LifeInsurance.unapply))
}