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

package v1.models.response.retrievePensions

import config.AppConfig
import play.api.libs.functional.syntax._
import play.api.libs.json.{JsPath, Json, OWrites, Reads}
import utils.JsonUtils
import v1.hateoas.{HateoasLinks, HateoasLinksFactory}
import v1.models.hateoas.{HateoasData, Link}

case class RetrievePensionsResponse(submittedOn: String,
                                    foreignPensions: Option[Seq[ForeignPensionsItem]],
                                    overseasPensionContributions: Option[Seq[OverseasPensionContributionsItem]])

object RetrievePensionsResponse extends HateoasLinks with JsonUtils {

  implicit val reads: Reads[RetrievePensionsResponse] = (
    (JsPath \ "submittedOn").read[String] and
      (JsPath \ "foreignPensions").readNullable[Seq[ForeignPensionsItem]].mapEmptySeqToNone and
      (JsPath \ "overseasPensionContributions").readNullable[Seq[OverseasPensionContributionsItem]].mapEmptySeqToNone
    ) (RetrievePensionsResponse.apply _)

  implicit val writes: OWrites[RetrievePensionsResponse] = Json.writes[RetrievePensionsResponse]

  implicit object RetrievePensionsLinksFactory extends HateoasLinksFactory[RetrievePensionsResponse, RetrievePensionsHateoasData] {
    override def links(appConfig: AppConfig, data: RetrievePensionsHateoasData): Seq[Link] = {
      import data._
      Seq(
        amendPensions(appConfig, nino, taxYear),
        retrievePensions(appConfig, nino, taxYear),
        deletePensions(appConfig, nino, taxYear)
      )
    }
  }

}

case class RetrievePensionsHateoasData(nino: String, taxYear: String) extends HateoasData