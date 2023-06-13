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
import play.api.libs.json.{JsObject, Json}
import support.UnitSpec

class ListEmploymentResponseSpec extends UnitSpec {

  private val desJson = Json.parse(
    """
      |{
      |    "employments": [{
      |            "employmentId": "00000000-0000-1000-8000-000000000000",
      |            "employerName": "Vera Lynn",
      |            "employerRef": "123/abc",
      |            "payrollId": "123345657",
      |            "startDate": "2020-06-17",
      |            "cessationDate": "2020-06-17",
      |            "dateIgnored": "2020-06-17T10:53:38Z"
      |        },
      |        {
      |            "employmentId": "00000000-0000-1000-8000-000000000001",
      |            "employerName": "Vera Lynn",
      |            "employerRef": "123/abc",
      |            "payrollId": "123345657",
      |            "startDate": "2020-06-17",
      |            "cessationDate": "2020-06-17",
      |            "dateIgnored": "2020-06-17T10:53:38Z"
      |        }
      |    ],
      |    "customerDeclaredEmployments": [{
      |            "employmentId": "00000000-0000-1000-8000-000000000002",
      |            "employerName": "Vera Lynn",
      |            "employerRef": "123/abc",
      |            "payrollId": "123345657",
      |            "startDate": "2020-06-17",
      |            "cessationDate": "2020-06-17",
      |            "submittedOn": "2020-06-17T10:53:38Z"
      |        },
      |        {
      |            "employmentId": "00000000-0000-1000-8000-000000000003",
      |            "employerName": "Vera Lynn",
      |            "employerRef": "123/abc",
      |            "payrollId": "123345657",
      |            "startDate": "2020-06-17",
      |            "cessationDate": "2020-06-17",
      |            "submittedOn": "2020-06-17T10:53:38Z"
      |        }
      |    ]
      |}
    """.stripMargin
  )

  private val mtdJson = Json.parse(
    """
      |{
      |   "employments":[
      |      {
      |         "employmentId":"00000000-0000-1000-8000-000000000000",
      |         "employerName":"Vera Lynn",
      |         "dateIgnored":"2020-06-17T10:53:38.000Z"
      |      },
      |      {
      |         "employmentId":"00000000-0000-1000-8000-000000000001",
      |         "employerName":"Vera Lynn",
      |         "dateIgnored":"2020-06-17T10:53:38.000Z"
      |      }
      |   ],
      |   "customEmployments":[
      |      {
      |         "employmentId":"00000000-0000-1000-8000-000000000002",
      |         "employerName":"Vera Lynn"
      |      },
      |      {
      |         "employmentId":"00000000-0000-1000-8000-000000000003",
      |         "employerName":"Vera Lynn"
      |      }
      |   ]
      |}
    """.stripMargin
  )

  private val listEmploymentResponseModel =
    ListEmploymentResponse(
      employments = Some(
        Seq(
          Employment(
            employmentId = "00000000-0000-1000-8000-000000000000",
            employerName = "Vera Lynn",
            dateIgnored = Some(Timestamp("2020-06-17T10:53:38.000Z"))),
          Employment(
            employmentId = "00000000-0000-1000-8000-000000000001",
            employerName = "Vera Lynn",
            dateIgnored = Some(Timestamp("2020-06-17T10:53:38.000Z")))
        )),
      customEmployments = Some(
        Seq(
          Employment(employmentId = "00000000-0000-1000-8000-000000000002", employerName = "Vera Lynn"),
          Employment(employmentId = "00000000-0000-1000-8000-000000000003", employerName = "Vera Lynn")
        ))
    )

  "ListEmploymentResponse" should {
    "the expected ListEmploymentResponse object" when {
      "a valid full json is supplied" in {
        desJson.as[ListEmploymentResponse[Employment]] shouldBe listEmploymentResponseModel
      }

      "a valid only HMRC employments json is supplied" in {
        val desHmrcJson = Json.parse(
          """
            |{
            |    "employments": [{
            |            "employmentId": "00000000-0000-1000-8000-000000000000",
            |            "employerName": "Vera Lynn",
            |            "employerRef": "123/abc",
            |            "payrollId": "123345657",
            |            "startDate": "2020-06-17",
            |            "cessationDate": "2020-06-17",
            |            "dateIgnored": "2020-06-17T10:53:38Z"
            |        },
            |        {
            |            "employmentId": "00000000-0000-1000-8000-000000000001",
            |            "employerName": "Vera Lynn",
            |            "employerRef": "123/abc",
            |            "payrollId": "123345657",
            |            "startDate": "2020-06-17",
            |            "cessationDate": "2020-06-17",
            |            "dateIgnored": "2020-06-17T10:53:38Z"
            |        }
            |    ]
            |}
            """.stripMargin
        )
        desHmrcJson.as[ListEmploymentResponse[Employment]] shouldBe listEmploymentResponseModel.copy(customEmployments = None)
      }

      "a valid only custom employments json is supplied" in {
        val desCustomJson = Json.parse(
          """
            |{
            |    "customerDeclaredEmployments": [{
            |            "employmentId": "00000000-0000-1000-8000-000000000002",
            |            "employerName": "Vera Lynn",
            |            "employerRef": "123/abc",
            |            "payrollId": "123345657",
            |            "startDate": "2020-06-17",
            |            "cessationDate": "2020-06-17",
            |            "submittedOn": "2020-06-17T10:53:38Z"
            |        },
            |        {
            |            "employmentId": "00000000-0000-1000-8000-000000000003",
            |            "employerName": "Vera Lynn",
            |            "employerRef": "123/abc",
            |            "payrollId": "123345657",
            |            "startDate": "2020-06-17",
            |            "cessationDate": "2020-06-17",
            |            "submittedOn": "2020-06-17T10:53:38Z"
            |        }
            |    ]
            |}
            """.stripMargin
        )
        desCustomJson.as[ListEmploymentResponse[Employment]] shouldBe listEmploymentResponseModel.copy(employments = None)
      }
    }
  }

  "read from empty JSON" should {
    "produce an empty ListEmploymentResponse object" in {
      val emptyJson = JsObject.empty

      emptyJson.as[ListEmploymentResponse[Employment]] shouldBe ListEmploymentResponse(None, None)
    }
  }

  "written to JSON" should {
    "produce the expected Json" in {
      Json.toJson(listEmploymentResponseModel) shouldBe mtdJson
    }
  }

}
