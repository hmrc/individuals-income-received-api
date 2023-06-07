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

package v2.models.response.createAmendDividends

import api.hateoas.{HateoasLinks, HateoasLinksFactory}
import api.models.hateoas.{HateoasData, Link}
import config.AppConfig
import play.api.libs.json.{Json, Writes}

object CreateAmendDividendsResponse extends HateoasLinks {

  implicit object AmendDividendsLinksFactory extends HateoasLinksFactory[Unit, CreateAmendDividendsHateoasData] {

    override def links(appConfig: AppConfig, data: CreateAmendDividendsHateoasData): Seq[Link] = {
      import data._
      Seq(
        amendDividends(appConfig, nino, taxYear),
        retrieveDividends(appConfig, nino, taxYear),
        deleteDividends(appConfig, nino, taxYear)
      )
    }

  }

}

case class CreateAmendDividendsHateoasData(nino: String, taxYear: String) extends HateoasData

object CreateAmendDividendsHateoasData {
  implicit val writes: Writes[CreateAmendDividendsHateoasData] = Json.writes[CreateAmendDividendsHateoasData]
}
