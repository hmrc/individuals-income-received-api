/*
 * Copyright 2021 HM Revenue & Customs
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

import play.api.libs.json.{JsObject, JsValue, Json}

object RetrieveDividendsControllerFixture {

  val fullRetrieveDividendsResponse: JsValue = Json.parse(
    """
      |{
      |   "submittedOn": "2020-07-06T09:37:17Z",
      |   "foreignDividend": [
      |      {
      |        "countryCode": "DEU",
      |        "amountBeforeTax": 1232.22,
      |        "taxTakenOff": 22.22,
      |        "specialWithholdingTax": 27.35,
      |        "foreignTaxCreditRelief": true,
      |        "taxableAmount": 2321.22
      |      },
      |      {
      |        "countryCode": "FRA",
      |        "amountBeforeTax": 1350.55,
      |        "taxTakenOff": 25.27,
      |        "specialWithholdingTax": 30.59,
      |        "foreignTaxCreditRelief": false,
      |        "taxableAmount": 2500.99
      |      }
      |   ],
      |   "dividendIncomeReceivedWhilstAbroad": [
      |      {
      |        "countryCode": "DEU",
      |        "amountBeforeTax": 1232.22,
      |        "taxTakenOff": 22.22,
      |        "specialWithholdingTax": 27.35,
      |        "foreignTaxCreditRelief": true,
      |        "taxableAmount": 2321.22
      |      },
      |      {
      |        "countryCode": "FRA",
      |        "amountBeforeTax": 1350.55,
      |        "taxTakenOff": 25.27,
      |        "specialWithholdingTax": 30.59,
      |        "foreignTaxCreditRelief": false,
      |        "taxableAmount": 2500.99
      |       }
      |   ],
      |   "stockDividend": {
      |      "customerReference": "my divs",
      |      "grossAmount": 12321.22
      |   },
      |   "redeemableShares": {
      |      "customerReference": "my shares",
      |      "grossAmount": 12345.75
      |   },
      |   "bonusIssuesOfSecurities": {
      |      "customerReference": "my secs",
      |      "grossAmount": 12500.89
      |   },
      |   "closeCompanyLoansWrittenOff": {
      |      "customerReference": "write off",
      |      "grossAmount": 13700.55
      |   }
      |}
    """.stripMargin
  )

  def mtdResponseWithHateoas(nino: String, taxYear: String): JsObject = fullRetrieveDividendsResponse.as[JsObject] ++ Json.parse(
    s"""
       |{
       |   "links":[
       |      {
       |         "href":"/individuals/income-received/dividends/$nino/$taxYear",
       |         "method":"PUT",
       |         "rel":"create-and-amend-dividends-income"
       |      },
       |      {
       |         "href":"/individuals/income-received/dividends/$nino/$taxYear",
       |         "method":"GET",
       |         "rel":"self"
       |      },
       |      {
       |         "href":"/individuals/income-received/dividends/$nino/$taxYear",
       |         "method":"DELETE",
       |         "rel":"delete-dividends-income"
       |      }
       |   ]
       |}
    """.stripMargin
  ).as[JsObject]
}
