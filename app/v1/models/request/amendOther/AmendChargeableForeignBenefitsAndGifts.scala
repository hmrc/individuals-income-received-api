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

case class AmendChargeableForeignBenefitsAndGifts(transactionBenefit: Option[BigDecimal],
                                                  protectedForeignIncomeSourceBenefit: Option[BigDecimal],
                                                  protectedForeignIncomeOnwardGift: Option[BigDecimal],
                                                  benefitReceivedAsASettler: Option[BigDecimal],
                                                  onwardGiftReceivedAsASettler: Option[BigDecimal])

object AmendChargeableForeignBenefitsAndGifts {
  val empty: AmendChargeableForeignBenefitsAndGifts = AmendChargeableForeignBenefitsAndGifts(None, None, None, None, None)

  implicit val reads: Reads[AmendChargeableForeignBenefitsAndGifts] = Json.reads[AmendChargeableForeignBenefitsAndGifts]

  implicit val writes: OWrites[AmendChargeableForeignBenefitsAndGifts] = (
    (JsPath \ "transactionBenefit").writeNullable[BigDecimal] and
      (JsPath \ "protectedForeignIncomeSourceBenefit").writeNullable[BigDecimal] and
      (JsPath \ "protectedForeignIncomeOnwardGift").writeNullable[BigDecimal] and
      (JsPath \ "benefitReceivedAsASettler").writeNullable[BigDecimal] and
      (JsPath \ "onwardGiftReceivedAsASettler").writeNullable[BigDecimal]
    ) (unlift(AmendChargeableForeignBenefitsAndGifts.unapply))
}