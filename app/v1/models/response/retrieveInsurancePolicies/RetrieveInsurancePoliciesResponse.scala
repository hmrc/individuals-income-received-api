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

package v1.models.response.retrieveInsurancePolicies

import config.AppConfig
import play.api.libs.functional.syntax._
import play.api.libs.json.{JsPath, Json, OWrites, Reads}
import v1.hateoas.{HateoasLinks, HateoasLinksFactory}
import v1.models.hateoas.{HateoasData, Link}
import v1.models.response.retrieveInsurancePolicies.ReadsWritesItems._

case class RetrieveInsurancePoliciesResponse(lifeInsurance: Option[Seq[InsurancePoliciesItem]],
                                             capitalRedemption: Option[Seq[InsurancePoliciesItem]],
                                             lifeAnnuity: Option[Seq[InsurancePoliciesItem]],
                                             voidedIsa: Option[Seq[InsurancePoliciesItem]],
                                             foreign: Option[Seq[InsurancePoliciesItem]])

object RetrieveInsurancePoliciesResponse extends HateoasLinks {

  implicit val reads: Reads[RetrieveInsurancePoliciesResponse] = (
    (JsPath \ "lifeInsurance").readNullable[Seq[InsurancePoliciesItem]] and
      (JsPath \ "capitalRedemption").readNullable[Seq[InsurancePoliciesItem]] and
      (JsPath \ "lifeAnnuity").readNullable[Seq[InsurancePoliciesItem]] and
      (JsPath \ "voidedIsa").readNullable[Seq[InsurancePoliciesItem]](voidedIsaReads) and
      (JsPath \ "foreign").readNullable[Seq[InsurancePoliciesItem]](foreignReads)
    ) (RetrieveInsurancePoliciesResponse.apply _)

  implicit val writes: OWrites[RetrieveInsurancePoliciesResponse] = Json.writes[RetrieveInsurancePoliciesResponse]

  implicit object RetrieveInsurancePoliciesLinksFactory extends HateoasLinksFactory[RetrieveInsurancePoliciesResponse, RetrieveInsurancePoliciesHateoasData] {
    override def links(appConfig: AppConfig, data: RetrieveInsurancePoliciesHateoasData): Seq[Link] = {
      import data._
      Seq(
        amendInsurancePolicies(appConfig, nino, taxYear),
        retrieveInsurancePolicies(appConfig, nino, taxYear),
        deleteInsurancePolicies(appConfig, nino, taxYear)
      )
    }
  }

}

case class RetrieveInsurancePoliciesHateoasData(nino: String, taxYear: String) extends HateoasData