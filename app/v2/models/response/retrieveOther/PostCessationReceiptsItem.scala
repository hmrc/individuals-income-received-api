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

package v2.models.response.retrieveOther

import play.api.libs.functional.syntax._
import play.api.libs.json.{JsPath, Json, OWrites, Reads}

case class PostCessationReceiptsItem(customerReference: Option[String],
                                     businessName: Option[String],
                                     dateBusinessCeased: Option[String],
                                     businessDescription: Option[String],
                                     incomeSource: Option[String],
                                     amount: BigDecimal,
                                     taxYearIncomeToBeTaxed: String)

object PostCessationReceiptsItem {

  implicit val reads: Reads[PostCessationReceiptsItem] = (
    (JsPath \ "customerReference").readNullable[String] and
      (JsPath \ "businessName").readNullable[String] and
      (JsPath \ "dateBusinessCeased").readNullable[String] and
      (JsPath \ "businessDescription").readNullable[String] and
      (JsPath \ "incomeSource").readNullable[String] and
      (JsPath \ "amount").read[BigDecimal] and
      (JsPath \ "taxYearIncomeToBeTaxed").read[String]
  )(PostCessationReceiptsItem.apply _)

  implicit val writes: OWrites[PostCessationReceiptsItem] = Json.writes[PostCessationReceiptsItem]
}
