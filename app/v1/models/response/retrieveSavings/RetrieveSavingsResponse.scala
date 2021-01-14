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

package v1.models.response.retrieveSavings

import config.AppConfig
import play.api.libs.functional.syntax._
import play.api.libs.json.{JsPath, Json, OWrites, Reads}
import utils.JsonUtils
import v1.hateoas.{HateoasLinks, HateoasLinksFactory}
import v1.models.hateoas.{HateoasData, Link}

case class RetrieveSavingsResponse(submittedOn: String,
                                   securities: Option[Securities],
                                   foreignInterest: Option[Seq[ForeignInterestItem]])

object RetrieveSavingsResponse extends HateoasLinks with JsonUtils {

  implicit val reads: Reads[RetrieveSavingsResponse] = (
    (JsPath \ "submittedOn").read[String] and
    (JsPath \ "securities").readNullable[Securities] and
      (JsPath \ "foreignInterest").readNullable[Seq[ForeignInterestItem]].mapEmptySeqToNone
    ) (RetrieveSavingsResponse.apply _)

  implicit val writes: OWrites[RetrieveSavingsResponse] = Json.writes[RetrieveSavingsResponse]

  implicit object RetrieveSavingsLinksFactory extends HateoasLinksFactory[RetrieveSavingsResponse, RetrieveSavingsHateoasData] {
    override def links(appConfig: AppConfig, data: RetrieveSavingsHateoasData): Seq[Link] = {
      import data._
      Seq(
        amendSavings(appConfig, nino, taxYear),
        retrieveSavings(appConfig, nino, taxYear),
        deleteSavings(appConfig, nino, taxYear)
      )
    }
  }

}

case class RetrieveSavingsHateoasData(nino: String, taxYear: String) extends HateoasData