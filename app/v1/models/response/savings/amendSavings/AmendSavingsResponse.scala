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

package v1.models.response.savings.amendSavings

import config.AppConfig
import v1.hateoas.{HateoasLinks, HateoasLinksFactory}
import v1.models.hateoas.{HateoasData, Link}

case class AmendSavingsResponse()

object AmendSavingsResponse extends HateoasLinks {

  implicit object AmendSavingsLinksFactory extends HateoasLinksFactory[AmendSavingsResponse, AmendSavingsHateoasData] {
    override def links(appConfig: AppConfig, data: AmendSavingsHateoasData): Seq[Link] = {
      import data._
      Seq(
        retrieveSavings(appConfig, nino, taxYear),
        deleteSavings(appConfig, nino, taxYear)
      )
    }
  }

}

case class AmendSavingsHateoasData(nino: String, taxYear: String) extends HateoasData

