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

import play.api.libs.json.{JsError, JsObject, Json}
import support.UnitSpec

class AmendInsurancePoliciesRequestBodySpec extends UnitSpec {

  private val json = Json.parse(
    """
      |{
      |   "lifeInsurance":[
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
      |   "capitalRedemption":[
      |      {
      |         "customerReference": "INPOLY123B",
      |         "gainAmount": 1.24,
      |         "taxPaid": true,
      |         "yearsHeld": 3,
      |         "yearsHeldSinceLastGain": 2,
      |         "deficiencyRelief": 1.23
      |      }
      |   ],
      |   "lifeAnnuity":[
      |      {
      |         "customerReference": "INPOLY123C",
      |         "gainAmount": 1.25,
      |         "taxPaid": true,
      |         "yearsHeld": 4,
      |         "yearsHeldSinceLastGain": 3,
      |         "deficiencyRelief": 1.23
      |      }
      |   ],
      |   "voidedIsa":[
      |      {
      |         "customerReference": "INPOLY123D",
      |         "gainAmount": 1.26,
      |         "taxPaidAmount": 1.36,
      |         "yearsHeld": 5,
      |         "yearsHeldSinceLastGain": 4
      |      }
      |   ],
      |   "foreign":[
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

  private val lifeInsuranceItemModel = AmendCommonInsurancePoliciesItem(
    customerReference = Some("INPOLY123A"),
    event = Some("Death of spouse"),
    gainAmount = 1.23,
    taxPaid = true,
    yearsHeld = Some(2),
    yearsHeldSinceLastGain = Some(1),
    deficiencyRelief = Some(1.23)
  )

  private val capitalRedemptionItemModel = AmendCommonInsurancePoliciesItem(
    customerReference = Some("INPOLY123B"),
    event = None,
    gainAmount = 1.24,
    taxPaid = true,
    yearsHeld = Some(3),
    yearsHeldSinceLastGain = Some(2),
    deficiencyRelief = Some(1.23)
  )

  private val lifeAnnuityItemModel = AmendCommonInsurancePoliciesItem(
    customerReference = Some("INPOLY123C"),
    event = None,
    gainAmount = 1.25,
    taxPaid = true,
    yearsHeld = Some(4),
    yearsHeldSinceLastGain = Some(3),
    deficiencyRelief = Some(1.23)
  )

  private val voidedIsaItemModel = AmendVoidedIsaPoliciesItem(
    customerReference = Some("INPOLY123D"),
    event = None,
    gainAmount = 1.26,
    taxPaidAmount = Some(1.36),
    yearsHeld = Some(5),
    yearsHeldSinceLastGain = Some(4)
  )

  private val foreignItemModel = AmendForeignPoliciesItem(
    customerReference = Some("INPOLY123E"),
    gainAmount = 1.27,
    taxPaidAmount = Some(1.37),
    yearsHeld = Some(6)
  )

  private val responseModel = AmendInsurancePoliciesRequestBody(
    lifeInsurance = Some(Seq(lifeInsuranceItemModel)),
    capitalRedemption = Some(Seq(capitalRedemptionItemModel)),
    lifeAnnuity = Some(Seq(lifeAnnuityItemModel)),
    voidedIsa = Some(Seq(voidedIsaItemModel)),
    foreign = Some(Seq(foreignItemModel))
  )

  "InsurancePoliciesItem" when {
    "read from valid JSON" should {
      "produce the expected object" in {
        json.as[AmendInsurancePoliciesRequestBody] shouldBe responseModel
      }
    }

    "read from invalid JSON" should {
      "produce a JsError" in {
        val invalidJson = Json.parse(
          """
            |{
            |  "lifeInsurance": [
            |    {
            |      "customerReference":true
            |    }
            |  ]
            |}
          """.stripMargin
        )

        invalidJson.validate[AmendInsurancePoliciesRequestBody] shouldBe a[JsError]
      }
    }

    "read from empty JSON" should {
      "produce an empty RetrieveInsurancePoliciesResponse Object" in {
        val emptyJson = JsObject.empty
        emptyJson.as[AmendInsurancePoliciesRequestBody] shouldBe AmendInsurancePoliciesRequestBody.empty
      }
    }

    "written to JSON" should {
      "produce the expected JSON" in {
        Json.toJson(responseModel) shouldBe json
      }
    }
  }

}
