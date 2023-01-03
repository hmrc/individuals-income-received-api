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

package v1.models.response.retrieveOtherEmployment

import api.models.domain.SharesAwardedOrReceivedSchemeType
import play.api.libs.functional.syntax._
import play.api.libs.json.{JsPath, Json, OWrites, Reads}

case class SharesAwardedOrReceivedItem(employerName: String,
                                       employerRef: Option[String],
                                       schemePlanType: SharesAwardedOrReceivedSchemeType,
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

object SharesAwardedOrReceivedItem {

  implicit val reads: Reads[SharesAwardedOrReceivedItem] = (
    (JsPath \ "employerName").read[String] and
      (JsPath \ "employerRef").readNullable[String] and
      (JsPath \ "schemePlanType").read[SharesAwardedOrReceivedSchemeType] and
      (JsPath \ "dateSharesCeasedToBeSubjectToPlan").read[String] and
      (JsPath \ "noOfShareSecuritiesAwarded").read[BigInt] and
      (JsPath \ "classOfShareAwarded").read[String] and
      (JsPath \ "dateSharesAwarded").read[String] and
      (JsPath \ "sharesSubjectToRestrictions").read[Boolean] and
      (JsPath \ "electionEnteredIgnoreRestrictions").read[Boolean] and
      (JsPath \ "actualMarketValueOfSharesOnAward").read[BigDecimal] and
      (JsPath \ "unrestrictedMarketValueOfSharesOnAward").read[BigDecimal] and
      (JsPath \ "amountPaidForSharesOnAward").read[BigDecimal] and
      (JsPath \ "marketValueAfterRestrictionsLifted").read[BigDecimal] and
      (JsPath \ "taxableAmount").read[BigDecimal]
  )(SharesAwardedOrReceivedItem.apply _)

  implicit val writes: OWrites[SharesAwardedOrReceivedItem] = Json.writes[SharesAwardedOrReceivedItem]
}
