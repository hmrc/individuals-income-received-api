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

package v1.models.request.insurancePolicies.amend

import play.api.libs.json.Json
import support.UnitSpec
import v1.fixtures.insurancePolicies.AmendInsurancePoliciesFixture._

class AmendRequestBodySpec extends UnitSpec {

  val fullAmendRequestBodyModel: AmendRequestBody = AmendRequestBody(
    lifeInsurance = Some(Seq(fullLifeInsuranceModel, fullLifeInsuranceModel)),
    capitalRedemption = Some(Seq(fullCapitalRedemptionModel, fullCapitalRedemptionModel)),
    lifeAnnuity = Some(Seq(fullLifeAnnuityModel, fullLifeAnnuityModel)),
    voidedIsa = Some(Seq(fullVoidedIsaModel, fullVoidedIsaModel)),
    foreign = Some(Seq(fullForeignModel, fullForeignModel))
  )

  val fullAmendRequestBodyJson = Json.parse(
    """
      |{
      |   "lifeInsurance":[
      |       {
      |           "customerReference": "INPOLY123A",
      |           "event": "Death of spouse",
      |           "gainAmount": 2000.99,
      |           "taxPaid": 5000.99,
      |           "yearsHeld": 15,
      |           "yearsHeldSinceLastGain": 12,
      |           "deficiencyRelief": 5000.99
      |       },
      |       {
      |           "customerReference": "INPOLY123A",
      |           "event": "Death of spouse",
      |           "gainAmount": 2000.99,
      |           "taxPaid": 5000.99,
      |           "yearsHeld": 15,
      |           "yearsHeldSinceLastGain": 12,
      |           "deficiencyRelief": 5000.99
      |       }
      |   ],
      |   "capitalRedemption":[
      |       {
      |           "customerReference": "INPOLY123A",
      |           "event": "Death of spouse",
      |           "gainAmount": 2000.99,
      |           "taxPaid": 5000.99,
      |           "yearsHeld": 15,
      |           "yearsHeldSinceLastGain": 12,
      |           "deficiencyRelief": 5000.99
      |       },
      |       {
      |           "customerReference": "INPOLY123A",
      |           "event": "Death of spouse",
      |           "gainAmount": 2000.99,
      |           "taxPaid": 5000.99,
      |           "yearsHeld": 15,
      |           "yearsHeldSinceLastGain": 12,
      |           "deficiencyRelief": 5000.99
      |       }
      |   ],
      |   "lifeAnnuity":[
      |       {
      |           "customerReference": "INPOLY123A",
      |           "event": "Death of spouse",
      |           "gainAmount": 2000.99,
      |           "taxPaid": 5000.99,
      |           "yearsHeld": 15,
      |           "yearsHeldSinceLastGain": 12,
      |           "deficiencyRelief": 5000.99
      |       },
      |       {
      |           "customerReference": "INPOLY123A",
      |           "event": "Death of spouse",
      |           "gainAmount": 2000.99,
      |           "taxPaid": 5000.99,
      |           "yearsHeld": 15,
      |           "yearsHeldSinceLastGain": 12,
      |           "deficiencyRelief": 5000.99
      |       }
      |   ],
      |   "voidedIsa":[
      |       {
      |           "customerReference": "INPOLY123A",
      |           "event": "Death of spouse",
      |           "gainAmount": 2000.99,
      |           "taxPaid": 5000.99,
      |           "yearsHeld": 15,
      |           "yearsHeldSinceLastGain": 12
      |       },
      |       {
      |           "customerReference": "INPOLY123A",
      |           "event": "Death of spouse",
      |           "gainAmount": 2000.99,
      |           "taxPaid": 5000.99,
      |           "yearsHeld": 15,
      |           "yearsHeldSinceLastGain": 12
      |       }
      |   ],
      |   "foreign":[
      |       {
      |           "customerReference": "INPOLY123A",
      |           "gainAmount": 2000.99,
      |           "taxPaid": 5000.99,
      |           "yearsHeld": 15
      |       },
      |       {
      |           "customerReference": "INPOLY123A",
      |           "gainAmount": 2000.99,
      |           "taxPaid": 5000.99,
      |           "yearsHeld": 15
      |       }
      |   ]
      |}
    """.stripMargin
  )

  val minAmendRequestBodyModel: AmendRequestBody = AmendRequestBody(
    lifeInsurance = Some(Seq(minLifeInsuranceModel)),
    capitalRedemption = Some(Seq(minCapitalRedemptionModel)),
    lifeAnnuity = Some(Seq(minLifeAnnuityModel)),
    voidedIsa = Some(Seq(minVoidedIsaModel)),
    foreign = Some(Seq(minForeignModel))
  )

  val minAmendRequestBodyJson = Json.parse(
    """
      |{
      |   "lifeInsurance":[
      |      {
      |          "gainAmount": 1
      |      }
      |   ],
      |   "capitalRedemption":[
      |      {
      |         "gainAmount":1
      |      }
      |   ],
      |   "lifeAnnuity":[
      |       {
      |           "gainAmount": 1
      |       }
      |   ],
      |   "voidedIsa":[
      |       {
      |           "gainAmount": 1
      |       }
      |   ],
      |   "foreign":[
      |       {
      |           "gainAmount": 1
      |       }
      |   ]
      |}
    """.stripMargin
  )


  "AmendRequestBody" should {
    "process Json correctly" when {
      "a full valid request is sent" in {
        fullAmendRequestBodyJson.as[AmendRequestBody] shouldBe fullAmendRequestBodyModel
      }

      "a minimal valid request is sent" in {
        minAmendRequestBodyJson.as[AmendRequestBody] shouldBe minAmendRequestBodyModel
      }
    }

    "write json correctly" when {
      "a full valid model is provided" in {
        Json.toJson(fullAmendRequestBodyModel) shouldBe fullAmendRequestBodyJson
      }

      "a  minimal valid model is provided" in {
        Json.toJson(minAmendRequestBodyModel) shouldBe minAmendRequestBodyJson
      }
    }
  }
}
