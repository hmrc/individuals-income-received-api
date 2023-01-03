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

package v1.models.request.createAmendPensions

import play.api.libs.functional.syntax._
import play.api.libs.json.{JsPath, Json, OWrites, Reads}

case class CreateAmendOverseasPensionContributions(customerReference: Option[String],
                                                   exemptEmployersPensionContribs: BigDecimal,
                                                   migrantMemReliefQopsRefNo: Option[String],
                                                   dblTaxationRelief: Option[BigDecimal],
                                                   dblTaxationCountryCode: Option[String],
                                                   dblTaxationArticle: Option[String],
                                                   dblTaxationTreaty: Option[String],
                                                   sf74reference: Option[String])

object CreateAmendOverseasPensionContributions {
  implicit val reads: Reads[CreateAmendOverseasPensionContributions] = Json.reads[CreateAmendOverseasPensionContributions]

  implicit val writes: OWrites[CreateAmendOverseasPensionContributions] = (
    (JsPath \ "customerReference").writeNullable[String] and
      (JsPath \ "exemptEmployersPensionContribs").write[BigDecimal] and
      (JsPath \ "migrantMemReliefQopsRefNo").writeNullable[String] and
      (JsPath \ "dblTaxationRelief").writeNullable[BigDecimal] and
      (JsPath \ "dblTaxationCountry").writeNullable[String] and
      (JsPath \ "dblTaxationArticle").writeNullable[String] and
      (JsPath \ "dblTaxationTreaty").writeNullable[String] and
      (JsPath \ "sf74Reference").writeNullable[String]
  )(unlift(CreateAmendOverseasPensionContributions.unapply))

}
