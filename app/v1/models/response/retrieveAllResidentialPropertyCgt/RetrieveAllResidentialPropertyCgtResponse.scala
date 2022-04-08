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

package v1.models.response.retrieveAllResidentialPropertyCgt

import api.hateoas.{HateoasLinks, HateoasLinksFactory}
import api.models.hateoas.{HateoasData, Link}
import config.AppConfig
import play.api.libs.json.{Json, OFormat}

case class RetrieveAllResidentialPropertyCgtResponse(ppdService: Option[PpdService], customerAddedDisposals: Option[CustomerAddedDisposals])

object RetrieveAllResidentialPropertyCgtResponse extends HateoasLinks {

  implicit val format: OFormat[RetrieveAllResidentialPropertyCgtResponse] = Json.format[RetrieveAllResidentialPropertyCgtResponse]

  implicit object RetrieveSavingsLinksFactory
      extends HateoasLinksFactory[RetrieveAllResidentialPropertyCgtResponse, RetrieveAllResidentialPropertyCgtHateoasData] {

    override def links(appConfig: AppConfig, data: RetrieveAllResidentialPropertyCgtHateoasData): Seq[Link] = {
      import data._
      Seq(
        createAmendCgtPpdOverrides(appConfig, nino, taxYear),
        deleteCgtPpdOverrides(appConfig, nino, taxYear),
        createAmendNonPpdCgt(appConfig, nino, taxYear),
        deleteNonPpdCgt(appConfig, nino, taxYear),
        retrieveAllCgtPpdDisposalsOverrides(appConfig, nino, taxYear)
      )
    }

  }

}

case class RetrieveAllResidentialPropertyCgtHateoasData(nino: String, taxYear: String) extends HateoasData
