/*
 * Copyright 2022 HM Revenue & Customs
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

package v1.models.response.listUkSavingsAccount

import api.hateoas.{HateoasLinks, HateoasListLinksFactory}
import api.models.hateoas.{HateoasData, Link}
import cats.Functor
import config.AppConfig
import play.api.libs.json._
import utils.JsonUtils

case class ListUkSavingsAccountResponse[E](savingsAccounts: Option[Seq[E]])

object ListUkSavingsAccountResponse extends HateoasLinks with JsonUtils {

  implicit def writes[E: Writes]: OWrites[ListUkSavingsAccountResponse[E]] = Json.writes[ListUkSavingsAccountResponse[E]]

  implicit def reads[E: Reads]: Reads[ListUkSavingsAccountResponse[E]] =
    JsPath
      .readNullable[Seq[E]]
      .mapEmptySeqToNone
      .map(ListUkSavingsAccountResponse(_))

  implicit object ListEmploymentLinksFactory extends HateoasListLinksFactory[ListUkSavingsAccountResponse, UkSavingsAccount, ListUkSavingsAccountHateoasData] {

    override def itemLinks(appConfig: AppConfig, data: ListUkSavingsAccountHateoasData, employment: UkSavingsAccount): Seq[Link] =
      Seq()

    override def links(appConfig: AppConfig, data: ListUkSavingsAccountHateoasData): Seq[Link] = {
      import data._
      Seq(
        addUkSavings(appConfig, nino),
        listUkSavings(appConfig, nino, isSelf = true)
      )
    }

  }

  implicit object ResponseFunctor extends Functor[ListUkSavingsAccountResponse] {

    override def map[A, B](fa: ListUkSavingsAccountResponse[A])(f: A => B): ListUkSavingsAccountResponse[B] =
      ListUkSavingsAccountResponse(fa.savingsAccounts.map(x => x.map(f)))

  }

}

case class ListUkSavingsAccountHateoasData(nino: String) extends HateoasData
