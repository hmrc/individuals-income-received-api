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

package v1.models.response.retrieveAllResidentialPropertyCgt

import api.models.domain.MtdSourceEnum
import play.api.libs.json.{JsValue, Json}
import support.UnitSpec

class MultiplePropertyDisposalsSpec extends UnitSpec {

  val mtdJson: JsValue = Json.parse(
    """
      |{
      |        "source": "hmrcHeld",
      |        "submittedOn": "2020-07-06",
      |        "ppdSubmissionId": "Da2467289108",
      |        "ppdSubmissionDate": "2020-07-06T09:37:17Z",
      |        "numberOfDisposals": 3,
      |        "disposalTaxYear": 2022,
      |        "completionDate": "2022-03-08",
      |        "amountOfNetLoss": 1999.99
      |}
      |""".stripMargin
  )

  val desJson: JsValue = Json.parse(
    """
      |{
      |        "source": "HMRC HELD",
      |        "submittedOn": "2020-07-06",
      |        "ppdSubmissionId": "Da2467289108",
      |        "ppdSubmissionDate": "2020-07-06T09:37:17Z",
      |        "numberOfDisposals": 3,
      |        "disposalTaxYear": "2022",
      |        "completionDate": "2022-03-08",
      |        "amountOfLoss": 1999.99
      |}
      |""".stripMargin
  )

  val model: MultiplePropertyDisposals =
    MultiplePropertyDisposals(
      MtdSourceEnum.hmrcHeld,
      Some("2020-07-06"),
      "Da2467289108",
      Some("2020-07-06T09:37:17Z"),
      Some(3),
      Some(2022),
      Some("2022-03-08"),
      None,
      Some(1999.99)
    )

  "MultiplePropertyDisposals" when {
    "Reads" should {
      "return a valid object" when {
        "a valid json is supplied" in {
          desJson.as[MultiplePropertyDisposals] shouldBe model
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
