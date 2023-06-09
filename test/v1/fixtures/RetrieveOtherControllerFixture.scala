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

import play.api.libs.json.{JsObject, JsValue, Json}

object RetrieveOtherControllerFixture {

  val fullRetrieveOtherResponse: JsValue = Json.parse(
    """
      |{
      |   "submittedOn":"2019-04-04T01:01:01.000Z",
      |   "postCessationReceipts":[
      |      {
      |         "customerReference":"String",
      |         "businessName":"LsMBEqEWnG9j,9JP9RpgkGmIcF2I30.NpxZRtgN3zA7-b8h-LvHvApdJtpY",
      |         "dateBusinessCeased":"2023-06-01",
      |         "businessDescription":"u2e'VarLXLa\\W&RHojlOZIqm9NDG",
      |         "incomeSource":"string",
      |         "amount":99999999999.99,
      |         "taxYearIncomeToBeTaxed":"2019-20"
      |      }
      |   ],
      |   "businessReceipts":[
      |      {
      |         "grossAmount":5000.99,
      |         "taxYear":"2018-19"
      |      },
      |      {
      |         "grossAmount":6000.99,
      |         "taxYear":"2019-20"
      |      }
      |   ],
      |   "allOtherIncomeReceivedWhilstAbroad":[
      |      {
      |         "countryCode":"FRA",
      |         "amountBeforeTax":1999.99,
      |         "taxTakenOff":2.23,
      |         "specialWithholdingTax":3.23,
      |         "foreignTaxCreditRelief":false,
      |         "taxableAmount":4.23,
      |         "residentialFinancialCostAmount":2999.99,
      |         "broughtFwdResidentialFinancialCostAmount":1999.99
      |      },
      |      {
      |         "countryCode":"IND",
      |         "amountBeforeTax":2999.99,
      |         "taxTakenOff":3.23,
      |         "specialWithholdingTax":4.23,
      |         "foreignTaxCreditRelief":true,
      |         "taxableAmount":5.23,
      |         "residentialFinancialCostAmount":3999.99,
      |         "broughtFwdResidentialFinancialCostAmount":2999.99
      |      }
      |   ],
      |   "overseasIncomeAndGains":{
      |      "gainAmount":3000.99
      |   },
      |   "chargeableForeignBenefitsAndGifts":{
      |      "transactionBenefit":1999.99,
      |      "protectedForeignIncomeSourceBenefit":2999.99,
      |      "protectedForeignIncomeOnwardGift":3999.99,
      |      "benefitReceivedAsASettler":4999.99,
      |      "onwardGiftReceivedAsASettler":5999.99
      |   },
      |   "omittedForeignIncome":{
      |      "amount":4000.99
      |   }
      |}
    """.stripMargin
  )

  def mtdResponseWithHateoas(nino: String, taxYear: String): JsObject = fullRetrieveOtherResponse.as[JsObject] ++ Json
    .parse(
      s"""
       |{
       |   "links":[
       |      {
       |         "href":"/individuals/income-received/other/$nino/$taxYear",
       |         "method":"PUT",
       |         "rel":"create-and-amend-other-income"
       |      },
       |      {
       |         "href":"/individuals/income-received/other/$nino/$taxYear",
       |         "method":"GET",
       |         "rel":"self"
       |      },
       |      {
       |         "href":"/individuals/income-received/other/$nino/$taxYear",
       |         "method":"DELETE",
       |         "rel":"delete-other-income"
       |      }
       |   ]
       |}
    """.stripMargin
    )
    .as[JsObject]

}
