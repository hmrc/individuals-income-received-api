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

package v1.models.response.retrieveAllResidentialPropertyCgt

import play.api.libs.functional.syntax._
import play.api.libs.json.{JsPath, Json, OWrites, Reads}
import v1.models.domain.MtdSourceEnum

case class SinglePropertyDisposals(source: MtdSourceEnum,
                                   submittedOn: Option[String],
                                   ppdSubmissionId: String,
                                   ppdSubmissionDate: Option[String],
                                   disposalDate: Option[String],
                                   completionDate: String,
                                   disposalProceeds: BigDecimal,
                                   acquisitionDate: String,
                                   acquisitionAmount: BigDecimal,
                                   improvementCosts: Option[BigDecimal],
                                   additionalCosts : Option[BigDecimal],
                                   prfAmount: Option[BigDecimal],
                                   otherReliefAmount: Option[BigDecimal],
                                   lossesFromThisYear: Option[BigDecimal],
                                   lossesFromPreviousYear: Option[BigDecimal],
                                   amountOfNetGain : Option[BigDecimal],
                                   amountOfNetLoss : Option[BigDecimal],
                                   ppdReturnCharge: Option[BigDecimal]
                                  )

object SinglePropertyDisposals {
  implicit val reads: Reads[SinglePropertyDisposals] = (
    (JsPath \ "source").read[DownstreamSourceEnum].map(_.toMtdEnum) and
      (JsPath \ "submittedOn").readNullable[String] and
      (JsPath \ "ppdSubmissionId").read[String] and
      (JsPath \ "ppdSubmissionDate").readNullable[String] and
      (JsPath \ "disposalDate").readNullable[String] and
      (JsPath \ "completionDate").read[String] and
      (JsPath \ "disposalProceeds").read[BigDecimal] and
      (JsPath \ "acquisitionDate").read[String] and
      (JsPath \ "acquisitionAmount").read[BigDecimal] and
      (JsPath \ "improvementCosts").readNullable[BigDecimal] and
      (JsPath \ "additionalCosts").readNullable[BigDecimal] and
      (JsPath \ "prfAmount").readNullable[BigDecimal] and
      (JsPath \ "otherReliefAmount").readNullable[BigDecimal] and
      (JsPath \ "lossesFromThisYear").readNullable[BigDecimal] and
      (JsPath \ "lossesFromPreviousYear").readNullable[BigDecimal] and
      (JsPath \ "amountOfNetGain").readNullable[BigDecimal] and
      (JsPath \ "amountOfLoss").readNullable[BigDecimal] and
      (JsPath \ "ppdReturnCharge").readNullable[BigDecimal]
    ) (SinglePropertyDisposals.apply _)

  implicit val writes: OWrites[SinglePropertyDisposals] = Json.writes[SinglePropertyDisposals]
}
