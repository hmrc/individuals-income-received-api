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

package v1.models.response.listEmployment

import api.models.domain.Timestamp
import play.api.libs.json.{JsError, JsObject, Json}
import support.UnitSpec

class EmploymentSpec extends UnitSpec {

  private val hmrcEmploymentJson = Json.parse(
    """
      |{
      |   "employmentId": "00000000-0000-1000-8000-000000000001",
      |   "employerName": "Vera Lynn",
      |   "dateIgnored": "2020-06-17T10:53:38.000Z"
      |}
    """.stripMargin
  )

  private val customEmploymentJson = Json.parse(
    """
      |{
      |   "employmentId": "00000000-0000-1000-8000-000000000001",
      |   "employerName": "Vera Lynn"
      |}
    """.stripMargin
  )

  private val hmrcEmploymentModel =
    Employment(
      employmentId = "00000000-0000-1000-8000-000000000001",
      employerName = "Vera Lynn",
      dateIgnored = Some(Timestamp("2020-06-17T10:53:38.000Z")))

  private val customEmploymentModel = Employment(employmentId = "00000000-0000-1000-8000-000000000001", employerName = "Vera Lynn")

  "Employment" should {
    "return a valid HMRC Employment model" when {
      "a valid hmrc employment json is supplied" in {
        hmrcEmploymentJson.as[Employment] shouldBe hmrcEmploymentModel
      }
    }

    "return a valid Custom Employment model" when {
      "a valid custom employment json is supplied" in {
        customEmploymentJson.as[Employment] shouldBe customEmploymentModel
      }
    }

    "return a JsError" when {
      "empty JSON is supplied" in {
        val invalidJson = JsObject.empty

        invalidJson.validate[Employment] shouldBe a[JsError]
      }
    }

    "written to JSON" should {
      "produce the expected hmrc JsObject" in {
        Json.toJson(hmrcEmploymentModel) shouldBe hmrcEmploymentJson
      }

      "produce the expected custom JsObject" in {
        Json.toJson(customEmploymentModel) shouldBe customEmploymentJson
      }
    }
  }

}
