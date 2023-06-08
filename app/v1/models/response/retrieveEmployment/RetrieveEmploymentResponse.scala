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

package v1.models.response.retrieveEmployment

import api.hateoas.{HateoasLinks, HateoasLinksFactory}
import api.models.domain.Timestamp
import api.models.hateoas.{HateoasData, Link}
import config.AppConfig
import play.api.libs.functional.syntax._
import play.api.libs.json.{JsPath, Json, OWrites, Reads}

case class RetrieveEmploymentResponse(employerRef: Option[String],
                                      employerName: String,
                                      startDate: Option[String],
                                      cessationDate: Option[String],
                                      payrollId: Option[String],
                                      occupationalPension: Option[Boolean],
                                      dateIgnored: Option[Timestamp],
                                      submittedOn: Option[Timestamp])

object RetrieveEmploymentResponse extends HateoasLinks {

  implicit val writes: OWrites[RetrieveEmploymentResponse] = Json.writes[RetrieveEmploymentResponse]

  implicit val reads: Reads[RetrieveEmploymentResponse] = (
    (JsPath \\ "employerRef").readNullable[String] and
      (JsPath \\ "employerName").read[String] and
      (JsPath \\ "startDate").readNullable[String] and
      (JsPath \\ "cessationDate").readNullable[String] and
      (JsPath \\ "payrollId").readNullable[String] and
      (JsPath \\ "occupationalPension").readNullable[Boolean] and
      (JsPath \\ "dateIgnored").readNullable[Timestamp] and
      (JsPath \\ "submittedOn").readNullable[Timestamp]
  )(RetrieveEmploymentResponse.apply _)

  implicit object RetrieveCustomEmploymentLinksFactory extends HateoasLinksFactory[RetrieveEmploymentResponse, RetrieveEmploymentHateoasData] {

    override def links(appConfig: AppConfig, data: RetrieveEmploymentHateoasData): Seq[Link] = {
      import data._

      val baseLinks = Seq(
        listEmployment(appConfig, nino, taxYear, isSelf = false),
        retrieveEmployment(appConfig, nino, taxYear, employmentId)
      )

      val customLinks = Seq(
        amendCustomEmployment(appConfig, nino, taxYear, employmentId),
        deleteCustomEmployment(appConfig, nino, taxYear, employmentId)
      )

      val hmrcLinks = data.response.dateIgnored match {
        case Some(_) => baseLinks ++ Seq(unignoreEmployment(appConfig, nino, taxYear, employmentId))
        case None    => baseLinks ++ Seq(ignoreEmployment(appConfig, nino, taxYear, employmentId))
      }

      data.response.submittedOn match {
        case Some(_) => baseLinks ++ customLinks
        case None    => hmrcLinks
      }
    }

  }

}

case class RetrieveEmploymentHateoasData(nino: String, taxYear: String, employmentId: String, response: RetrieveEmploymentResponse)
    extends HateoasData
