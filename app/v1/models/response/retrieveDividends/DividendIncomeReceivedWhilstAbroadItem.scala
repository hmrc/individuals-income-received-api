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

package v1.models.response.retrieveDividends

import play.api.libs.functional.syntax._
import play.api.libs.json.{JsPath, Json, OWrites, Reads}

case class DividendIncomeReceivedWhilstAbroadItem(countryCode: String,
                                                  amountBeforeTax: Option[BigDecimal],
                                                  taxTakenOff: Option[BigDecimal],
                                                  specialWithholdingTax: Option[BigDecimal],
                                                  foreignTaxCreditRelief: Boolean,
                                                  taxableAmount: BigDecimal
                                                 )

object DividendIncomeReceivedWhilstAbroadItem {
  implicit val reads: Reads[DividendIncomeReceivedWhilstAbroadItem] = (
    (JsPath \ "countryCode").read[String] and
      (JsPath \ "amountBeforeTax").readNullable[BigDecimal] and
      (JsPath \ "taxTakenOff").readNullable[BigDecimal] and
      (JsPath \ "specialWithholdingTax").readNullable[BigDecimal] and
      (JsPath \ "foreignTaxCreditRelief").read[Boolean] and
      (JsPath \ "taxableAmount").read[BigDecimal]
    ) (DividendIncomeReceivedWhilstAbroadItem.apply _)

  implicit val writes: OWrites[DividendIncomeReceivedWhilstAbroadItem] = Json.writes[DividendIncomeReceivedWhilstAbroadItem]

}
