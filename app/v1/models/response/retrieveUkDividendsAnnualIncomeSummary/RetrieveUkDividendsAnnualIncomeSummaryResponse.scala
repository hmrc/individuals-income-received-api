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

package v1.models.response.retrieveUkDividendsAnnualIncomeSummary

import api.hateoas.{HateoasLinks, HateoasLinksFactory}
import api.models.hateoas.{HateoasData, Link}
import config.AppConfig
import play.api.libs.json.{JsPath, Json, OWrites, Reads}

case class RetrieveUkDividendsAnnualIncomeSummaryResponse(ukDividends: Option[BigDecimal], otherUkDividends: Option[BigDecimal])

object RetrieveUkDividendsAnnualIncomeSummaryResponse extends HateoasLinks {

  implicit val writes: OWrites[RetrieveUkDividendsAnnualIncomeSummaryResponse] = Json.writes

  implicit val reads: Reads[RetrieveUkDividendsAnnualIncomeSummaryResponse] = {
     val defaultReads: Reads[RetrieveUkDividendsAnnualIncomeSummaryResponse] = Json.reads

    // On IFS the required object is one layer down:
    val ifsReads = (JsPath \ "ukDividendsAnnual").read(defaultReads)

    ifsReads orElse defaultReads
  }

  implicit object LinksFactory
      extends HateoasLinksFactory[RetrieveUkDividendsAnnualIncomeSummaryResponse, RetrieveUkDividendsAnnualIncomeSummaryHateoasData] {

    override def links(appConfig: AppConfig, data: RetrieveUkDividendsAnnualIncomeSummaryHateoasData): Seq[Link] = {
      import data._
      Seq(
        createAmendUkDividends(appConfig, nino, taxYear),
        retrieveUkDividends(appConfig, nino, taxYear),
        deleteUkDividends(appConfig, nino, taxYear)
      )
    }

  }

}

case class RetrieveUkDividendsAnnualIncomeSummaryHateoasData(nino: String, taxYear: String) extends HateoasData
