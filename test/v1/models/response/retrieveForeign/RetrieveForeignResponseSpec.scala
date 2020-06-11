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

package v1.models.response.retrieveForeign

import play.api.libs.json.{JsError, Json}
import support.UnitSpec
import v1.fixtures.foreign.RetrieveForeignFixture._

class RetrieveForeignResponseSpec extends UnitSpec {

  private val fullRetrieveForeignResponseJson = Json.parse(
    """
      |{
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

  private val fullRetrieveResponseBodyModel = RetrieveForeignResponse(
    Some(fullForeignEarningsModel),
    Some(Seq(fullUnremittableForeignIncomeModel1, fullUnremittableForeignIncomeModel2))
  )

  private val minRetrieveResponseBodyModel = RetrieveForeignResponse(None, None)

  private val responseModelNoUnremittableForeignIncome = RetrieveForeignResponse(Some(fullForeignEarningsModel), None)

  private val responseModelNoForeignEarnings = RetrieveForeignResponse(
    None,
    Some(Seq(fullUnremittableForeignIncomeModel1, fullUnremittableForeignIncomeModel2))
  )

  "RetrieveForeignResponse" when {
    "read from valid JSON" should {
      "produce the expected RetrieveForeignResponseBody model" in {
        fullRetrieveForeignResponseJson.as[RetrieveForeignResponse] shouldBe fullRetrieveResponseBodyModel
      }
    }

    "read from empty JSON" should {
      "produce a model with 'foreignEarnings' and 'unremittableForeignIncome' as None" in {
        val emptyJson = Json.parse("""{}""")

        emptyJson.as[RetrieveForeignResponse] shouldBe minRetrieveResponseBodyModel
      }
    }

    "read from valid JSON with an empty 'unremittableForeignIncome' array" should {
      "produce a model with 'unremittableForeignIncome' as None" in {
        val responseEmptyUnremittableForeignIncomeJson = Json.parse(
          """
            |{
            |   "foreignEarnings": {
            |     "customerReference": "FOREIGNINCME123A",
            |     "earningsNotTaxableUK": 1999.99
            |   },
            |   "unremittableForeignIncome": []
            |}
          """.stripMargin
        )

        responseEmptyUnremittableForeignIncomeJson.as[RetrieveForeignResponse] shouldBe responseModelNoUnremittableForeignIncome
      }
    }

    "read from valid JSON with empty 'foreignEarnings'" should {
      "produce a model with 'foreignEarnings' as None" in {
        val responseEmptyForeignEarningsJson = Json.parse(
          """
            |{
            |   "foreignEarnings": { },
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
            |
            |}
          """.stripMargin
        )

        responseEmptyForeignEarningsJson.as[RetrieveForeignResponse] shouldBe responseModelNoForeignEarnings
      }
    }

    "read from invalid JSON" should {
      "produce a JsError" in {
        val invalidJson = Json.parse(
          """
            |{
            |   "foreignEarnings": {
            |     "customerReference": false,
            |     "earningsNotTaxableUK": 1999.99
            |   }
            |}
          """.stripMargin
        )

        invalidJson.validate[RetrieveForeignResponse] shouldBe a[JsError]
      }
    }

    "written to JSON" should {
      "produce the expected JSON object" in {
        Json.toJson(fullRetrieveResponseBodyModel) shouldBe fullRetrieveForeignResponseJson
      }
    }
  }
}
