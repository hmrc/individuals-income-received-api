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

package v1.fixtures

import play.api.libs.json.{JsObject, Json}

object RetrieveEmploymentControllerFixture {

  private val hmrcEnteredResponse = Json.parse(
    """
      |{
      |    "employments": {
      |            "employmentId": "00000000-0000-1000-8000-000000000000",
      |            "employerName": "Vera Lynn",
      |            "employerRef": "123/abc",
      |            "payrollId": "123345657",
      |            "startDate": "2020-06-17",
      |            "cessationDate": "2020-06-17",
      |            "dateIgnored": "2020-06-17T10:53:38Z"
      |        }
      |}
    """.stripMargin
  )

  private val customEnteredResponse = Json.parse(
    """
      |{
      |    "customerDeclaredEmployments": {
      |            "employmentId": "00000000-0000-1000-8000-000000000000",
      |            "employerName": "Vera Lynn",
      |            "employerRef": "123/abc",
      |            "payrollId": "123345657",
      |            "startDate": "2020-06-17",
      |            "cessationDate": "2020-06-17",
      |            "submittedOn": "2020-06-17T10:53:38Z"
      |        }
      |}
    """.stripMargin
  )

  def mtdResponseWithHateoas(nino: String, taxYear: String, employmentId: String): JsObject = hmrcEnteredResponse.as[JsObject] ++ Json.parse(
    s"""
      |{
      |   "links":[
      |      {
      |         "href": "/individuals/income-received/employments/$nino/$taxYear",
      |         "method": "GET",
      |         "rel": "list-employments"
      |      },
      |      {
      |         "href": "/individuals/income-received/employments/$nino/$taxYear/$employmentId",
      |         "method": "GET",
      |         "rel": "self"
      |      },
      |      {
      |         "href": "/individuals/income-received/employments/$nino/$taxYear/$employmentId",
      |         "method": "PUT",
      |         "rel": "amend-custom-employment"
      |      },
      |      {
      |         "href": "/individuals/income-received/employments/$nino/$taxYear/$employmentId",
      |         "method": "delete-custom-employment",
      |         "rel": "self"
      |      }
      |   ]
      |}
      |""".stripMargin
  ).as[JsObject]

}
