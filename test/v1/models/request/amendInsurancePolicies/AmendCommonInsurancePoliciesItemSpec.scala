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

class AmendCommonInsurancePoliciesItemSpec extends UnitSpec {

  private val json = Json.parse(
    """
      |{
      |   "customerReference": "INPOLY123A",
      |   "event": "Death of spouse",
      |   "gainAmount": 1.23,
      |   "taxPaid": true,
      |   "yearsHeld": 2,
      |   "yearsHeldSinceLastGain": 1,
      |   "deficiencyRelief": 1.23
      |}
    """.stripMargin
  )

  private val model = AmendCommonInsurancePoliciesItem(
    customerReference = Some("INPOLY123A"),
    event = Some("Death of spouse"),
    gainAmount = 1.23,
    taxPaid = true,
    yearsHeld = Some(2),
    yearsHeldSinceLastGain = Some(1),
    deficiencyRelief = Some(1.23)
  )

  "InsurancePoliciesItem" when {
    "read from valid JSON" should {
      "produce the expected object" in {
        json.as[AmendCommonInsurancePoliciesItem] shouldBe model
      }
    }

    "read from invalid JSON" should {
      "produce a JsError" in {
        val invalidJson = JsObject.empty
        invalidJson.validate[AmendCommonInsurancePoliciesItem] shouldBe a[JsError]
      }
    }

    "written to JSON" should {
      "produce the expected JSON" in {
        Json.toJson(model) shouldBe json
      }
    }
  }

}
