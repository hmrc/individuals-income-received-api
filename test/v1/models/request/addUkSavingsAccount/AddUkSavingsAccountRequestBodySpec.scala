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

package v1.models.request.addUkSavingsAccount

import play.api.libs.json.{JsError, JsObject, Json}
import support.UnitSpec

class AddUkSavingsAccountRequestBodySpec extends UnitSpec {

  val mtdJson = Json.parse("""
      |{
      |   "accountName": "Shares savings account"
      |}
      |""".stripMargin)

  val desJson = Json.parse("""
      |{
      |    "incomeSourceType": "interest-from-uk-banks",
      |    "incomeSourceName": "Shares savings account"
      |}
      |""".stripMargin)

  val model: AddUkSavingsAccountRequestBody = AddUkSavingsAccountRequestBody("Shares savings account")

  "AddUkSavings" when {
    "read from a valid JSON" should {
      "produce the expected object" in {
        mtdJson.as[AddUkSavingsAccountRequestBody] shouldBe model
      }
    }
  }

  "read from empty JSON" should {
    "produce a JsError" in {
      val invalidJson = JsObject.empty
      invalidJson.validate[AddUkSavingsAccountRequestBody] shouldBe a[JsError]
    }
  }

  "written to JSON" should {
    "Produce the expected JsObject" in {
      Json.toJson(model) shouldBe desJson
    }
  }

}
