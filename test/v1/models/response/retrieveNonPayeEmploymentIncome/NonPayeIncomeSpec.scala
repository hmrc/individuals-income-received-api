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

package v1.models.response.retrieveNonPayeEmploymentIncome

import play.api.libs.json.{JsValue, Json}
import support.UnitSpec

class NonPayeIncomeSpec extends UnitSpec {

  val mtdJson: JsValue = Json.parse(
    """
      |{
      |  "tips": 7654.32
      |}
      |""".stripMargin
  )

  val desJson: JsValue = Json.parse(
    """
      |{
      |  "tips": 7654.32
      |}
      |""".stripMargin
  )

  val model: NonPayeIncome =
    NonPayeIncome(Some(7654.32))

  "RetrieveAllResidentialPropertyCgtResponse" when {
    "Reads" should {
      "return a valid object" when {
        "a valid json is supplied" in {
          desJson.as[NonPayeIncome] shouldBe model
        }
      }
    }

    "writes" should {
      "produce the expected json" in {
        Json.toJson(model) shouldBe mtdJson
      }
    }
  }

}
