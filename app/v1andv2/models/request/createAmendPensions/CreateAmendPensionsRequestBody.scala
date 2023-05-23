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

package v1andv2.models.request.createAmendPensions

import play.api.libs.functional.syntax._
import play.api.libs.json.{JsPath, OWrites, Reads}
import utils.JsonUtils

case class CreateAmendPensionsRequestBody(foreignPensions: Option[Seq[CreateAmendForeignPensionsItem]],
                                          overseasPensionContributions: Option[Seq[CreateAmendOverseasPensionContributions]])

object CreateAmendPensionsRequestBody extends JsonUtils {
  val empty: CreateAmendPensionsRequestBody = CreateAmendPensionsRequestBody(None, None)

  implicit val reads: Reads[CreateAmendPensionsRequestBody] = (
    (JsPath \ "foreignPensions").readNullable[Seq[CreateAmendForeignPensionsItem]].mapEmptySeqToNone and
      (JsPath \ "overseasPensionContributions").readNullable[Seq[CreateAmendOverseasPensionContributions]].mapEmptySeqToNone
  )(CreateAmendPensionsRequestBody.apply _)

  implicit val writes: OWrites[CreateAmendPensionsRequestBody] = (
    (JsPath \ "foreignPension").writeNullable[Seq[CreateAmendForeignPensionsItem]] and
      (JsPath \ "overseasPensionContribution").writeNullable[Seq[CreateAmendOverseasPensionContributions]]
  )(unlift(CreateAmendPensionsRequestBody.unapply))

}
