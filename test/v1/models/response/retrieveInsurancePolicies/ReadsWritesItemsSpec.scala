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

import play.api.libs.json.{JsError, Json}
import support.UnitSpec

class ReadsWritesItemsSpec extends UnitSpec {

  private val json = Json.parse(
    """
      |[
      |  {
      |     "customerReference":"INPOLY123A",
      |     "event":"Death of spouse",
      |     "gainAmount":1.23,
      |     "taxPaid":1.23,
      |     "yearsHeld":2,
      |     "yearsHeldSinceLastGain":1,
      |     "deficiencyRelief":1.23
      |  }
      |]
    """.stripMargin
  )

  "voidedIsaReads" when {
    "read from JSON" should {
      "not read omitted fields" in {

        val model = InsurancePoliciesItem(
          customerReference = "INPOLY123A",
          event = Some("Death of spouse"),
          gainAmount = Some(1.23),
          taxPaid = Some(1.23),
          yearsHeld = Some(2),
          yearsHeldSinceLastGain = Some(1),
          deficiencyRelief = None
        )

        json.as[Seq[InsurancePoliciesItem]](ReadsWritesItems.voidedIsaReads) shouldBe Seq(model)
      }

      "produce a JsError for invalid a sequence of JSON containing an invalid object" in {
        val invalidJson = Json.parse("""[{}]""")
        invalidJson.validate[Seq[InsurancePoliciesItem]](ReadsWritesItems.voidedIsaReads) shouldBe a[JsError]
      }

      "produce a JsError when the supplied JSON is not an array" in {
        val invalidJson = Json.parse("""{}""")
        invalidJson.validate[Seq[InsurancePoliciesItem]](ReadsWritesItems.voidedIsaReads) shouldBe a[JsError]
      }
    }
  }

  "foreignReads" when {
    "read from JSON" should {
      "not read omitted fields" in {

        val model = InsurancePoliciesItem(
          customerReference = "INPOLY123A",
          event = None,
          gainAmount = Some(1.23),
          taxPaid = Some(1.23),
          yearsHeld = Some(2),
          yearsHeldSinceLastGain = None,
          deficiencyRelief = None
        )

        json.as[Seq[InsurancePoliciesItem]](ReadsWritesItems.foreignReads) shouldBe Seq(model)
      }

      "produce a JsError for invalid a sequence of JSON containing an invalid object" in {
        val invalidJson = Json.parse("""[{}]""")
        invalidJson.validate[Seq[InsurancePoliciesItem]](ReadsWritesItems.foreignReads) shouldBe a[JsError]
      }

      "produce a JsError when the supplied JSON is not an array" in {
        val invalidJson = Json.parse("""{}""")
        invalidJson.validate[Seq[InsurancePoliciesItem]](ReadsWritesItems.foreignReads) shouldBe a[JsError]
      }
    }
  }

}
