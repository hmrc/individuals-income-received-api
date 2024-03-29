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

package v1.models.response.createAmendCgtPpdOverrides

import api.hateoas.{HateoasLinks, HateoasLinksFactory}
import api.models.hateoas.{HateoasData, Link}
import config.AppConfig
import play.api.libs.json.{Json, Writes}

object CreateAmendCgtPpdOverridesResponse extends HateoasLinks {

  implicit object CreateAmendCgtPpdOverridesLinksFactory extends HateoasLinksFactory[Unit, CreateAmendCgtPpdOverridesHateoasData] {

    override def links(appConfig: AppConfig, data: CreateAmendCgtPpdOverridesHateoasData): Seq[Link] = {
      import data._
      Seq(
        createAmendCgtPpdOverrides(appConfig, nino, taxYear),
        deleteCgtPpdOverrides(appConfig, nino, taxYear),
        retrieveAllCgtPpdDisposalsOverrides(appConfig, nino, taxYear)
      )
    }

  }

}

case class CreateAmendCgtPpdOverridesHateoasData(nino: String, taxYear: String) extends HateoasData

object CreateAmendCgtPpdOverridesHateoasData {
  implicit val writes: Writes[CreateAmendCgtPpdOverridesHateoasData] = Json.writes[CreateAmendCgtPpdOverridesHateoasData]

}
