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

package v2.models.request.createAmendOther

import play.api.libs.functional.syntax._
import play.api.libs.json.{JsPath, OWrites, Reads}
import utils.JsonUtils

case class CreateAmendOtherRequestBody(postCessationReceipts: Option[Seq[PostCessationReceiptsItem]],
                                       businessReceipts: Option[Seq[BusinessReceiptsItem]],
                                       allOtherIncomeReceivedWhilstAbroad: Option[Seq[AllOtherIncomeReceivedWhilstAbroadItem]],
                                       overseasIncomeAndGains: Option[OverseasIncomeAndGains],
                                       chargeableForeignBenefitsAndGifts: Option[ChargeableForeignBenefitsAndGifts],
                                       omittedForeignIncome: Option[OmittedForeignIncome])

object CreateAmendOtherRequestBody extends JsonUtils {
  val empty: CreateAmendOtherRequestBody = CreateAmendOtherRequestBody(None, None, None, None, None, None)

  implicit val reads: Reads[CreateAmendOtherRequestBody] = (
    (JsPath \ "postCessationReceipts").readNullable[Seq[PostCessationReceiptsItem]].mapEmptySeqToNone and
      (JsPath \ "businessReceipts").readNullable[Seq[BusinessReceiptsItem]].mapEmptySeqToNone and
      (JsPath \ "allOtherIncomeReceivedWhilstAbroad").readNullable[Seq[AllOtherIncomeReceivedWhilstAbroadItem]].mapEmptySeqToNone and
      (JsPath \ "overseasIncomeAndGains").readNullable[OverseasIncomeAndGains] and
      (JsPath \ "chargeableForeignBenefitsAndGifts")
        .readNullable[ChargeableForeignBenefitsAndGifts]
        .map(_.flatMap {
          case ChargeableForeignBenefitsAndGifts.empty => None
          case chargeableForeignBenefitsAndGifts       => Some(chargeableForeignBenefitsAndGifts)
        }) and
      (JsPath \ "omittedForeignIncome").readNullable[OmittedForeignIncome]
  )(CreateAmendOtherRequestBody.apply _)

  implicit val writes: OWrites[CreateAmendOtherRequestBody] = (
    (JsPath \ "postCessationReceipts").writeNullable[Seq[PostCessationReceiptsItem]] and
      (JsPath \ "businessReceipts").writeNullable[Seq[BusinessReceiptsItem]] and
      (JsPath \ "allOtherIncomeReceivedWhilstAbroad").writeNullable[Seq[AllOtherIncomeReceivedWhilstAbroadItem]] and
      (JsPath \ "overseasIncomeAndGains").writeNullable[OverseasIncomeAndGains] and
      (JsPath \ "chargeableForeignBenefitsAndGifts").writeNullable[ChargeableForeignBenefitsAndGifts] and
      (JsPath \ "omittedForeignIncome").writeNullable[OmittedForeignIncome]
  )(unlift(CreateAmendOtherRequestBody.unapply))

}
