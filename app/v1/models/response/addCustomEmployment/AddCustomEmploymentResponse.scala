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

package v1.models.response.addCustomEmployment

import api.hateoas.{HateoasLinks, HateoasLinksFactory}
import api.models.hateoas.{HateoasData, Link}
import config.AppConfig
import play.api.libs.json.{Json, OFormat}

case class AddCustomEmploymentResponse(employmentId: String)

object AddCustomEmploymentResponse extends HateoasLinks {
  implicit val format: OFormat[AddCustomEmploymentResponse] = Json.format[AddCustomEmploymentResponse]

  implicit object AddCustomEmploymentLinksFactory extends HateoasLinksFactory[AddCustomEmploymentResponse, AddCustomEmploymentHateoasData] {

    override def links(appConfig: AppConfig, data: AddCustomEmploymentHateoasData): Seq[Link] = {
      import data._
      Seq(
        listEmployment(appConfig, nino, taxYear, isSelf = false),
        retrieveEmployment(appConfig, nino, taxYear, employmentId),
        amendCustomEmployment(appConfig, nino, taxYear, employmentId),
        deleteCustomEmployment(appConfig, nino, taxYear, employmentId)
      )
    }

  }

}

case class AddCustomEmploymentHateoasData(nino: String, taxYear: String, employmentId: String) extends HateoasData
