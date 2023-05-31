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

package v1andv2.models.response.createAmendCgtResidentialPropertyDisposals

import api.hateoas.{HateoasLinks, HateoasLinksFactory}
import api.models.hateoas.{HateoasData, Link}
import config.AppConfig
import play.api.libs.json.{Json, Writes}

object CreateAmendCgtResidentialPropertyDisposalsResponse extends HateoasLinks {

  implicit object CreateAmendCgtResidentialPropertyDisposalsLinksFactory
      extends HateoasLinksFactory[Unit, CreateAmendCgtResidentialPropertyDisposalsHateoasData] {

    override def links(appConfig: AppConfig, data: CreateAmendCgtResidentialPropertyDisposalsHateoasData): Seq[Link] = {
      import data._
      Seq(
        createAmendNonPpdCgt(appConfig, nino, taxYear),
        retrieveAllCgtPpdDisposalsOverrides(appConfig, nino, taxYear),
        deleteNonPpdCgt(appConfig, nino, taxYear)
      )
    }

  }

}

case class CreateAmendCgtResidentialPropertyDisposalsHateoasData(nino: String, taxYear: String) extends HateoasData

object CreateAmendCgtResidentialPropertyDisposalsHateoasData {

  implicit val writes: Writes[CreateAmendCgtResidentialPropertyDisposalsHateoasData] =
    Json.writes[CreateAmendCgtResidentialPropertyDisposalsHateoasData]

}
