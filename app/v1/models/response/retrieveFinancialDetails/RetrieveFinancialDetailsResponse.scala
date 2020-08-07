/*
 * Copyright 2020 HM Revenue & Customs
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

import config.AppConfig
import play.api.libs.functional.syntax._
import play.api.libs.json.{JsPath, Json, OWrites, Reads}
import v1.hateoas.{HateoasLinks, HateoasLinksFactory}
import v1.models.domain.MtdSourceEnum
import v1.models.hateoas.{HateoasData, Link}

case class RetrieveFinancialDetailsResponse(submittedOn: String,
                                            source: Option[MtdSourceEnum],
                                            customerAdded: Option[String],
                                            dateIgnored: Option[String],
                                            employment: Employment)

object RetrieveFinancialDetailsResponse extends HateoasLinks {
  implicit val writes: OWrites[RetrieveFinancialDetailsResponse] = Json.writes[RetrieveFinancialDetailsResponse]

  implicit val reads: Reads[RetrieveFinancialDetailsResponse] = (
    (JsPath \ "submittedOn").read[String] and
      (JsPath \ "source").readNullable[DesSourceEnum].map(_.map(_.toMtdEnum)) and
      (JsPath \ "customerAdded").readNullable[String] and
      (JsPath \ "dateIgnored").readNullable[String] and
      (JsPath \ "employment").read[Employment]
  ) (RetrieveFinancialDetailsResponse.apply _)

  implicit object RetrieveFinancialDetailsLinksFactory extends HateoasLinksFactory[RetrieveFinancialDetailsResponse, RetrieveFinancialDetailsHateoasData] {
    override def links(appConfig: AppConfig, data: RetrieveFinancialDetailsHateoasData): Seq[Link] = {
      import data._
      Seq(
        retrieveFinancialDetails(appConfig, nino, taxYear, employmentId),
        amendFinancialDetails(appConfig, nino, taxYear, employmentId),
        deleteFinancialDetails(appConfig, nino, taxYear, employmentId)
      )
    }
  }
}

case class RetrieveFinancialDetailsHateoasData(nino: String, taxYear: String, employmentId: String) extends HateoasData
