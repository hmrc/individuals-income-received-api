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

package v1.models.response.addUkSavingsAccount

import api.hateoas.{HateoasLinks, HateoasLinksFactory}
import api.models.hateoas.{HateoasData, Link}
import config.AppConfig
import play.api.libs.json.{JsPath, Json, OWrites, Reads}

case class AddUkSavingsAccountResponse(savingsAccountId: String)

object AddUkSavingsAccountResponse extends HateoasLinks {

  implicit val reads: Reads[AddUkSavingsAccountResponse] = (JsPath \ "incomeSourceId").read[String].map(AddUkSavingsAccountResponse(_))

  implicit val writes: OWrites[AddUkSavingsAccountResponse] = Json.writes[AddUkSavingsAccountResponse]

  implicit object AddUkSavingsLinksFactory extends HateoasLinksFactory[AddUkSavingsAccountResponse, AddUkSavingsAccountHateoasData] {

    override def links(appConfig: AppConfig, data: AddUkSavingsAccountHateoasData): Seq[Link] = {
      import data._
      Seq(
        listUkSavings(appConfig, nino)
      )
    }

  }

}

case class AddUkSavingsAccountHateoasData(nino: String, savingsAccountId: String) extends HateoasData
