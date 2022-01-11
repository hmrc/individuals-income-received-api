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

package v1r7.models.response.retrieveDividends

import play.api.libs.functional.syntax._
import play.api.libs.json.{JsPath, Json, OWrites, Reads}

case class CloseCompanyLoansWrittenOff(customerReference: Option[String], grossAmount: BigDecimal)

object CloseCompanyLoansWrittenOff {
  implicit val reads: Reads[CloseCompanyLoansWrittenOff] = (
    (JsPath \ "customerReference").readNullable[String] and
      (JsPath \ "grossAmount").read[BigDecimal]
    ) (CloseCompanyLoansWrittenOff.apply _)

  implicit val writes: OWrites[CloseCompanyLoansWrittenOff] = Json.writes[CloseCompanyLoansWrittenOff]

}