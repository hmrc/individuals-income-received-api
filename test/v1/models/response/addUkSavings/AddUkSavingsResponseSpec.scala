/*
 * Copyright 2022 HM Revenue & Customs
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

package v1.models.response.addUkSavings

import play.api.libs.json.Json
import support.UnitSpec

class AddUkSavingsResponseSpec extends UnitSpec {

  val model: AddUkSavingsResponse = AddUkSavingsResponse("SAVKB2UVwUTBQGJ")

  val mtdJson = Json.parse(
    """
      |{
      |    "savingsAccountId": "SAVKB2UVwUTBQGJ"
      |}
      |""".stripMargin)

  val desJson = Json.parse(
    """
      |{
      |    "incomeSourceId": "SAVKB2UVwUTBQGJ"
      |}
      |""".stripMargin)


  "AddUkSavingsResponse" when {
    "read from valid JSON" should {
      "produce the expected object" in {
        desJson.as[AddUkSavingsResponse] shouldBe model
      }
    }

    "written to JSON" should {
      "produce the expected Json" in {
        Json.toJson(model) shouldBe mtdJson
      }
    }
  }
}
