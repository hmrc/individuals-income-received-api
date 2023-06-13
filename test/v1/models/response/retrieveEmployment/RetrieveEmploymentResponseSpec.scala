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

package v1.models.response.retrieveEmployment

import api.models.domain.Timestamp
import play.api.libs.json.{JsError, Json}
import support.UnitSpec

class RetrieveEmploymentResponseSpec extends UnitSpec {

  private val hmrcEnteredJson = Json.parse(
    """
      |{
      |   "employments": {
      |      "employmentId": "00000000-0000-1000-8000-000000000000",
      |      "employerName": "Vera Lynn",
      |      "employerRef": "123/abc",
      |      "payrollId": "123345657",
      |      "occupationalPension": false,
      |      "startDate": "2020-06-17",
      |      "cessationDate": "2020-06-17",
      |      "dateIgnored": "2020-06-17T10:53:38Z"
      |   }
      |}
    """.stripMargin
  )

  private val customEnteredJson = Json.parse(
    """
      |{
      |   "customerDeclaredEmployments": {
      |      "employmentId": "00000000-0000-1000-8000-000000000000",
      |      "employerName": "Vera Lynn",
      |      "employerRef": "123/abc",
      |      "payrollId": "123345657",
      |      "occupationalPension": false,
      |      "startDate": "2020-06-17",
      |      "cessationDate": "2020-06-17",
      |      "submittedOn": "2020-06-17T10:53:38Z"
      |   }
      |}
    """.stripMargin
  )

  private val hmrcEnteredMtdJson = Json.parse(
    """
      |{
      |   "employerRef": "123/abc",
      |   "employerName": "Vera Lynn",
      |   "startDate": "2020-06-17",
      |   "cessationDate": "2020-06-17",
      |   "payrollId": "123345657",
      |   "occupationalPension": false,
      |   "dateIgnored": "2020-06-17T10:53:38.000Z"
      |}
    """.stripMargin
  )

  private val customEnteredMtdJson = Json.parse(
    """
      |{
      |   "employerRef": "123/abc",
      |   "employerName": "Vera Lynn",
      |   "startDate": "2020-06-17",
      |   "cessationDate": "2020-06-17",
      |   "payrollId": "123345657",
      |   "occupationalPension": false,
      |   "submittedOn": "2020-06-17T10:53:38.000Z"
      |}
    """.stripMargin
  )

  private val hmrcEnteredEmployment = RetrieveEmploymentResponse(
    employerRef = Some("123/abc"),
    employerName = "Vera Lynn",
    startDate = Some("2020-06-17"),
    cessationDate = Some("2020-06-17"),
    payrollId = Some("123345657"),
    occupationalPension = Some(false),
    dateIgnored = Some(Timestamp("2020-06-17T10:53:38.000Z")),
    None
  )

  private val customEnteredEmployment = RetrieveEmploymentResponse(
    employerRef = Some("123/abc"),
    employerName = "Vera Lynn",
    startDate = Some("2020-06-17"),
    cessationDate = Some("2020-06-17"),
    payrollId = Some("123345657"),
    occupationalPension = Some(false),
    None,
    submittedOn = Some(Timestamp("2020-06-17T10:53:38.000Z"))
  )

  "Reads" should {
    "return RetrieveEmploymentResponse object" when {
      "a valid HMRC entered employment json is supplied" in {
        hmrcEnteredJson.as[RetrieveEmploymentResponse] shouldBe hmrcEnteredEmployment
      }
    }

    "return RetrieveEmploymentResponse object" when {
      "a valid customer entered employment json is supplied" in {
        customEnteredJson.as[RetrieveEmploymentResponse] shouldBe customEnteredEmployment
      }
    }
  }

  "read from invalid JSON" should {
    "return a JsError" in {
      val invalidJson = Json.parse(
        """
          |{
          |   "customerDeclaredEmployments": {
          |      "employmentId": "00000000-0000-1000-8000-000000000000",
          |      "employerRef": "123/abc",
          |      "payrollId": "123345657",
          |      "startDate": "2020-06-17",
          |      "cessationDate": "2020-06-17",
          |      "submittedOn": "2020-06-17T10:53:38Z"
          |   }
          |}
          """.stripMargin
      )
      invalidJson.validate[RetrieveEmploymentResponse] shouldBe a[JsError]
    }
  }

  "writes" should {
    "return the expected hmrc entered format json" in {
      Json.toJson(hmrcEnteredEmployment) shouldBe hmrcEnteredMtdJson
    }

    "return the expected customer entered format json" in {
      Json.toJson(customEnteredEmployment) shouldBe customEnteredMtdJson
    }
  }

}
