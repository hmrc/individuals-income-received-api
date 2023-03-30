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

package v1.models.response.createAmendOtherCgt

import api.hateoas.{HateoasLinks, HateoasLinksFactory}
import api.models.hateoas.{HateoasData, Link}
import config.AppConfig
import play.api.libs.json.{Json, Writes}

object CreateAmendOtherCgtResponse extends HateoasLinks {

  implicit object CreateAmendOtherCgtLinksFactory extends HateoasLinksFactory[Unit, CreateAmendOtherCgtHateoasData] {

    override def links(appConfig: AppConfig, data: CreateAmendOtherCgtHateoasData): Seq[Link] = {
      import data._
      Seq(
        createAmendOtherCgt(appConfig, nino, taxYear),
        retrieveOtherCgt(appConfig, nino, taxYear),
        deleteOtherCgt(appConfig, nino, taxYear)
      )
    }

  }

}

case class CreateAmendOtherCgtHateoasData(nino: String, taxYear: String) extends HateoasData

object CreateAmendOtherCgtHateoasData {
  implicit val writes: Writes[CreateAmendOtherCgtHateoasData] = Json.writes[CreateAmendOtherCgtHateoasData]

}
