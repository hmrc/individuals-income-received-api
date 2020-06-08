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

package v1.fixtures.insurancePolicies

import play.api.libs.json.{JsObject, JsValue, Json}
import v1.models.response.retrieveInsurancePolicies.{InsurancePoliciesItem, RetrieveInsurancePoliciesResponse}

object RetrieveInsurancePoliciesFixture {

   val fullRetrieveInsurancePoliciesResponse: JsValue = Json.parse(
    """
      |{
      |   "lifeInsurance": [
      |      {
      |         "customerReference": "INPOLY123A",
      |         "event": "Death of spouse",
      |         "gainAmount": 1.23,
      |         "taxPaid": 1.33,
      |         "yearsHeld": 2,
      |         "yearsHeldSinceLastGain": 1,
      |         "deficiencyRelief": 1.23
      |      }
      |   ],
      |   "capitalRedemption": [
      |      {
      |         "customerReference": "INPOLY123B",
      |         "event": "Death of spouse",
      |         "gainAmount": 1.24,
      |         "taxPaid": 1.34,
      |         "yearsHeld": 3,
      |         "yearsHeldSinceLastGain": 2,
      |         "deficiencyRelief": 1.23
      |      }
      |   ],
      |   "lifeAnnuity": [
      |      {
      |         "customerReference": "INPOLY123C",
      |         "event": "Death of spouse",
      |         "gainAmount": 1.25,
      |         "taxPaid": 1.35,
      |         "yearsHeld": 4,
      |         "yearsHeldSinceLastGain": 3,
      |         "deficiencyRelief": 1.23
      |      }
      |   ],
      |   "voidedIsa": [
      |      {
      |         "customerReference": "INPOLY123D",
      |         "event": "Death of spouse",
      |         "gainAmount": 1.26,
      |         "taxPaid": 1.36,
      |         "yearsHeld": 5,
      |         "yearsHeldSinceLastGain": 4
      |      }
      |   ],
      |   "foreign": [
      |      {
      |         "customerReference": "INPOLY123E",
      |         "gainAmount": 1.27,
      |         "taxPaid": 1.37,
      |         "yearsHeld": 6
      |      }
      |   ]
      |}
    """.stripMargin
  )

  def mtdResponseWithHateoas(nino: String, taxYear: String): JsObject = fullRetrieveInsurancePoliciesResponse.as[JsObject] ++ Json.parse(
    s"""
       |{
       |   "links":[
       |      {
       |         "href":"/individuals/income-received/insurance-policies/$nino/$taxYear",
       |         "method":"PUT",
       |         "rel":"amend-insurance-policies-income"
       |      },
       |      {
       |         "href":"/individuals/income-received/insurance-policies/$nino/$taxYear",
       |         "method":"GET",
       |         "rel":"self"
       |      },
       |      {
       |         "href":"/individuals/income-received/insurance-policies/$nino/$taxYear",
       |         "method":"DELETE",
       |         "rel":"delete-insurance-policies-income"
       |      }
       |   ]
       |}
    """.stripMargin
  ).as[JsObject]

  val itemModel1: InsurancePoliciesItem = InsurancePoliciesItem(
    customerReference = "INPOLY123A",
    event = Some("Death of spouse"),
    gainAmount = Some(1.23),
    taxPaid = Some(1.33),
    yearsHeld = Some(2),
    yearsHeldSinceLastGain = Some(1),
    deficiencyRelief = Some(1.23)
  )

  val itemModel2: InsurancePoliciesItem = InsurancePoliciesItem(
    customerReference = "INPOLY123B",
    event = Some("Death of spouse"),
    gainAmount = Some(1.24),
    taxPaid = Some(1.34),
    yearsHeld = Some(3),
    yearsHeldSinceLastGain = Some(2),
    deficiencyRelief = Some(1.23)
  )

  val itemModel3: InsurancePoliciesItem = InsurancePoliciesItem(
    customerReference = "INPOLY123C",
    event = Some("Death of spouse"),
    gainAmount = Some(1.25),
    taxPaid = Some(1.35),
    yearsHeld = Some(4),
    yearsHeldSinceLastGain = Some(3),
    deficiencyRelief = Some(1.23)
  )

  val itemModel4: InsurancePoliciesItem = InsurancePoliciesItem(
    customerReference = "INPOLY123D",
    event = Some("Death of spouse"),
    gainAmount = Some(1.26),
    taxPaid = Some(1.36),
    yearsHeld = Some(5),
    yearsHeldSinceLastGain = Some(4),
    deficiencyRelief = None
  )

  val itemModel5: InsurancePoliciesItem = InsurancePoliciesItem(
    customerReference = "INPOLY123E",
    event = None,
    gainAmount = Some(1.27),
    taxPaid = Some(1.37),
    yearsHeld = Some(6),
    yearsHeldSinceLastGain = None,
    deficiencyRelief = None
  )

  val retrieveInsurancePoliciesResponseModel: RetrieveInsurancePoliciesResponse = RetrieveInsurancePoliciesResponse(
    lifeInsurance = Some(Seq(itemModel1)),
    capitalRedemption = Some(Seq(itemModel2)),
    lifeAnnuity = Some(Seq(itemModel3)),
    voidedIsa = Some(Seq(itemModel4)),
    foreign = Some(Seq(itemModel5))
  )
}
