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

import v1.models.response.savings.retrieveSavings.{ForeignInterest, RetrieveSavingsResponse, SecuritiesItems}

object RetrieveSavingsFixture {

  val fullSecuritiesItemsModel: SecuritiesItems =
    SecuritiesItems(
      taxTakenOff = Some(100.0),
      grossAmount = Some(1455.0),
      netAmount = Some(123.22)
    )

  val minimalSecuritiesItemsModel: SecuritiesItems =  SecuritiesItems(None,None,None)

  val fullForeignInterestsModel: ForeignInterest =
    ForeignInterest(
      amountBeforeTax = Some(1232.22),
      countryCode = "GER",
      taxTakenOff = Some(22.22),
      specialWithholdingTax = Some(22.22),
      taxableAmount = Some(2321.22),
      foreignTaxCreditRelief = true
    )

  val fullForeignInterestsModel2: ForeignInterest =
    ForeignInterest(
      amountBeforeTax = Some(1232.22),
      countryCode = "FRA",
      taxTakenOff = Some(22.22),
      specialWithholdingTax = Some(22.22),
      taxableAmount = Some(2321.22),
      foreignTaxCreditRelief = true
    )

  val minimalForeignInterestsModel: ForeignInterest =  ForeignInterest(None,"GER",None,None,None,true)

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
