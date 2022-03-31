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

package v1r7.models.request.createAmendCgtResidentialPropertyDisposals

import play.api.libs.json._
import play.api.libs.functional.syntax._

case class Disposal(customerReference: Option[String],
                    disposalDate: String,
                    completionDate: String,
                    disposalProceeds: BigDecimal,
                    acquisitionDate: String,
                    acquisitionAmount: BigDecimal,
                    improvementCosts: Option[BigDecimal],
                    additionalCosts: Option[BigDecimal],
                    prfAmount: Option[BigDecimal],
                    otherReliefAmount: Option[BigDecimal],
                    lossesFromThisYear: Option[BigDecimal],
                    lossesFromPreviousYear: Option[BigDecimal],
                    amountOfNetGain: Option[BigDecimal],
                    amountOfNetLoss: Option[BigDecimal]) {
  def gainAndLossAreBothSupplied: Boolean = amountOfNetLoss.isDefined && amountOfNetGain.isDefined
}

object Disposal {
  implicit val reads: Reads[Disposal] = Json.reads[Disposal]

  implicit val writes: OWrites[Disposal] = (
    (__ \ "customerRef").writeNullable[String] and
      (__ \ "disposalDate").write[String] and
      (__ \ "completionDate").write[String] and
      (__ \ "disposalProceeds").write[BigDecimal] and
      (__ \ "acquisitionDate").write[String] and
      (__ \ "acquisitionAmount").write[BigDecimal] and
      (__ \ "improvementCosts").writeNullable[BigDecimal] and
      (__ \ "additionalCosts").writeNullable[BigDecimal] and
      (__ \ "prfAmount").writeNullable[BigDecimal] and
      (__ \ "otherReliefAmount").writeNullable[BigDecimal] and
      (__ \ "lossesFromThisYear").writeNullable[BigDecimal] and
      (__ \ "lossesFromPreviousYear").writeNullable[BigDecimal] and
      (__ \ "amountOfNetGain").writeNullable[BigDecimal] and
      (__ \ "amountOfLoss").writeNullable[BigDecimal]
  )(unlift(Disposal.unapply))

}
