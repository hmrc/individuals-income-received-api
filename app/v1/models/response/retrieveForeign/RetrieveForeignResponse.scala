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

package v1.models.response.retrieveForeign

import api.hateoas.{HateoasLinks, HateoasLinksFactory}
import api.models.domain.Timestamp
import api.models.hateoas.{HateoasData, Link}
import config.AppConfig
import play.api.libs.functional.syntax._
import play.api.libs.json.{JsPath, Json, OWrites, Reads}
import utils.JsonUtils

case class RetrieveForeignResponse(submittedOn: Timestamp,
                                   foreignEarnings: Option[ForeignEarnings],
                                   unremittableForeignIncome: Option[Seq[UnremittableForeignIncome]])

object RetrieveForeignResponse extends HateoasLinks with JsonUtils {

  implicit val reads: Reads[RetrieveForeignResponse] = (
    (JsPath \ "submittedOn").read[Timestamp] and
      (JsPath \ "foreignEarnings").readNullable[ForeignEarnings] and
      (JsPath \ "unremittableForeignIncome").readNullable[Seq[UnremittableForeignIncome]].mapEmptySeqToNone
  )(RetrieveForeignResponse.apply _)

  implicit val writes: OWrites[RetrieveForeignResponse] = Json.writes[RetrieveForeignResponse]

  implicit object RetrieveForeignLinksFactory extends HateoasLinksFactory[RetrieveForeignResponse, RetrieveForeignHateoasData] {

    override def links(appConfig: AppConfig, data: RetrieveForeignHateoasData): Seq[Link] = {
      import data._
      Seq(
        retrieveForeign(appConfig, nino, taxYear),
        amendForeign(appConfig, nino, taxYear),
        deleteForeign(appConfig, nino, taxYear)
      )
    }

  }

}

case class RetrieveForeignHateoasData(nino: String, taxYear: String) extends HateoasData
