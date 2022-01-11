/*
 * Copyright 2022 HM Revenue & Customs
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

package v1r6.models.request.createAmendCgtPpdOverrides

import play.api.libs.functional.syntax._
import play.api.libs.json.{JsPath, Json, OWrites, Reads}
import utils.JsonUtils

case class CreateAmendCgtPpdOverridesRequestBody(multiplePropertyDisposals: Option[Seq[MultiplePropertyDisposals]],
                                                 singlePropertyDisposals: Option[Seq[SinglePropertyDisposals]])

object CreateAmendCgtPpdOverridesRequestBody extends JsonUtils {
  val empty: CreateAmendCgtPpdOverridesRequestBody = CreateAmendCgtPpdOverridesRequestBody(None, None)

  implicit val reads: Reads[CreateAmendCgtPpdOverridesRequestBody] =  Json.format[CreateAmendCgtPpdOverridesRequestBody]

  implicit val writes: OWrites[CreateAmendCgtPpdOverridesRequestBody] = (
    (JsPath \ "multiplePropertyDisposals").writeNullable[Seq[MultiplePropertyDisposals]] and
      (JsPath \ "singlePropertyDisposals").writeNullable[Seq[SinglePropertyDisposals]]
    ) (unlift(CreateAmendCgtPpdOverridesRequestBody.unapply))
}
