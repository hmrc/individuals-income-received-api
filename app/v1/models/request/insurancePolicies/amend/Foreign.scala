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

case class Foreign(customerReference: String,
                   gainAmount: Option[BigDecimal],
                   taxPaid: Option[BigDecimal],
                   yearsHeld: Option[Int])

object Foreign {

  implicit val reads: Reads[Foreign] = Json.reads[Foreign]

  implicit val writes: OWrites[Foreign] = (
    (JsPath \ "customerReference").write[String] and
      (JsPath \ "gainAmount").writeNullable[BigDecimal] and
      (JsPath \ "taxPaid").writeNullable[BigDecimal] and
      (JsPath \ "yearsHeld").writeNullable[Int]
    ) (unlift(Foreign.unapply))

}