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

package v1.models.request.amendInsurancePolicies

import play.api.libs.functional.syntax._
import play.api.libs.json.{JsPath, OWrites, Reads}
import utils.JsonUtils

case class AmendInsurancePoliciesRequestBody(lifeInsurance: Option[Seq[AmendCommonInsurancePoliciesItem]],
                                             capitalRedemption: Option[Seq[AmendCommonInsurancePoliciesItem]],
                                             lifeAnnuity: Option[Seq[AmendCommonInsurancePoliciesItem]],
                                             voidedIsa: Option[Seq[AmendVoidedIsaPoliciesItem]],
                                             foreign: Option[Seq[AmendForeignPoliciesItem]])

object AmendInsurancePoliciesRequestBody extends JsonUtils {
  val empty: AmendInsurancePoliciesRequestBody = AmendInsurancePoliciesRequestBody(None, None, None, None, None)

  implicit val reads: Reads[AmendInsurancePoliciesRequestBody] = (
    (JsPath \ "lifeInsurance").readNullable[Seq[AmendCommonInsurancePoliciesItem]].mapEmptySeqToNone and
      (JsPath \ "capitalRedemption").readNullable[Seq[AmendCommonInsurancePoliciesItem]].mapEmptySeqToNone and
      (JsPath \ "lifeAnnuity").readNullable[Seq[AmendCommonInsurancePoliciesItem]].mapEmptySeqToNone and
      (JsPath \ "voidedIsa").readNullable[Seq[AmendVoidedIsaPoliciesItem]].mapEmptySeqToNone and
      (JsPath \ "foreign").readNullable[Seq[AmendForeignPoliciesItem]].mapEmptySeqToNone
  )(AmendInsurancePoliciesRequestBody.apply _)

  implicit val writes: OWrites[AmendInsurancePoliciesRequestBody] = (
    (JsPath \ "lifeInsurance").writeNullable[Seq[AmendCommonInsurancePoliciesItem]] and
      (JsPath \ "capitalRedemption").writeNullable[Seq[AmendCommonInsurancePoliciesItem]] and
      (JsPath \ "lifeAnnuity").writeNullable[Seq[AmendCommonInsurancePoliciesItem]] and
      (JsPath \ "voidedIsa").writeNullable[Seq[AmendVoidedIsaPoliciesItem]] and
      (JsPath \ "foreign").writeNullable[Seq[AmendForeignPoliciesItem]]
  )(unlift(AmendInsurancePoliciesRequestBody.unapply))

}
