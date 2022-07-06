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

package v1.models.response.listUkSavingsAccount
import play.api.libs.json.Json
import support.UnitSpec

class ListUkSavingsAccountSpec extends UnitSpec {

  val ukSavingsAccountsFromDES =
    Json.parse("""
      |[
      |   {
      |      "incomeSourceId": "000000000000001",
      |      "incomeSourceName": "Bank Account 1",
      |      "identifier": "AA111111A",
      |      "incomeSourceType": "interest-from-uk-banks"
      |   },
      |   {
      |      "incomeSourceId": "000000000000002",
      |      "incomeSourceName": "Bank Account 2",
      |      "identifier": "AA111111A",
      |      "incomeSourceType": "interest-from-uk-banks"
      |   },
      |   {
      |      "incomeSourceId": "000000000000003",
      |      "incomeSourceName": "Bank Account 3",
      |      "identifier": "AA111111A",
      |      "incomeSourceType": "interest-from-uk-banks"
      |   }
      |]
    """.stripMargin)

  val ukSavingsAccountsFromMDT = Json.parse(
    """
         |{
         | "savingsAccounts":
         |  [
         |    {
         |        "savingsAccountId": "000000000000001",
         |        "accountName": "Bank Account 1"
         |    },
         |    {
         |        "savingsAccountId": "000000000000002",
         |        "accountName": "Bank Account 2"
         |    },
         |    {
         |        "savingsAccountId": "000000000000003",
         |        "accountName": "Bank Account 3"
         |    }
         | ]
         |}
       """.stripMargin
  )

    val ukSavingsAccountsFromMDTWithHateos = Json.parse(
    """
         |{
         | "savingsAccounts":
         |  [
         |    {
         |        "savingsAccountId": "000000000000001",
         |        "accountName": "Bank Account 1"
         |    },
         |    {
         |        "savingsAccountId": "000000000000002",
         |        "accountName": "Bank Account 2"
         |    },
         |    {
         |        "savingsAccountId": "000000000000003",
         |        "accountName": "Bank Account 3"
         |    }
         | ],
         | "links": [
         |      {
         |         "href":"/individuals/income-received/savings/uk-accounts/{nino}",
         |         "rel":"add-uk-savings-account",
         |         "method":"POST"
         |      },
         |      {
         |         "href":"/individuals/income-received/savings/uk-accounts/{nino}",
         |         "rel":"self",
         |         "method":"GET"
         |      }
         | ]
         |}
         |
       """.stripMargin
  )

  val validListUkSavingsAccountResponse = ListUkSavingsAccountResponse(
    Some(
      Seq(
        UkSavingsAccount("000000000000001", "Bank Account 1"),
        UkSavingsAccount("000000000000002", "Bank Account 2"),
        UkSavingsAccount("000000000000003", "Bank Account 3")
      )
    )
  )

  "ListUkSavingsAccountResponse" should {
    "return a valid ListUkSavingsAccountResponse model " when {
      "a valid uk savings account list json from DES is supplied" in {
        ukSavingsAccountsFromDES.as[ListUkSavingsAccountResponse[UkSavingsAccount]] shouldBe
          validListUkSavingsAccountResponse
      }
    }

    "reutrn a valid list uk savings account response MTD json " when {
      "a valid UkSavingAccountListResponse is supplied " in {
        Json.toJson(validListUkSavingsAccountResponse) shouldBe
          ukSavingsAccountsFromMDT

      }
    }
  }
}
