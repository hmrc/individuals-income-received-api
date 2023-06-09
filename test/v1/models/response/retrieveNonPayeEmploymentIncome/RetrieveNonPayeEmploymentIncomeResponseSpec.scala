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

import api.models.domain.{MtdSourceEnum, Timestamp}
import play.api.libs.json.{JsValue, Json}
import support.UnitSpec

class RetrieveNonPayeEmploymentIncomeResponseSpec extends UnitSpec {

  val mtdJson: JsValue = Json.parse(
    """
      |{
      |  "submittedOn": "2020-07-06T09:37:17.000Z",
      |  "source": "hmrcHeld",
      |  "totalNonPayeIncome": 435.12,
      |  "nonPayeIncome": {
      |    "tips": 653.34
      |  }
      |}
      |""".stripMargin
  )

  val desJson: JsValue = Json.parse(
    """
      |{
      |  "submittedOn": "2020-07-06T09:37:17Z",
      |  "source": "HMRC HELD",
      |  "totalNonPayeIncome": 435.12,
      |  "nonPayeIncome": {
      |    "tips": 653.34
      |  }
      |}
      |""".stripMargin
  )

  val model: RetrieveNonPayeEmploymentIncomeResponse =
    RetrieveNonPayeEmploymentIncomeResponse(
      Timestamp("2020-07-06T09:37:17.000Z"),
      MtdSourceEnum.hmrcHeld,
      Some(435.12),
      Some(NonPayeIncome(Some(653.34)))
    )

  "RetrieveAllResidentialPropertyCgtResponse" when {
    "Reads" should {
      "return a valid object" when {
        "a valid json is supplied" in {
          desJson.as[RetrieveNonPayeEmploymentIncomeResponse] shouldBe model
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
