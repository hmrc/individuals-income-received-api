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

import play.api.libs.json.{JsObject, JsValue, Json}
import v1.models.response.retrieveSavings.{ForeignInterest, RetrieveSavingsResponse, Securities}

object RetrieveSavingsFixture {

  val desRetrieveSavingsResponse: JsValue = Json.parse(
    """
      |{
      |   "securities":
      |      {
      |         "taxTakenOff": 100.0,
      |         "grossAmount": 1455.0,
      |         "netAmount": 123.22
      |      },
      |   "foreignInterest": [
      |      {
      |         "amountBeforeTax": 1232.22,
      |         "countryCode": "GER",
      |         "taxTakenOff": 22.22,
      |         "specialWithholdingTax": 22.22,
      |         "taxableAmount": 2321.22,
      |         "foreignTaxCreditRelief": true
      |      }
      |   ]
      |}
    """.stripMargin
  )

  val mtdRetrieveSavingsResponse: JsValue = Json.parse(
    """
      |{
      |   "securities":
      |      {
      |         "taxTakenOff": 100.0,
      |         "grossAmount": 1455.0,
      |         "netAmount": 123.22
      |      },
      |   "foreignInterest": [
      |      {
      |         "amountBeforeTax": 1232.22,
      |         "countryCode": "GER",
      |         "taxTakenOff": 22.22,
      |         "specialWithholdingTax": 22.22,
      |         "taxableAmount": 2321.22,
      |         "foreignTaxCreditRelief": true
      |      }
      |   ]
      |}
    """.stripMargin
  )

  def mtdResponseWithHateoas(nino: String, taxYear: String): JsObject = mtdRetrieveSavingsResponse.as[JsObject] ++ Json.parse(
    s"""
       |{
       |   "links":[
       |      {
       |         "href":"/individuals/income-received/savings/$nino/$taxYear",
       |         "method":"PUT",
       |         "rel":"amend-savings-income"
       |      },
       |      {
       |         "href":"/individuals/income-received/savings/$nino/$taxYear",
       |         "method":"GET",
       |         "rel":"self"
       |      },
       |      {
       |         "href":"/individuals/income-received/savings/$nino/$taxYear",
       |         "method":"DELETE",
       |         "rel":"delete-savings-income"
       |      }
       |   ]
       |}
    """.stripMargin
  ).as[JsObject]

  val fullSecuritiesItemsModel: Securities =
    Securities(
      taxTakenOff = Some(100.0),
      grossAmount = Some(1455.0),
      netAmount = Some(123.22)
    )

  val minimalSecuritiesItemsModel: Securities =  Securities(None,None,None)

  val fullForeignInterestsModel: ForeignInterest =
    ForeignInterest(
      amountBeforeTax = Some(1232.22),
      countryCode = "GER",
      taxTakenOff = Some(22.22),
      specialWithholdingTax = Some(22.22),
      taxableAmount = 2321.22,
      foreignTaxCreditRelief = true
    )

  val fullForeignInterestsModel2: ForeignInterest =
    ForeignInterest(
      amountBeforeTax = Some(1232.22),
      countryCode = "FRA",
      taxTakenOff = Some(22.22),
      specialWithholdingTax = Some(22.22),
      taxableAmount = 2321.22,
      foreignTaxCreditRelief = true
    )

  val minimalForeignInterestsModel: ForeignInterest =  ForeignInterest(
    amountBeforeTax = None,
    countryCode = "GER",
    taxTakenOff = None,
    specialWithholdingTax = None,
    taxableAmount = 100,
    foreignTaxCreditRelief = true
  )

  val retrieveSavingsResponseModel: RetrieveSavingsResponse =
    RetrieveSavingsResponse(
      securities = Some(fullSecuritiesItemsModel),
      foreignInterest = Some(Seq(fullForeignInterestsModel))
    )

  val responseModelNoSecurities: RetrieveSavingsResponse =
    RetrieveSavingsResponse(
      securities = None,
      foreignInterest = Some(Seq(fullForeignInterestsModel))
    )

  val responseModelNoForeignInterest: RetrieveSavingsResponse =
    RetrieveSavingsResponse(
      securities = Some(fullSecuritiesItemsModel),
      foreignInterest = None
    )

  val responseModelMinimalForeignInterest: RetrieveSavingsResponse =
    RetrieveSavingsResponse(
      securities = Some(fullSecuritiesItemsModel),
      foreignInterest = Some(Seq(minimalForeignInterestsModel))
    )

  val minimalRetrieveSavingsResponseModel: RetrieveSavingsResponse =
    RetrieveSavingsResponse(
      securities = None,
      foreignInterest = None
    )

  val responseModelWithMinimalFields: RetrieveSavingsResponse =
    RetrieveSavingsResponse(
      securities = None,
      foreignInterest = Some(Seq(minimalForeignInterestsModel))
    )

  val responseModelMultipleForeignInterest: RetrieveSavingsResponse =
    RetrieveSavingsResponse(
      securities = Some(fullSecuritiesItemsModel),
      foreignInterest = Some(Seq(
        fullForeignInterestsModel,
        fullForeignInterestsModel2)
      )
    )
}
