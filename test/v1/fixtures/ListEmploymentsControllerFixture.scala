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

package v1.fixtures

import play.api.libs.json.{JsObject, Json}

object ListEmploymentsControllerFixture {

  def mtdResponseWithCustomHateoas(nino: String, taxYear: String, employmentId: String): JsObject = Json
    .parse(
      s"""
       |{
       |   "employments": [{
       |         "employmentId": "$employmentId",
       |         "employerName": "Vera Lynn",
       |         "dateIgnored": "2020-06-17T10:53:38.000Z",
       |         "links": [{
       |               "href": "/individuals/income-received/employments/$nino/$taxYear/$employmentId",
       |               "method": "GET",
       |               "rel": "self"
       |         }]
       |      },
       |      {
       |         "employmentId": "$employmentId",
       |         "employerName": "Vera Lynn",
       |         "dateIgnored": "2020-06-17T10:53:38.000Z",
       |         "links": [{
       |               "href": "/individuals/income-received/employments/$nino/$taxYear/$employmentId",
       |               "method": "GET",
       |               "rel": "self"
       |         }]
       |   }],
       |   "customEmployments": [{
       |         "employmentId": "$employmentId",
       |         "employerName": "Vera Lynn",
       |         "links": [{
       |               "href": "/individuals/income-received/employments/$nino/$taxYear/$employmentId",
       |               "method": "GET",
       |               "rel": "self"
       |         }]
       |      },
       |      {
       |         "employmentId": "$employmentId",
       |         "employerName": "Vera Lynn",
       |         "links": [{
       |               "href": "/individuals/income-received/employments/$nino/$taxYear/$employmentId",
       |               "method": "GET",
       |               "rel": "self"
       |         }]
       |   }],
       |   "links": [
       |      {
       |         "href": "/individuals/income-received/employments/$nino/$taxYear",
       |         "method": "POST",
       |         "rel": "add-custom-employment"
       |      },
       |      {
       |         "href": "/individuals/income-received/employments/$nino/$taxYear",
       |         "method": "GET",
       |         "rel": "self"
       |      }
       |   ]
       |}
    """.stripMargin
    )
    .as[JsObject]

}
