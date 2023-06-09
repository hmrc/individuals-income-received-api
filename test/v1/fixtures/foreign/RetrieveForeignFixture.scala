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

package v1.fixtures.foreign

import play.api.libs.json.{JsObject, JsValue, Json}

object RetrieveForeignFixture {

  val fullRetrieveForeignResponseJson: JsValue = Json.parse(
    """
      |{
      |   "submittedOn": "2019-04-04T01:01:01.000Z",
      |   "foreignEarnings": {
      |     "customerReference": "FOREIGNINCME123A",
      |     "earningsNotTaxableUK": 1999.99
      |   },
      |   "unremittableForeignIncome": [
      |     {
      |       "countryCode": "FRA",
      |       "amountInForeignCurrency": 1999.99,
      |       "amountTaxPaid": 1999.99
      |     },
      |     {
      |       "countryCode": "IND",
      |       "amountInForeignCurrency": 2999.99,
      |       "amountTaxPaid": 2999.99
      |     }
      |   ]
      |}
    """.stripMargin
  )

  def mtdResponseWithHateoas(nino: String, taxYear: String): JsObject = fullRetrieveForeignResponseJson.as[JsObject] ++ Json
    .parse(
      s"""
       |{
       |   "links":[
       |      {
       |         "href":"/individuals/income-received/foreign/$nino/$taxYear",
       |         "method":"GET",
       |         "rel":"self"
       |      },
       |      {
       |         "href":"/individuals/income-received/foreign/$nino/$taxYear",
       |         "method":"PUT",
       |         "rel":"create-and-amend-foreign-income"
       |      },
       |      {
       |         "href":"/individuals/income-received/foreign/$nino/$taxYear",
       |         "method":"DELETE",
       |         "rel":"delete-foreign-income"
       |      }
       |   ]
       |}
    """.stripMargin
    )
    .as[JsObject]

}
