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

package v1.models.response.listUkSavingsAccounts

import play.api.libs.json.{JsError, Json}
import support.UnitSpec

class UkSavingsAccountSpec extends UnitSpec {

  val validUkSavingsAccountFromDESJson = Json.parse(
    """
      |{
      |   "incomeSourceId": "SAVKB2UVwUTBQGJ",
      |   "incomeSourceName": "Shares savings account"
      |}
    """.stripMargin
  )

  val invalidUkSavingsAccountFromDESJson = Json.parse(
    """
      |{
      |   "incomeSourceId": "SAVKB2UVwUTBQGJ"
      |}
    """.stripMargin
  )

  val validUkSavigsAccountFromMTDJson = Json.parse(
    """
      |{
      |   "savingsAccountId": "SAVKB2UVwUTBQGJ",
      |   "accountName": "Shares savings account"
      |}
    """.stripMargin
  )

  val emptyJson = Json.parse("{}")

  "UkSavingsAccount" should {
    "return a valid UkSavingsAccount model " when {
      "a valid uk savings account json from DES is supplied" in {
        validUkSavingsAccountFromDESJson.as[UkSavingsAccount] shouldBe
          UkSavingsAccount("SAVKB2UVwUTBQGJ", "Shares savings account")
      }
    }

    "return a JsError" when {
      "an invalid uk savings account json from DES is supplied" in {
        invalidUkSavingsAccountFromDESJson.validate[UkSavingsAccount] shouldBe a[JsError]
      }
    }

    "return a JsError" when {
      "an empty json from DES is supplied" in {
        emptyJson.validate[UkSavingsAccount] shouldBe a[JsError]
      }
    }

    "return a valid MTD uk savings json" when {
      "a valid UkSavingsAccount model is supplier" in {
        Json.toJson(UkSavingsAccount("SAVKB2UVwUTBQGJ", "Shares savings account")) shouldBe
          validUkSavigsAccountFromMTDJson
      }
    }
  }

}
