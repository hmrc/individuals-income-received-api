/*
 * Copyright 2021 HM Revenue & Customs
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

package v1.models.response.retrieveForeign

import play.api.libs.json.{JsError, JsValue, Json}
import support.UnitSpec

class ForeignEarningsSpec extends UnitSpec {

  val fullForeignEarningsJson: JsValue = Json.parse(
    """
      |{
      |    "customerReference": "FOREIGNINCME123A",
      |    "earningsNotTaxableUK": 1999.99
      |}
    """.stripMargin
  )

  val fullForeignEarningsModel: ForeignEarnings = ForeignEarnings(
    customerReference = Some("FOREIGNINCME123A"),
    earningsNotTaxableUK = 1999.99
  )

  "ForeignEarnings" when {
    "read from valid JSON" should {
      "produce the expected ForeignEarnings object" in {
        fullForeignEarningsJson.as[ForeignEarnings] shouldBe fullForeignEarningsModel
      }
    }

    "read from invalid JSON" should {
      "produce a JsError" in {
        val invalidJson = Json.parse(
          """
            |{
            |  "customerReference": false,
            |  "earningsNotTaxableUK": "1999.99"
            |}
          """.stripMargin
        )

        invalidJson.validate[ForeignEarnings] shouldBe a[JsError]
      }
    }

    "written to JSON" should {
      "produce the expected JSON object" in {
        Json.toJson(fullForeignEarningsModel) shouldBe fullForeignEarningsJson
      }
    }
  }
}
