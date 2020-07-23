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

package v1.models.response.retrieveCustomEmployment

import config.AppConfig
import play.api.libs.functional.syntax._
import play.api.libs.json.{JsPath, Json, OWrites, Reads}
import v1.hateoas.{HateoasLinks, HateoasLinksFactory}
import v1.models.hateoas.{HateoasData, Link}

case class RetrieveCustomEmploymentResponse(employerRef: Option[String],
                                            employerName: String,
                                            startDate: String,
                                            cessationDate: Option[String],
                                            payrollId: Option[String],
                                            dateIgnored: Option[String],
                                            submittedOn: Option[String])

object RetrieveCustomEmploymentResponse extends HateoasLinks {

  implicit val writes: OWrites[RetrieveCustomEmploymentResponse] = Json.writes[RetrieveCustomEmploymentResponse]

  implicit val reads: Reads[RetrieveCustomEmploymentResponse] = (
      (JsPath \\ "employerRef").readNullable[String] and
        (JsPath \\ "employerName").read[String] and
        (JsPath \\ "startDate").read[String] and
        (JsPath \\ "cessationDate").readNullable[String] and
        (JsPath \\ "payrollId").readNullable[String] and
        (JsPath \\ "dateIgnored").readNullable[String] and
        (JsPath \\ "submittedOn").readNullable[String]
      )(RetrieveCustomEmploymentResponse.apply _)

  implicit object RetrieveCustomEmploymentLinksFactory extends HateoasLinksFactory[RetrieveCustomEmploymentResponse, RetrieveCustomEmploymentHateoasData] {
    override def links(appConfig: AppConfig, data: RetrieveCustomEmploymentHateoasData): Seq[Link] = {
      import data._
      Seq(
        listEmployment(appConfig, nino, taxYear, isSelf = false),
        retrieveEmployment(appConfig, nino, taxYear, employmentId, isSelf = true),
        amendCustomEmployment(appConfig, nino, taxYear, employmentId),
        deleteCustomEmployment(appConfig, nino, taxYear, employmentId)
      )
    }
  }
}

case class RetrieveCustomEmploymentHateoasData(nino: String, taxYear: String, employmentId: String) extends HateoasData
