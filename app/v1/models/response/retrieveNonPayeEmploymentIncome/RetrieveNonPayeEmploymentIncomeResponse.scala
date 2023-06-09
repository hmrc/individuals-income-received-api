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

package v1.models.response.retrieveNonPayeEmploymentIncome

import api.hateoas.{HateoasLinks, HateoasLinksFactory}
import api.models.domain.{MtdSourceEnum, Timestamp}
import api.models.hateoas.{HateoasData, Link}
import config.AppConfig
import play.api.libs.functional.syntax._
import play.api.libs.json.{JsPath, Json, OWrites, Reads}

case class RetrieveNonPayeEmploymentIncomeResponse(submittedOn: Timestamp,
                                                   source: MtdSourceEnum,
                                                   totalNonPayeIncome: Option[BigDecimal],
                                                   nonPayeIncome: Option[NonPayeIncome])

object RetrieveNonPayeEmploymentIncomeResponse extends HateoasLinks {

  implicit val reads: Reads[RetrieveNonPayeEmploymentIncomeResponse] = (
    (JsPath \ "submittedOn").read[Timestamp] and
      (JsPath \ "source").read[DownstreamSourceEnum].map(_.toMtdEnum) and
      (JsPath \ "totalNonPayeIncome").readNullable[BigDecimal] and
      (JsPath \ "nonPayeIncome").readNullable[NonPayeIncome]
  )(RetrieveNonPayeEmploymentIncomeResponse.apply _)

  implicit val writes: OWrites[RetrieveNonPayeEmploymentIncomeResponse] = Json.writes[RetrieveNonPayeEmploymentIncomeResponse]

  implicit object RetrieveOtherEmploymentLinksFactory
      extends HateoasLinksFactory[RetrieveNonPayeEmploymentIncomeResponse, RetrieveNonPayeEmploymentIncomeHateoasData] {

    override def links(appConfig: AppConfig, data: RetrieveNonPayeEmploymentIncomeHateoasData): Seq[Link] = {
      import data._
      Seq(
        createAmendNonPayeEmployment(appConfig, nino, taxYear),
        retrieveNonPayeEmployment(appConfig, nino, taxYear),
        deleteNonPayeEmployment(appConfig, nino, taxYear)
      )
    }

  }

}

case class RetrieveNonPayeEmploymentIncomeHateoasData(nino: String, taxYear: String) extends HateoasData
