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

package v1.fixtures.foreign

import play.api.libs.json.{JsValue, Json}
import v1.models.response.retrieveForeign.{ForeignEarnings, UnremittableForeignIncome}

object RetrieveForeignFixture {

  val fullForeignEarningsModel: ForeignEarnings = ForeignEarnings(
    customerReference = Some("FOREIGNINCME123A"),
    earningsNotTaxableUK = Some(1999.99)
  )

  val fullForeignEarningsJson: JsValue = Json.parse(
    """
      |{
      |    "customerReference": "FOREIGNINCME123A",
      |    "earningsNotTaxableUK": 1999.99
      |}
    """.stripMargin
  )

  val minForeignEarningsModel: ForeignEarnings = ForeignEarnings(
    customerReference = None,
    earningsNotTaxableUK = None
  )

  val minForeignEarningsJson: JsValue = Json.parse("""{}""")


  val fullUnremittableForeignIncomeModel1: UnremittableForeignIncome = UnremittableForeignIncome(
    countryCode =  "FRA",
    amountInForeignCurrency = Some(1999.99),
    amountTaxPaid = Some(1999.99)
  )

  val fullUnremittableForeignIncomeModel2: UnremittableForeignIncome = UnremittableForeignIncome(
    countryCode =  "IND",
    amountInForeignCurrency = Some(2999.99),
    amountTaxPaid = Some(2999.99)
  )

  val fullUnremittableForeignIncomeJson: JsValue = Json.parse(
    """
      |{
      |    "countryCode": "FRA",
      |    "amountInForeignCurrency": 1999.99,
      |    "amountTaxPaid": 1999.99
      |}
    """.stripMargin
  )

  val minUnremittableForeignIncomeModel: UnremittableForeignIncome = UnremittableForeignIncome(
    countryCode =  "IND",
    amountInForeignCurrency = None,
    amountTaxPaid = None
  )

  val minUnremittableForeignIncomeJson: JsValue = Json.parse(
    """
      |{
      |    "countryCode": "IND"
      |}
    """.stripMargin
  )
}
