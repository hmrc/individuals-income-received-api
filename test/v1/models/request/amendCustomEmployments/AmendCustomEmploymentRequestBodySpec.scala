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

package v1.models.request.amendCustomEmployments

import play.api.libs.json.{JsError, JsValue, Json}
import support.UnitSpec
import v1.models.request.amendCustomEmployment.AmendCustomEmploymentRequestBody

class AmendCustomEmploymentRequestBodySpec extends UnitSpec {

  private val model = AmendCustomEmploymentRequestBody(
    employerRef = Some("123/AZ12334"),
    employerName = "AMD infotech Ltd",
    startDate = "2019-01-01",
    cessationDate = Some("2020-06-01"),
    payrollId = Some("124214112412"),
    occupationalPension = false
  )

  private val minimumModel = AmendCustomEmploymentRequestBody(
    employerRef = None,
    employerName = "AMD infotech Ltd",
    startDate = "2019-01-01",
    cessationDate = None,
    payrollId = None,
    occupationalPension = false
  )

  private val json = Json.parse(
    """
      |{
      |  "employerRef": "123/AZ12334",
      |  "employerName": "AMD infotech Ltd",
      |  "startDate": "2019-01-01",
      |  "cessationDate": "2020-06-01",
      |  "payrollId": "124214112412",
      |  "occupationalPension": false
      |}
    """.stripMargin
  )

  private val desResponse: JsValue = Json.parse(
    """
      |{
      |  "employerRef": "123/AZ12334",
      |  "employerName": "AMD infotech Ltd",
      |  "startDate": "2019-01-01",
      |  "cessationDate": "2020-06-01",
      |  "payrollId": "124214112412",
      |  "occupationalPension": false
      |}
    """.stripMargin
  )

  private val mtdResponse: JsValue = Json.parse(
    """
      |{
      |  "employerRef": "123/AZ12334",
      |  "employerName": "AMD infotech Ltd",
      |  "startDate": "2019-01-01",
      |  "cessationDate": "2020-06-01",
      |  "payrollId": "124214112412",
      |  "occupationalPension": false
      |}
    """.stripMargin
  )

  private val desResponseInvalid: JsValue = Json.parse(
    """
      |{
      |  "employerRef": 123.12334,
      |  "employerName": "#123ad",
      |  "startDate": "abc",
      |  "cessationDate": 120.22,
      |  "payrollId": 124214112412,
      |  "occupationalPension": false
      |}
    """.stripMargin
  )

  private val minimumDesResponse: JsValue = Json.parse(
    """
      |{
      |  "employerName": "AMD infotech Ltd",
      |  "startDate": "2019-01-01",
      |  "occupationalPension": false
      |}
    """.stripMargin
  )

  private val minimumMtdResponse: JsValue = Json.parse(
    """
      |{
      |  "employerName": "AMD infotech Ltd",
      |  "startDate": "2019-01-01",
      |  "occupationalPension": false
      |}
    """.stripMargin
  )

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

      json.validate[AmendCustomEmploymentRequestBody] shouldBe a[JsError]
    }
  }

  "AmendCustomEmploymentRequestBody" when {
    "read from valid JSON" should {
      "produce the expected AmendCustomEmploymentRequestBody object" in {
        desResponse.as[AmendCustomEmploymentRequestBody] shouldBe model
      }
    }

    "read from a JSON with only mandatory fields" should {
      "produce the expected AmendCustomEmploymentRequestBody object" in {
        minimumDesResponse.as[AmendCustomEmploymentRequestBody] shouldBe minimumModel
      }
    }

    "read from invalid JSON" should {
      "produce a JsError" in {
        desResponseInvalid.validate[AmendCustomEmploymentRequestBody] shouldBe a[JsError]
      }
    }

    "written to JSON" should {
      "produce the expected JSON object" in {
        Json.toJson(model) shouldBe mtdResponse
      }
    }

    "not write empty fields" in {
      Json.toJson(minimumModel) shouldBe minimumMtdResponse
    }

    "AmendCustomEmploymentRequestBody" when {
      "read from valid JSON" should {
        "produce the expected AmendCustomEmploymentRequestBody object" in {
          json.as[AmendCustomEmploymentRequestBody] shouldBe model
        }
      }

      "written to JSON" should {
        "produce the expected JsObject" in {
          Json.toJson(model) shouldBe json
        }
      }
    }
  }

}
