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

package v1.models.response.retrieveOtherCgt

import play.api.libs.json.{Json, OFormat}

case class Disposal(assetType: String,
                    assetDescription: String,
                    acquisitionDate: String,
                    disposalDate: String,
                    disposalProceeds: BigDecimal,
                    allowableCosts: BigDecimal,
                    gain: Option[BigDecimal],
                    loss: Option[BigDecimal],
                    claimOrElectionCodes: Option[Seq[String]],
                    gainAfterRelief: Option[BigDecimal],
                    lossAfterRelief: Option[BigDecimal],
                    rttTaxPaid: Option[BigDecimal])

object Disposal {
  implicit val format: OFormat[Disposal] = Json.format[Disposal]
}
