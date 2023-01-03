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

package v1.models.response.listEmployment

import api.hateoas.{HateoasLinks, HateoasListLinksFactory}
import api.models.hateoas.{HateoasData, Link}
import cats.Functor
import config.AppConfig
import play.api.libs.functional.syntax._
import play.api.libs.json._
import utils.JsonUtils

case class ListEmploymentResponse[E](employments: Option[Seq[E]], customEmployments: Option[Seq[E]])

object ListEmploymentResponse extends HateoasLinks with JsonUtils {

  implicit def writes[E: Writes]: OWrites[ListEmploymentResponse[E]] = Json.writes[ListEmploymentResponse[E]]

  implicit def reads[E: Reads]: Reads[ListEmploymentResponse[E]] = (
    (JsPath \ "employments").readNullable[Seq[E]].mapEmptySeqToNone and
      (JsPath \ "customerDeclaredEmployments").readNullable[Seq[E]].mapEmptySeqToNone
  )((employments, customerEmployments) => ListEmploymentResponse(employments, customerEmployments))

  implicit object ListEmploymentLinksFactory extends HateoasListLinksFactory[ListEmploymentResponse, Employment, ListEmploymentHateoasData] {

    override def itemLinks(appConfig: AppConfig, data: ListEmploymentHateoasData, employment: Employment): Seq[Link] =
      Seq(
        retrieveEmployment(appConfig, data.nino, data.taxYear, employment.employmentId)
      )

    override def links(appConfig: AppConfig, data: ListEmploymentHateoasData): Seq[Link] = {
      import data._
      Seq(
        addCustomEmployment(appConfig, nino, taxYear),
        listEmployment(appConfig, nino, taxYear, isSelf = true)
      )
    }

  }

  implicit object ResponseFunctor extends Functor[ListEmploymentResponse] {

    override def map[A, B](fa: ListEmploymentResponse[A])(f: A => B): ListEmploymentResponse[B] =
      ListEmploymentResponse(fa.employments.map(x => x.map(f)), fa.customEmployments.map(x => x.map(f)))

  }

}

case class ListEmploymentHateoasData(nino: String, taxYear: String) extends HateoasData
