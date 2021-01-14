/*
 * Copyright 2021 HM Revenue & Customs
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

package v1.models.response.retrieveSavings

import play.api.libs.functional.syntax._
import play.api.libs.json.{JsPath, Json, OWrites, Reads}

case class ForeignInterestItem(amountBeforeTax: Option[BigDecimal],
                               countryCode: String,
                               taxTakenOff: Option[BigDecimal],
                               specialWithholdingTax: Option[BigDecimal],
                               taxableAmount: BigDecimal,
                               foreignTaxCreditRelief: Boolean)

object ForeignInterestItem {
  implicit val reads: Reads[ForeignInterestItem] = (
    (JsPath \ "amountBeforeTax").readNullable[BigDecimal] and
      (JsPath \ "countryCode").read[String] and
      (JsPath \ "taxTakenOff").readNullable[BigDecimal] and
      (JsPath \ "specialWithholdingTax").readNullable[BigDecimal] and
      (JsPath \ "taxableAmount").read[BigDecimal] and
      (JsPath \ "foreignTaxCreditRelief").read[Boolean]
    ) (ForeignInterestItem.apply _)

  implicit val writes: OWrites[ForeignInterestItem] = Json.writes[ForeignInterestItem]
}
