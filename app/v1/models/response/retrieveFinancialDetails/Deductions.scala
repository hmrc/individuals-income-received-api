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

package v1.models.response.retrieveFinancialDetails

import play.api.libs.json.{JsPath, Json, OWrites, Reads}

case class Deductions(studentLoans: Option[StudentLoans])

object Deductions {
  val empty: Deductions = Deductions(None)

  implicit val writes: OWrites[Deductions] = Json.writes[Deductions]

  implicit val reads: Reads[Deductions] = (JsPath \ "studentLoans")
    .readNullable[StudentLoans]
    .map {
      case Some(StudentLoans.empty) => None
      case other                    => other
    }
    .map(Deductions.apply)

}
