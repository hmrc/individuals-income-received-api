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
import play.api.libs.json.{JsPath, OWrites, Reads}
import utils.JsonUtils

case class AmendOtherRequestBody(businessReceipts: Option[Seq[AmendBusinessReceiptsItem]],
                                 allOtherIncomeReceivedWhilstAbroad: Option[Seq[AmendAllOtherIncomeReceivedWhilstAbroadItem]],
                                 overseasIncomeAndGains: Option[AmendOverseasIncomeAndGains],
                                 chargeableForeignBenefitsAndGifts: Option[AmendChargeableForeignBenefitsAndGifts],
                                 omittedForeignIncome: Option[AmendOmittedForeignIncome])

object AmendOtherRequestBody extends JsonUtils {
  val empty: AmendOtherRequestBody = AmendOtherRequestBody(None, None,None,None,None)

  implicit val reads: Reads[AmendOtherRequestBody] = (
    (JsPath \ "businessReceipts").readNullable[Seq[AmendBusinessReceiptsItem]].mapEmptySeqToNone and
      (JsPath \ "allOtherIncomeReceivedWhilstAbroad").readNullable[Seq[AmendAllOtherIncomeReceivedWhilstAbroadItem]].mapEmptySeqToNone and
      (JsPath \ "overseasIncomeAndGains").readNullable[AmendOverseasIncomeAndGains] and
      (JsPath \ "chargeableForeignBenefitsAndGifts").readNullable[AmendChargeableForeignBenefitsAndGifts].map(_.flatMap {
        case AmendChargeableForeignBenefitsAndGifts.empty => None
        case chargeableForeignBenefitsAndGifts => Some(chargeableForeignBenefitsAndGifts)
      }) and
      (JsPath \ "omittedForeignIncome").readNullable[AmendOmittedForeignIncome]
    ) (AmendOtherRequestBody.apply _)

  implicit val writes: OWrites[AmendOtherRequestBody] = (
    (JsPath \ "businessReceipts").writeNullable[Seq[AmendBusinessReceiptsItem]] and
      (JsPath \ "allOtherIncomeReceivedWhilstAbroad").writeNullable[Seq[AmendAllOtherIncomeReceivedWhilstAbroadItem]] and
      (JsPath \ "overseasIncomeAndGains").writeNullable[AmendOverseasIncomeAndGains] and
      (JsPath \ "chargeableForeignBenefitsAndGifts").writeNullable[AmendChargeableForeignBenefitsAndGifts] and
      (JsPath \ "omittedForeignIncome").writeNullable[AmendOmittedForeignIncome]
    ) (unlift(AmendOtherRequestBody.unapply))
}