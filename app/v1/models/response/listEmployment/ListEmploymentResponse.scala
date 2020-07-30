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

package v1.models.response.listEmployment

import cats.Functor
import config.AppConfig
import play.api.libs.functional.syntax._
import play.api.libs.json.{JsPath, Json, OWrites, Reads, Writes}
import utils.JsonUtils
import v1.hateoas.{HateoasLinks, HateoasListLinksFactory}
import v1.models.hateoas.{HateoasData, Link}

case class ListEmploymentResponse[Employment](employments: Option[Seq[Employment]],
                                  customEmployments: Option[Seq[Employment]])

object ListEmploymentResponse extends HateoasLinks with JsonUtils{

  implicit def writes[Employment: Writes]: OWrites[ListEmploymentResponse[Employment]] = Json.writes[ListEmploymentResponse[Employment]]

  implicit def reads[Employment: Reads]: Reads[ListEmploymentResponse[Employment]] = (
    (JsPath \ "employments").readNullable[Seq[Employment]].mapEmptySeqToNone and
      (JsPath \ "customerDeclaredEmployments").readNullable[Seq[Employment]].mapEmptySeqToNone
      )((employments, customerEmployments) => ListEmploymentResponse(employments, customerEmployments))

  implicit object ListEmploymentLinksFactory extends HateoasListLinksFactory[ListEmploymentResponse, Employment, ListEmploymentHateoasData] {
    override def itemLinks(appConfig: AppConfig, data: ListEmploymentHateoasData, employment: Employment): Seq[Link] =
      Seq(
        retrieveEmployment(appConfig, data.nino, data.taxYear, employment.employmentId, isSelf = true),
      )

    override def links(appConfig: AppConfig, data: ListEmploymentHateoasData): Seq[Link] = {
      import data._
      Seq(
        listEmployment(appConfig, nino, taxYear, isSelf = true),
        addCustomEmployment(appConfig, nino, taxYear),
      )
    }
  }

  implicit object ResponseFunctor extends Functor[ListEmploymentResponse] {
    override def map[A, B](fa: ListEmploymentResponse[A])(f: A => B): ListEmploymentResponse[B] =
      ListEmploymentResponse(fa.employments.map(x => x.map(f)), fa.customEmployments.map(x => x.map(f)))
  }
}

case class ListEmploymentHateoasData(nino: String, taxYear: String) extends HateoasData
