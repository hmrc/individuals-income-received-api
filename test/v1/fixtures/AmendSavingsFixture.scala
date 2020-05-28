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

import v1.models.request.savings.amend.{AmendForeignInterest, AmendSecuritiesItems, AmendSavingsRequestBody}

object AmendSavingsFixture {

  val fullAmendSecuritiesItemsModel: AmendSecuritiesItems =
    AmendSecuritiesItems(
      taxTakenOff = Some(100.0),
      grossAmount = Some(1455.0),
      netAmount = Some(123.22)
    )

  val minimalAmendSecuritiesItemsModel: AmendSecuritiesItems =  AmendSecuritiesItems(None,None,None)

  val fullAmendForeignInterestModel: AmendForeignInterest =
    AmendForeignInterest(
      amountBeforeTax = Some(1232.22),
      countryCode = "GER",
      taxTakenOff = Some(22.22),
      specialWithholdingTax = Some(22.22),
      taxableAmount = 2321.22,
      foreignTaxCreditRelief = true
    )

  val fullAmendForeignInterestModel2: AmendForeignInterest =
    AmendForeignInterest(
      amountBeforeTax = Some(1232.22),
      countryCode = "FRA",
      taxTakenOff = Some(22.22),
      specialWithholdingTax = Some(22.22),
      taxableAmount = 2321.22,
      foreignTaxCreditRelief = true
    )

  val minimalAmendForeignInterestModel: AmendForeignInterest =  AmendForeignInterest(None,"GER",None,None,2321.22,foreignTaxCreditRelief = true)

  val amendSavingsRequestModel: AmendSavingsRequestBody =
    AmendSavingsRequestBody(
      securities = Some(fullAmendSecuritiesItemsModel),
      foreignInterest = Some(Seq(fullAmendForeignInterestModel))
    )

  val requestModelNoSecurities: AmendSavingsRequestBody =
    AmendSavingsRequestBody(
      securities = None,
      foreignInterest = Some(Seq(fullAmendForeignInterestModel))
    )

  val requestModelNoForeignInterest: AmendSavingsRequestBody =
    AmendSavingsRequestBody(
      securities = Some(fullAmendSecuritiesItemsModel),
      foreignInterest = None
    )

  val requestModelMinimalForeignInterest: AmendSavingsRequestBody =
    AmendSavingsRequestBody(
      securities = Some(fullAmendSecuritiesItemsModel),
      foreignInterest = Some(Seq(minimalAmendForeignInterestModel))
    )

  val minimalAmendSavingsRequestModel: AmendSavingsRequestBody =
    AmendSavingsRequestBody(
      securities = None,
      foreignInterest = None
    )

  val requestModelWithMinimalFields: AmendSavingsRequestBody =
    AmendSavingsRequestBody(
      securities = None,
      foreignInterest = Some(Seq(minimalAmendForeignInterestModel))
    )

  val requestModelMultipleForeignInterest: AmendSavingsRequestBody =
    AmendSavingsRequestBody(
      securities = Some(fullAmendSecuritiesItemsModel),
      foreignInterest = Some(Seq(
        fullAmendForeignInterestModel,
        fullAmendForeignInterestModel2)
      )
    )
}
