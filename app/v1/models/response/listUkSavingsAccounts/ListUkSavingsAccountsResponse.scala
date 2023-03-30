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

package v1.models.response.listUkSavingsAccounts

import api.hateoas.{HateoasLinks, HateoasLinksFactory}
import api.models.hateoas.{HateoasData, Link}
import config.AppConfig
import play.api.libs.json._
import utils.JsonUtils

case class ListUkSavingsAccountsResponse[E](savingsAccounts: Option[Seq[E]])

object ListUkSavingsAccountsResponse extends HateoasLinks with JsonUtils {

  implicit def writes[E: Writes]: OWrites[ListUkSavingsAccountsResponse[E]] = Json.writes[ListUkSavingsAccountsResponse[E]]

  implicit def reads[E: Reads]: Reads[ListUkSavingsAccountsResponse[E]] =
    JsPath
      .readNullable[Seq[E]]
      .mapEmptySeqToNone
      .map(ListUkSavingsAccountsResponse(_))

  implicit object ListUkSavingsAccountsLinksFactory
      extends HateoasLinksFactory[ListUkSavingsAccountsResponse[UkSavingsAccount], ListUkSavingsAccountsHateoasData] {

    override def links(appConfig: AppConfig, data: ListUkSavingsAccountsHateoasData): Seq[Link] = {
      import data._
      Seq(
        addUkSavings(appConfig, nino),
        listUkSavings(appConfig, nino, isSelf = true)
      )
    }

  }

}

case class ListUkSavingsAccountsHateoasData(nino: String) extends HateoasData
