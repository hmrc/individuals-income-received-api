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

package v1.models.request.amendOtherEmployment

import play.api.libs.functional.syntax._
import play.api.libs.json.{JsPath, Json, OWrites, Reads}

case class AmendSharesAwardedOrReceivedItem(employerName: String,
                                            employerRef: Option[String],
                                            schemePlanType: String,
                                            dateSharesCeasedToBeSubjectToPlan: String,
                                            noOfShareSecuritiesAwarded: BigInt,
                                            classOfShareAwarded: String,
                                            dateSharesAwarded: String,
                                            sharesSubjectToRestrictions: Boolean,
                                            electionEnteredIgnoreRestrictions: Boolean,
                                            actualMarketValueOfSharesOnAward: BigDecimal,
                                            unrestrictedMarketValueOfSharesOnAward: BigDecimal,
                                            amountPaidForSharesOnAward: BigDecimal,
                                            marketValueAfterRestrictionsLifted: BigDecimal,
                                            taxableAmount: BigDecimal)

object AmendSharesAwardedOrReceivedItem {
  implicit val reads: Reads[AmendSharesAwardedOrReceivedItem] = Json.reads[AmendSharesAwardedOrReceivedItem]

  implicit val writes: OWrites[AmendSharesAwardedOrReceivedItem] = (
    (JsPath \ "employerName").write[String] and
      (JsPath \ "employerRef").writeNullable[String] and
      (JsPath \ "schemePlanType").write[String] and
      (JsPath \ "dateSharesCeasedToBeSubjectToPlan").write[String] and
      (JsPath \ "noOfShareSecuritiesAwarded").write[BigInt] and
      (JsPath \ "classOfShareAwarded").write[String] and
      (JsPath \ "dateSharesAwarded").write[String] and
      (JsPath \ "sharesSubjectToRestrictions").write[Boolean] and
      (JsPath \ "electionEnteredIgnoreRestrictions").write[Boolean] and
      (JsPath \ "actualMarketValueOfSharesOnAward").write[BigDecimal] and
      (JsPath \ "unrestrictedMarketValueOfSharesOnAward").write[BigDecimal] and
      (JsPath \ "amountPaidForSharesOnAward").write[BigDecimal] and
      (JsPath \ "marketValueAfterRestrictionsLifted").write[BigDecimal] and
      (JsPath \ "taxableAmount").write[BigDecimal]
  )(unlift(AmendSharesAwardedOrReceivedItem.unapply))

}
