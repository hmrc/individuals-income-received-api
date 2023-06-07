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

package v2.models.request.createAmendDividends

import play.api.libs.functional.syntax._
import play.api.libs.json.{JsPath, OWrites, Reads}
import utils.JsonUtils

case class CreateAmendDividendsRequestBody(foreignDividend: Option[Seq[CreateAmendForeignDividendItem]],
                                           dividendIncomeReceivedWhilstAbroad: Option[Seq[CreateAmendDividendIncomeReceivedWhilstAbroadItem]],
                                           stockDividend: Option[CreateAmendCommonDividends],
                                           redeemableShares: Option[CreateAmendCommonDividends],
                                           bonusIssuesOfSecurities: Option[CreateAmendCommonDividends],
                                           closeCompanyLoansWrittenOff: Option[CreateAmendCommonDividends])

object CreateAmendDividendsRequestBody extends JsonUtils {
  val empty: CreateAmendDividendsRequestBody = CreateAmendDividendsRequestBody(None, None, None, None, None, None)

  implicit val reads: Reads[CreateAmendDividendsRequestBody] = (
    (JsPath \ "foreignDividend").readNullable[Seq[CreateAmendForeignDividendItem]].mapEmptySeqToNone and
      (JsPath \ "dividendIncomeReceivedWhilstAbroad").readNullable[Seq[CreateAmendDividendIncomeReceivedWhilstAbroadItem]].mapEmptySeqToNone and
      (JsPath \ "stockDividend").readNullable[CreateAmendCommonDividends] and
      (JsPath \ "redeemableShares").readNullable[CreateAmendCommonDividends] and
      (JsPath \ "bonusIssuesOfSecurities").readNullable[CreateAmendCommonDividends] and
      (JsPath \ "closeCompanyLoansWrittenOff").readNullable[CreateAmendCommonDividends]
  )(CreateAmendDividendsRequestBody.apply _)

  implicit val writes: OWrites[CreateAmendDividendsRequestBody] = (
    (JsPath \ "foreignDividend").writeNullable[Seq[CreateAmendForeignDividendItem]] and
      (JsPath \ "dividendIncomeReceivedWhilstAbroad").writeNullable[Seq[CreateAmendDividendIncomeReceivedWhilstAbroadItem]] and
      (JsPath \ "stockDividend").writeNullable[CreateAmendCommonDividends] and
      (JsPath \ "redeemableShares").writeNullable[CreateAmendCommonDividends] and
      (JsPath \ "bonusIssuesOfSecurities").writeNullable[CreateAmendCommonDividends] and
      (JsPath \ "closeCompanyLoansWrittenOff").writeNullable[CreateAmendCommonDividends]
  )(unlift(CreateAmendDividendsRequestBody.unapply))

}
