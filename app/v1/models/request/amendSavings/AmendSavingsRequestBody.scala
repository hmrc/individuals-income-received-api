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

package v1.models.request.amendSavings

import play.api.libs.functional.syntax._
import play.api.libs.json.{JsPath, OWrites, Reads}
import utils.JsonUtils

case class AmendSavingsRequestBody(securities: Option[AmendSecurities], foreignInterest: Option[Seq[AmendForeignInterestItem]])

object AmendSavingsRequestBody extends JsonUtils {
  val empty: AmendSavingsRequestBody = AmendSavingsRequestBody(None, None)

  implicit val reads: Reads[AmendSavingsRequestBody] = (
    (JsPath \ "securities").readNullable[AmendSecurities] and
      (JsPath \ "foreignInterest").readNullable[Seq[AmendForeignInterestItem]].mapEmptySeqToNone
    ) (AmendSavingsRequestBody.apply _)

  implicit val writes: OWrites[AmendSavingsRequestBody] = (
    (JsPath \ "securities").writeNullable[AmendSecurities] and
      (JsPath \ "foreignInterest").writeNullable[Seq[AmendForeignInterestItem]]
    ) (unlift(AmendSavingsRequestBody.unapply))
}
