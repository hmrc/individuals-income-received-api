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

package v1.models.response.retrieveInsurancePolicies

import api.hateoas.{HateoasLinks, HateoasLinksFactory}
import api.models.domain.Timestamp
import api.models.hateoas.{HateoasData, Link}
import config.AppConfig
import play.api.libs.functional.syntax._
import play.api.libs.json.{JsPath, Json, OWrites, Reads}
import utils.JsonUtils

case class RetrieveInsurancePoliciesResponse(submittedOn: Timestamp,
                                             lifeInsurance: Option[Seq[CommonInsurancePoliciesItem]],
                                             capitalRedemption: Option[Seq[CommonInsurancePoliciesItem]],
                                             lifeAnnuity: Option[Seq[CommonInsurancePoliciesItem]],
                                             voidedIsa: Option[Seq[VoidedIsaPoliciesItem]],
                                             foreign: Option[Seq[ForeignPoliciesItem]])

object RetrieveInsurancePoliciesResponse extends HateoasLinks with JsonUtils {

  implicit val reads: Reads[RetrieveInsurancePoliciesResponse] = (
    (JsPath \ "submittedOn").read[Timestamp] and
      (JsPath \ "lifeInsurance").readNullable[Seq[CommonInsurancePoliciesItem]].mapEmptySeqToNone and
      (JsPath \ "capitalRedemption").readNullable[Seq[CommonInsurancePoliciesItem]].mapEmptySeqToNone and
      (JsPath \ "lifeAnnuity").readNullable[Seq[CommonInsurancePoliciesItem]].mapEmptySeqToNone and
      (JsPath \ "voidedIsa").readNullable[Seq[VoidedIsaPoliciesItem]].mapEmptySeqToNone and
      (JsPath \ "foreign").readNullable[Seq[ForeignPoliciesItem]].mapEmptySeqToNone
  )(RetrieveInsurancePoliciesResponse.apply _)

  implicit val writes: OWrites[RetrieveInsurancePoliciesResponse] = Json.writes[RetrieveInsurancePoliciesResponse]

  implicit object RetrieveInsurancePoliciesLinksFactory
      extends HateoasLinksFactory[RetrieveInsurancePoliciesResponse, RetrieveInsurancePoliciesHateoasData] {

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
