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
import play.api.libs.json.{JsPath, OWrites, Reads}
import utils.JsonUtils

case class AmendOtherEmploymentRequestBody(shareOption: Option[Seq[AmendShareOptionItem]],
                                           sharesAwardedOrReceived: Option[Seq[AmendSharesAwardedOrReceivedItem]],
                                           disability: Option[AmendCommonOtherEmployment],
                                           foreignService: Option[AmendCommonOtherEmployment],
                                           lumpSums: Option[Seq[AmendLumpSums]])

object AmendOtherEmploymentRequestBody extends JsonUtils {
  val empty: AmendOtherEmploymentRequestBody = AmendOtherEmploymentRequestBody(None, None, None, None, None)

  implicit val reads: Reads[AmendOtherEmploymentRequestBody] = (
    (JsPath \ "shareOption").readNullable[Seq[AmendShareOptionItem]].mapEmptySeqToNone and
      (JsPath \ "sharesAwardedOrReceived").readNullable[Seq[AmendSharesAwardedOrReceivedItem]].mapEmptySeqToNone and
      (JsPath \ "disability").readNullable[AmendCommonOtherEmployment] and
      (JsPath \ "foreignService").readNullable[AmendCommonOtherEmployment] and
      (JsPath \ "lumpSums").readNullable[Seq[AmendLumpSums]].mapEmptySeqToNone
  )(AmendOtherEmploymentRequestBody.apply _)

  implicit val writes: OWrites[AmendOtherEmploymentRequestBody] = (
    (JsPath \ "shareOption").writeNullable[Seq[AmendShareOptionItem]] and
      (JsPath \ "sharesAwardedOrReceived").writeNullable[Seq[AmendSharesAwardedOrReceivedItem]] and
      (JsPath \ "disability").writeNullable[AmendCommonOtherEmployment] and
      (JsPath \ "foreignService").writeNullable[AmendCommonOtherEmployment] and
      (JsPath \ "lumpSums").writeNullable[Seq[AmendLumpSums]]
  )(unlift(AmendOtherEmploymentRequestBody.unapply))

}
