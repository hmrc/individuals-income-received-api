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

class UnremittableForeignIncomeSpec extends UnitSpec {

  "UnremittableForeignIncome" when {
    "read from valid JSON" should {
      "produce the expected UnremittableForeignIncome object" in {
        fullUnremittableForeignIncomeJson.as[UnremittableForeignIncome] shouldBe fullUnremittableForeignIncomeModel1
      }
    }

    "read from a JSON with only mandatory fields" should {
      "produce a UnremittableForeignIncome object with only mandatory fields" in {
        minUnremittableForeignIncomeJson.as[UnremittableForeignIncome] shouldBe minUnremittableForeignIncomeModel
      }
    }

    "read from empty JSON" should {
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
