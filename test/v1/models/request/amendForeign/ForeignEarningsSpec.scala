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

package v1.models.request.amendForeign

import play.api.libs.json.{JsError, JsObject, Json}
import support.UnitSpec

class ForeignEarningsSpec extends UnitSpec {

  private val json = Json.parse(
    """
      |{
      |  "customerReference": "ref",
      |  "earningsNotTaxableUK": 111.11
      |}
    """.stripMargin
  )

  private val model = ForeignEarnings(
    customerReference = Some("ref"),
    earningsNotTaxableUK = Some(111.11)
  )

  "ForeignEarnings" when {
    "read from valid JSON" should {
      "produce the expected ForeignEarnings object" in {
        json.as[ForeignEarnings] shouldBe model
      }
    }

    "read from empty JSON" should {
      "produce the expected ForeignEarnings object" in {
        val emptyJson = JsObject.empty
        emptyJson.as[ForeignEarnings] shouldBe ForeignEarnings.empty
      }
    }

    "read from invalid JSON" should {
      "produce a JsError" in {
        val invalidJson = Json.parse(
          """
            |{
            |  "customerReference": true,
            |  "earningsNotTaxableUK": "111.11"
            |}
          """.stripMargin
        )

        invalidJson.validate[ForeignEarnings] shouldBe a[JsError]
      }
    }

    "written to JSON" should {
      "produce the expected JsObject" in {
        Json.toJson(model) shouldBe json
      }
    }
  }
}
