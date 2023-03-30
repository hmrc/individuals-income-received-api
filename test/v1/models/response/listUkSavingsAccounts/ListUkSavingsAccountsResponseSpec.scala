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

import play.api.libs.json.Json
import support.UnitSpec

class ListUkSavingsAccountsResponseSpec extends UnitSpec {

  val ukSavingsAccountsFromDES = Json.parse(
    """
      |[
      |  {
      |    "incomeSourceId": "000000000000001",
      |    "incomeSourceName": "Bank Account 1",
      |    "identifier": "AA111111A",
      |    "incomeSourceType": "interest-from-uk-banks"
      |  },
      |  {
      |    "incomeSourceId": "000000000000002",
      |    "incomeSourceName": "Bank Account 2",
      |    "identifier": "AA111111A",
      |    "incomeSourceType": "interest-from-uk-banks"
      |  },
      |  {
      |    "incomeSourceId": "000000000000003",
      |    "incomeSourceName": "Bank Account 3",
      |    "identifier": "AA111111A",
      |    "incomeSourceType": "interest-from-uk-banks"
      |  }
      |]
    """.stripMargin
  )

  val ukSavingsAccountsFromMTD = Json.parse(
    """
      |{
      |  "savingsAccounts":
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
      |  ]
      |}
    """.stripMargin
  )

  val emptyJson = Json.parse("""[]""")

  val validListUkSavingsAccountResponse = ListUkSavingsAccountsResponse(
    Some(
      Seq(
        UkSavingsAccount("000000000000001", "Bank Account 1"),
        UkSavingsAccount("000000000000002", "Bank Account 2"),
        UkSavingsAccount("000000000000003", "Bank Account 3")
      )
    )
  )

  "ListUkSavingsAccountsResponse" should {
    "return a valid ListUkSavingsAccountsResponse model " when {
      "a valid uk savings account list json from DES is supplied" in {
        ukSavingsAccountsFromDES.as[ListUkSavingsAccountsResponse[UkSavingsAccount]] shouldBe
          validListUkSavingsAccountResponse
      }
    }

    "return a valid list uk savings account response MTD json " when {
      "a valid UkSavingAccountListResponse is supplied " in {
        Json.toJson(validListUkSavingsAccountResponse) shouldBe
          ukSavingsAccountsFromMTD
      }
    }

    "return a valid empty ListUkSavingsAccountsResponse model " when {
      "a valid empty uk savings account list json from DES is supplied" in {
        emptyJson.as[ListUkSavingsAccountsResponse[UkSavingsAccount]] shouldBe
          ListUkSavingsAccountsResponse(None)
      }
    }
  }

}
