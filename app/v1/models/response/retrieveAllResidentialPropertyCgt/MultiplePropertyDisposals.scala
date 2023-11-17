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

package v1.models.response.retrieveAllResidentialPropertyCgt

import api.models.domain.{MtdSourceEnum, Timestamp}
import play.api.libs.functional.syntax._
import play.api.libs.json.{JsPath, Json, OWrites, Reads}

case class MultiplePropertyDisposals(source: MtdSourceEnum,
                                     submittedOn: Option[Timestamp],
                                     ppdSubmissionId: String,
                                     ppdSubmissionDate: Option[Timestamp],
                                     numberOfDisposals: Option[BigInt],
                                     disposalTaxYear: Option[BigInt],
                                     completionDate: Option[String],
                                     amountOfNetGain: Option[BigDecimal],
                                     amountOfNetLoss: Option[BigDecimal])

object MultiplePropertyDisposals {

  implicit val reads: Reads[MultiplePropertyDisposals] = (
    (JsPath \ "source").read[DownstreamSourceEnum].map(_.toMtdEnum) and
      (JsPath \ "submittedOn").readNullable[Timestamp] and
      (JsPath \ "ppdSubmissionId").read[String] and
      (JsPath \ "ppdSubmissionDate").readNullable[Timestamp] and
      (JsPath \ "numberOfDisposals").readNullable[BigInt] and
      (JsPath \ "disposalTaxYear").readNullable[String].map(_.map(BigInt(_))) and
      (JsPath \ "completionDate").readNullable[String] and
      (JsPath \ "amountOfNetGain").readNullable[BigDecimal] and
      (JsPath \ "amountOfLoss").readNullable[BigDecimal]
    )(MultiplePropertyDisposals.apply _)

  implicit val writes: OWrites[MultiplePropertyDisposals] = Json.writes[MultiplePropertyDisposals]

}
