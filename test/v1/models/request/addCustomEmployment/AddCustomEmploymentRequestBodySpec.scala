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

package v1.models.request.addCustomEmployment

import play.api.libs.json.{JsError, JsValue, Json}
import support.UnitSpec

class AddCustomEmploymentRequestBodySpec extends UnitSpec {

  "AddCustomEmploymentRequestBody" when {
    val json: JsValue = Json.parse(
      """
        |{
        |  "employerRef": "123/AB56797",
        |  "employerName": "AMD infotech Ltd",
        |  "startDate": "2019-01-01",
        |  "cessationDate": "2020-06-01",
        |  "payrollId": "124214112412",
        |  "occupationalPension": false
        |}
    """.stripMargin
    )

    val model: AddCustomEmploymentRequestBody = AddCustomEmploymentRequestBody(
      employerRef = Some("123/AB56797"),
      employerName = "AMD infotech Ltd",
      startDate = "2019-01-01",
      cessationDate = Some("2020-06-01"),
      payrollId = Some("124214112412"),
      occupationalPension = false
    )

    "read from valid JSON" should {
      "produce the expected AddCustomEmploymentRequestBody object" in {
        json.as[AddCustomEmploymentRequestBody] shouldBe model
      }
    }

    "read from invalid JSON" should {
      "produce a JsError" in {
        val json: JsValue = Json.parse(
          """
            |{
            |  "employerRef": "123/AB56797",
            |  "cessationDate": "2020-06-01",
            |  "payrollId": "124214112412",
            |  "occupationalPension": false
            |}
          """.stripMargin
        )

        json.validate[AddCustomEmploymentRequestBody] shouldBe a[JsError]
      }
    }

    "written to JSON" should {
      "produce the expected JsObject" in {
        Json.toJson(model) shouldBe json
      }
    }
  }

}
