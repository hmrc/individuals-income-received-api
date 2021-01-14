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

package v1.models.request.amendDividends

import play.api.libs.functional.syntax._
import play.api.libs.json.{JsPath, OWrites, Reads}
import utils.JsonUtils

case class AmendDividendsRequestBody(foreignDividend: Option[Seq[AmendForeignDividendItem]],
                                     dividendIncomeReceivedWhilstAbroad: Option[Seq[AmendDividendIncomeReceivedWhilstAbroadItem]],
                                     stockDividend: Option[AmendCommonDividends],
                                     redeemableShares: Option[AmendCommonDividends],
                                     bonusIssuesOfSecurities: Option[AmendCommonDividends],
                                     closeCompanyLoansWrittenOff: Option[AmendCommonDividends])

object AmendDividendsRequestBody extends JsonUtils {
  val empty: AmendDividendsRequestBody = AmendDividendsRequestBody(None, None, None, None, None, None)

  implicit val reads: Reads[AmendDividendsRequestBody] = (
    (JsPath \ "foreignDividend").readNullable[Seq[AmendForeignDividendItem]].mapEmptySeqToNone and
      (JsPath \ "dividendIncomeReceivedWhilstAbroad").readNullable[Seq[AmendDividendIncomeReceivedWhilstAbroadItem]].mapEmptySeqToNone and
      (JsPath \ "stockDividend").readNullable[AmendCommonDividends] and
      (JsPath \ "redeemableShares").readNullable[AmendCommonDividends] and
      (JsPath \ "bonusIssuesOfSecurities").readNullable[AmendCommonDividends] and
      (JsPath \ "closeCompanyLoansWrittenOff").readNullable[AmendCommonDividends]
    ) (AmendDividendsRequestBody.apply _)

  implicit val writes: OWrites[AmendDividendsRequestBody] = (
    (JsPath \ "foreignDividend").writeNullable[Seq[AmendForeignDividendItem]] and
      (JsPath \ "dividendIncomeReceivedWhilstAbroad").writeNullable[Seq[AmendDividendIncomeReceivedWhilstAbroadItem]] and
      (JsPath \ "stockDividend").writeNullable[AmendCommonDividends] and
      (JsPath \ "redeemableShares").writeNullable[AmendCommonDividends] and
      (JsPath \ "bonusIssuesOfSecurities").writeNullable[AmendCommonDividends] and
      (JsPath \ "closeCompanyLoansWrittenOff").writeNullable[AmendCommonDividends]
    ) (unlift(AmendDividendsRequestBody.unapply))
}