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

package v1.fixtures

import play.api.libs.json.{JsObject, JsValue, Json}

object RetrieveInsurancePoliciesControllerFixture {

  val fullRetrieveInsurancePoliciesResponse: JsValue = Json.parse(
    """
      |{
      |   "submittedOn": "2020-07-06T09:37:17.000Z",
      |   "lifeInsurance": [
      |      {
      |         "customerReference": "INPOLY123A",
      |         "event": "Death of spouse",
      |         "gainAmount": 1.23,
      |         "taxPaid": true,
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
      |         "taxPaid": true,
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
      |         "taxPaid": true,
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
      |         "taxPaidAmount": 1.36,
      |         "yearsHeld": 5,
      |         "yearsHeldSinceLastGain": 4
      |      }
      |   ],
      |   "foreign": [
      |      {
      |         "customerReference": "INPOLY123E",
      |         "gainAmount": 1.27,
      |         "taxPaidAmount": 1.37,
      |         "yearsHeld": 6
      |      }
      |   ]
      |}
    """.stripMargin
  )

  def mtdResponseWithHateoas(nino: String, taxYear: String): JsObject = fullRetrieveInsurancePoliciesResponse.as[JsObject] ++ Json
    .parse(
      s"""
       |{
       |   "links":[
       |      {
       |         "href":"/individuals/income-received/insurance-policies/$nino/$taxYear",
       |         "method":"PUT",
       |         "rel":"create-and-amend-insurance-policies-income"
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
    )
    .as[JsObject]

}
