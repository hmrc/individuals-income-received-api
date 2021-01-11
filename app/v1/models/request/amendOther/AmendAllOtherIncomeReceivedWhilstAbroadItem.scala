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

package v1.models.request.amendOther

import play.api.libs.functional.syntax._
import play.api.libs.json.{JsPath, Json, OWrites, Reads}

case class AmendAllOtherIncomeReceivedWhilstAbroadItem(countryCode: String,
                                                       amountBeforeTax: Option[BigDecimal],
                                                       taxTakenOff: Option[BigDecimal],
                                                       specialWithholdingTax: Option[BigDecimal],
                                                       foreignTaxCreditRelief: Boolean,
                                                       taxableAmount: BigDecimal,
                                                       residentialFinancialCostAmount: Option[BigDecimal],
                                                       broughtFwdResidentialFinancialCostAmount: Option[BigDecimal])

object AmendAllOtherIncomeReceivedWhilstAbroadItem {
  implicit val reads: Reads[AmendAllOtherIncomeReceivedWhilstAbroadItem] = Json.reads[AmendAllOtherIncomeReceivedWhilstAbroadItem]

  implicit val writes: OWrites[AmendAllOtherIncomeReceivedWhilstAbroadItem] = (
    (JsPath \ "countryCode").write[String] and
      (JsPath \ "amountBeforeTax").writeNullable[BigDecimal] and
      (JsPath \ "taxTakenOff").writeNullable[BigDecimal] and
      (JsPath \ "specialWithholdingTax").writeNullable[BigDecimal] and
      (JsPath \ "foreignTaxCreditRelief").write[Boolean] and
      (JsPath \ "taxableAmount").write[BigDecimal] and
      (JsPath \ "residentialFinancialCostAmount").writeNullable[BigDecimal] and
      (JsPath \ "broughtFwdResidentialFinancialCostAmount").writeNullable[BigDecimal]
    ) (unlift(AmendAllOtherIncomeReceivedWhilstAbroadItem.unapply))
}