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

class EmployerSpec extends UnitSpec {

  val json: JsValue = Json.parse(
    """
      |{
      |  "employerRef": "anEmployerReference",
      |  "employerName": "Butlins Ltd"
      |}
    """.stripMargin
  )

  val model: Employer = Employer(
    employerRef = Some("anEmployerReference"),
    employerName = "Butlins Ltd"
  )

  "Employer" when {
    "read from valid JSON" should {
      "produce the expected 'Employer' object" in {
        json.as[Employer] shouldBe model
      }
    }

    "read from invalid JSON" should {
      "produce a JsError" in {
        val invalidJson: JsValue = Json.parse(
          """
            |{
            |  "employerRef": "anEmployerReference"
            |}
          """.stripMargin
        )

        invalidJson.validate[Employer] shouldBe a[JsError]
      }
    }

    "written to JSON" should {
      "produce the expected JsObject" in {
        Json.toJson(model) shouldBe json
      }
    }
  }

}
