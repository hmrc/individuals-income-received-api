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

package v1.models.request.createAmendCgtPpdOverrides

import play.api.libs.functional.syntax._
import play.api.libs.json.{JsPath, Json, OWrites, Reads}

case class MultiplePropertyDisposals(submissionId: String,
                                     amountOfNetGain: Option[BigDecimal],
                                     amountOfNetLoss: Option[BigDecimal])

object MultiplePropertyDisposals {
  implicit val reads: Reads[MultiplePropertyDisposals] = Json.reads[MultiplePropertyDisposals]

  implicit val writes: OWrites[MultiplePropertyDisposals] = (
    (JsPath \ "ppdSubmissionId").write[String] and
      (JsPath \ "amountOfNetGain").writeNullable[BigDecimal] and
      (JsPath \ "amountOfLoss").writeNullable[BigDecimal]
  ) (unlift(MultiplePropertyDisposals.unapply))
}