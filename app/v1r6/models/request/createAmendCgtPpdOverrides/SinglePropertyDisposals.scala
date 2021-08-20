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

package v1r6.models.request.createAmendCgtPpdOverrides

import play.api.libs.functional.syntax._
import play.api.libs.json.{JsPath, Json, OWrites, Reads}

case class SinglePropertyDisposals(ppdSubmissionId: String,
                                   completionDate: String,
                                   disposalProceeds: BigDecimal,
                                   acquisitionDate: String,
                                   acquisitionAmount: BigDecimal,
                                   improvementCosts: BigDecimal,
                                   additionalCosts: BigDecimal,
                                   prfAmount: BigDecimal,
                                   otherReliefAmount: BigDecimal,
                                   lossesFromThisYear: Option[BigDecimal],
                                   lossesFromPreviousYear: Option[BigDecimal],
                                   amountOfNetGain: Option[BigDecimal],
                                   amountOfNetLoss: Option[BigDecimal]) {

  def isNetGainEmpty: Boolean = amountOfNetGain.isEmpty
  def isNetLossEmpty: Boolean = amountOfNetLoss.isEmpty
  def isBothSupplied: Boolean = !isNetGainEmpty && ! isNetLossEmpty
  def isBothEmpty: Boolean = isNetGainEmpty && isNetLossEmpty
}

object SinglePropertyDisposals {
  implicit val reads: Reads[SinglePropertyDisposals] = Json.reads[SinglePropertyDisposals]

  implicit val writes: OWrites[SinglePropertyDisposals] = (
    (JsPath \ "ppdSubmissionId").write[String] and
      (JsPath \ "completionDate").write[String] and
      (JsPath \ "disposalProceeds").write[BigDecimal] and
      (JsPath \ "acquisitionDate").write[String] and
      (JsPath \ "acquisitionAmount").write[BigDecimal] and
      (JsPath \ "improvementCosts").write[BigDecimal] and
      (JsPath \ "additionalCosts").write[BigDecimal] and
      (JsPath \ "prfAmount").write[BigDecimal] and
      (JsPath \ "otherReliefAmount").write[BigDecimal] and
      (JsPath \ "lossesFromThisYear").writeNullable[BigDecimal] and
      (JsPath \ "lossesFromPreviousYear").writeNullable[BigDecimal] and
      (JsPath \ "amountOfNetGain").writeNullable[BigDecimal] and
      (JsPath \ "amountOfLoss").writeNullable[BigDecimal]
  ) (unlift(SinglePropertyDisposals.unapply))
}