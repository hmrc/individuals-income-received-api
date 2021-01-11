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

package v1.models.response.retrieveForeign

import play.api.libs.json.{JsError, JsValue, Json}
import support.UnitSpec

class UnremittableForeignIncomeSpec extends UnitSpec {

  val fullUnremittableForeignIncomeJson: JsValue = Json.parse(
    """
      |{
      |    "countryCode": "FRA",
      |    "amountInForeignCurrency": 1999.99,
      |    "amountTaxPaid": 1999.99
      |}
    """.stripMargin
  )

  val fullUnremittableForeignIncomeModel1: UnremittableForeignIncome = UnremittableForeignIncome(
    countryCode =  "FRA",
    amountInForeignCurrency = 1999.99,
    amountTaxPaid = Some(1999.99)
  )

  "UnremittableForeignIncome" when {
    "read from valid JSON" should {
      "produce the expected UnremittableForeignIncome object" in {
        fullUnremittableForeignIncomeJson.as[UnremittableForeignIncome] shouldBe fullUnremittableForeignIncomeModel1
      }
    }

    "read from invalid JSON" should {
      "produce a JsError" in {
        val invalidJson = Json.parse("""{}""")

        invalidJson.validate[UnremittableForeignIncome] shouldBe a[JsError]
      }
    }

    "written to JSON" should {
      "produce the expected JSON object" in {
        Json.toJson(fullUnremittableForeignIncomeModel1) shouldBe fullUnremittableForeignIncomeJson
      }
    }
  }
}
