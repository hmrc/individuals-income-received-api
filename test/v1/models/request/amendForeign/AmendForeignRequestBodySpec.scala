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

package v1.models.request.amendForeign

import play.api.libs.json.{JsError, Json}
import support.UnitSpec

class AmendForeignRequestBodySpec extends UnitSpec {

  private val json = Json.parse(
    """
      |{
      |   "foreignEarnings": {
      |     "customerReference": "ref",
      |     "earningsNotTaxableUK": 111.11
      |   },
      |   "unremittableForeignIncome": [
      |     {
      |       "countryCode": "GBR",
      |       "amountInForeignCurrency": 222.22,
      |       "amountTaxPaid": 333.33
      |     },
      |     {
      |       "countryCode": "DEU",
      |       "amountInForeignCurrency": 444.44,
      |       "amountTaxPaid": 555.55
      |     }
      |   ]
      |}
    """.stripMargin
  )

  private val foreignEarningsModel = ForeignEarnings(
    customerReference = Some("ref"),
    earningsNotTaxableUK = 111.11
  )

  private val unremittableForeignIncomeModel = Seq(
    UnremittableForeignIncomeItem(
      countryCode = "GBR",
      amountInForeignCurrency = 222.22,
      amountTaxPaid = Some(333.33)
    ),
    UnremittableForeignIncomeItem(
      countryCode = "DEU",
      amountInForeignCurrency = 444.44,
      amountTaxPaid = Some(555.55)
    )
  )

  private val requestBodyModel = AmendForeignRequestBody(
    Some(foreignEarningsModel),
    Some(unremittableForeignIncomeModel)
  )

  "AmendForeignRequestBody" when {
    "read from valid JSON" should {
      "produce the expected AmendForeignRequestBody object" in {
        json.as[AmendForeignRequestBody] shouldBe requestBodyModel
      }
    }

    "read from valid JSON with an empty 'unremittableForeignIncome' array" should {
      "produce the expected AmendForeignRequestBody object" in {
        val json = Json.parse(
          """
            |{
            |   "foreignEarnings": {
            |     "customerReference": "ref",
            |     "earningsNotTaxableUK": 111.11
            |   },
            |   "unremittableForeignIncome": [
            |   ]
            |}
          """.stripMargin
        )

        json.as[AmendForeignRequestBody] shouldBe requestBodyModel.copy(unremittableForeignIncome = None)
      }
    }

    "read from invalid JSON" should {
      "produce a JsError" in {
        val json = Json.parse(
          """
            |{
            |   "foreignEarnings": {
            |     "customerReference": true,
            |     "earningsNotTaxableUK": 111.11
            |   }
            |}
          """.stripMargin
        )

        json.validate[AmendForeignRequestBody] shouldBe a[JsError]
      }
    }

    "written to JSON" should {
      "produce the expected JsObject" in {
        Json.toJson(requestBodyModel) shouldBe json
      }
    }
  }
}
