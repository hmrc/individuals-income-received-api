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

import api.models.domain.Timestamp
import play.api.libs.json.{JsObject, JsValue, Json}
import v1.models.response.retrieveDividends._

object RetrieveDividendsFixtures {

  val foreignDividendModel: ForeignDividendItem = ForeignDividendItem(
    countryCode = "DEU",
    amountBeforeTax = Some(1000.99),
    taxTakenOff = Some(2000.99),
    specialWithholdingTax = Some(3000.99),
    foreignTaxCreditRelief = true,
    taxableAmount = 4000.99
  )

  val foreignDividendJson: JsValue = Json.parse(
    s"""
       |{
       |  "countryCode": "DEU",
       |  "amountBeforeTax": 1000.99,
       |  "taxTakenOff": 2000.99,
       |  "specialWithholdingTax": 3000.99,
       |  "foreignTaxCreditRelief": true,
       |  "taxableAmount": 4000.99
       |}
       |""".stripMargin
  )

  val dividendIncomeReceivedWhilstAbroadModel: DividendIncomeReceivedWhilstAbroadItem = DividendIncomeReceivedWhilstAbroadItem(
    countryCode = "DEU",
    amountBeforeTax = Some(1000.99),
    taxTakenOff = Some(2000.99),
    specialWithholdingTax = Some(3000.99),
    foreignTaxCreditRelief = true,
    taxableAmount = 4000.99
  )

  val dividendIncomeReceivedWhilstAbroadJson: JsValue = Json.parse(
    s"""
       |{
       |  "countryCode": "DEU",
       |  "amountBeforeTax": 1000.99,
       |  "taxTakenOff": 2000.99,
       |  "specialWithholdingTax": 3000.99,
       |  "foreignTaxCreditRelief": true,
       |  "taxableAmount": 4000.99
       |}
       |""".stripMargin
  )

  val stockDividendModel: StockDividend = StockDividend(customerReference = Some("my divs"), grossAmount = 1000.99)

  val stockDividendJson: JsValue = Json.parse(
    s"""
       |{
       |   "customerReference": "my divs",
       |   "grossAmount": 1000.99
       |}
       |""".stripMargin
  )

  val redeemableSharesModel: RedeemableShares = RedeemableShares(customerReference = Some("my shares"), grossAmount = 1000.99)

  val redeemableSharesJson: JsValue = Json.parse(
    s"""
       |{
       |   "customerReference": "my shares",
       |   "grossAmount": 1000.99
       |}
       |""".stripMargin
  )

  val bonusIssuesOfSecuritiesModel: BonusIssuesOfSecurities = BonusIssuesOfSecurities(customerReference = Some("my secs"), grossAmount = 1000.99)

  val bonusIssuesOfSecuritiesJson: JsValue = Json.parse(
    s"""
       |{
       |   "customerReference": "my secs",
       |   "grossAmount": 1000.99
       |}
       |""".stripMargin
  )

  val closeCompanyLoansWrittenOffModel: CloseCompanyLoansWrittenOff = CloseCompanyLoansWrittenOff(
    customerReference = Some("write off"),
    grossAmount = 1000.99
  )

  val closeCompanyLoansWrittenOffJson: JsValue = Json.parse(
    s"""
       |{
       |   "customerReference": "write off",
       |   "grossAmount": 1000.99
       |}
       |""".stripMargin
  )

  val responseModel: RetrieveDividendsResponse = RetrieveDividendsResponse(
    submittedOn = Timestamp("2020-07-06T09:37:17.000Z"),
    foreignDividend = Some(Seq(foreignDividendModel)),
    dividendIncomeReceivedWhilstAbroad = Some(Seq(dividendIncomeReceivedWhilstAbroadModel)),
    stockDividend = Some(stockDividendModel),
    redeemableShares = Some(redeemableSharesModel),
    bonusIssuesOfSecurities = Some(bonusIssuesOfSecuritiesModel),
    closeCompanyLoansWrittenOff = Some(closeCompanyLoansWrittenOffModel)
  )

  val responseJson: JsValue = Json.parse(
    s"""
      |{
      |   "submittedOn": "2020-07-06T09:37:17.000Z",
      |   "foreignDividend": [$foreignDividendJson],
      |   "dividendIncomeReceivedWhilstAbroad": [$dividendIncomeReceivedWhilstAbroadJson],
      |   "stockDividend": $stockDividendJson,
      |   "redeemableShares": $redeemableSharesJson,
      |   "bonusIssuesOfSecurities": $bonusIssuesOfSecuritiesJson,
      |   "closeCompanyLoansWrittenOff": $closeCompanyLoansWrittenOffJson
      |}
    """.stripMargin
  )

  def mtdResponseWithHateoas(nino: String, taxYear: String): JsObject = responseJson.as[JsObject] ++ Json
    .parse(
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
    )
    .as[JsObject]

}
