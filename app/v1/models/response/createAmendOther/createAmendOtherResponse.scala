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

package v1.models.response.createAmendOther

import api.hateoas.{HateoasLinks, HateoasLinksFactory}
import api.models.hateoas.{HateoasData, Link}
import config.AppConfig

object CreateAmendOtherResponse extends HateoasLinks {

  implicit object CreateAmendOtherLinksFactory extends HateoasLinksFactory[Unit, CreateAmendOtherHateoasData] {

    override def links(appConfig: AppConfig, data: CreateAmendOtherHateoasData): Seq[Link] = {
      import data._
      Seq(
        amendOther(appConfig, nino, taxYear),
        retrieveOther(appConfig, nino, taxYear),
        deleteOther(appConfig, nino, taxYear)
      )
    }

  }

}

case class CreateAmendOtherHateoasData(nino: String, taxYear: String) extends HateoasData
