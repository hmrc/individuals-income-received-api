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

package v1.models.response.retrieveFinancialDetails

import play.api.libs.json.{JsError, JsValue, Json}
import support.UnitSpec

class PaySpec extends UnitSpec {

  val json: JsValue = Json.parse(
    """
      |{
      |  "taxablePayToDate": 100.11,
      |  "totalTaxToDate": 102.11,
      |  "payFrequency": "CALENDAR MONTHLY",
      |  "paymentDate": "2020-04-23",
      |  "taxWeekNo": 2,
      |  "taxMonthNo": 3
      |}
    """.stripMargin
  )

  val model: Pay = Pay(
    taxablePayToDate = Some(100.11),
    totalTaxToDate = Some(102.11),
    payFrequency = Some("CALENDAR MONTHLY"),
    paymentDate = Some("2020-04-23"),
    taxWeekNo = Some(2),
    taxMonthNo = Some(3)
  )

  "Pay" when {
    "read from valid JSON" should {
      "return the expected Pay object" in {
        json.as[Pay] shouldBe model
      }
    }

    "read from invalid JSON" should {
      "return a JsError" in {
        val invalidJson: JsValue = Json.parse(
          """
            |{
            |  "taxablePayToDate": "taxablePayToDate"
            |}
          """.stripMargin
        )

        invalidJson.validate[Pay] shouldBe a[JsError]
      }
    }

    "written to JSON" should {
      "produce the expected JsObject" in {
        Json.toJson(model) shouldBe json
      }
    }
  }

}
