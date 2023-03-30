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

package v1.models.response.createAmendNonPayeEmployment

import api.hateoas.{HateoasLinks, HateoasLinksFactory}
import api.models.hateoas.{HateoasData, Link}
import config.AppConfig

object CreateAmendNonPayeEmploymentResponse extends HateoasLinks {

  implicit object CreateAmendNonPayeEmploymentLinksFactory extends HateoasLinksFactory[Unit, CreateAmendNonPayeEmploymentHateoasData] {

    override def links(appConfig: AppConfig, data: CreateAmendNonPayeEmploymentHateoasData): Seq[Link] = {
      import data._
      Seq(
        createAmendNonPayeEmployment(appConfig, nino, taxYear),
        retrieveNonPayeEmployment(appConfig, nino, taxYear),
        deleteNonPayeEmployment(appConfig, nino, taxYear)
      )
    }

  }

}

case class CreateAmendNonPayeEmploymentHateoasData(nino: String, taxYear: String) extends HateoasData
