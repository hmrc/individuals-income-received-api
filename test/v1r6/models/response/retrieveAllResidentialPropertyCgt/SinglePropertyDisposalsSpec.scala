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

package v1r6.models.response.retrieveAllResidentialPropertyCgt

import play.api.libs.json.{JsValue, Json}
import support.UnitSpec
import v1r6.models.domain.MtdSourceEnum

class SinglePropertyDisposalsSpec extends UnitSpec {

  val mtdJson: JsValue = Json.parse(
    """
      |{
      |        "source": "hmrcHeld",
      |        "submittedOn": "2020-07-06",
      |        "ppdSubmissionId": "Da2467289108",
      |        "ppdSubmissionDate": "2020-07-06T09:37:17Z",
      |        "disposalDate": "2022-02-04",
      |        "completionDate": "2022-03-08",
      |        "disposalProceeds": 1999.99,
      |        "acquisitionDate": "2018-04-06",
      |        "acquisitionAmount": 1999.99,
      |        "improvementCosts": 1999.99,
      |        "additionalCosts": 5000.99,
      |        "prfAmount": 1999.99,
      |        "otherReliefAmount": 1999.99,
      |        "lossesFromThisYear": 1999.99,
      |        "lossesFromPreviousYear": 1999.99,
      |        "amountOfNetLoss": 1999.99,
      |        "ppdReturnCharge": 1999.99
      |      }
      |""".stripMargin
  )


  val desJson: JsValue = Json.parse(
    """
      |{
      |        "source": "HMRC HELD",
      |        "submittedOn": "2020-07-06",
      |        "ppdSubmissionId": "Da2467289108",
      |        "ppdSubmissionDate": "2020-07-06T09:37:17Z",
      |        "disposalDate": "2022-02-04",
      |        "completionDate": "2022-03-08",
      |        "disposalProceeds": 1999.99,
      |        "acquisitionDate": "2018-04-06",
      |        "acquisitionAmount": 1999.99,
      |        "improvementCosts": 1999.99,
      |        "additionalCosts": 5000.99,
      |        "prfAmount": 1999.99,
      |        "otherReliefAmount": 1999.99,
      |        "lossesFromThisYear": 1999.99,
      |        "lossesFromPreviousYear": 1999.99,
      |        "amountOfLoss": 1999.99,
      |        "ppdReturnCharge": 1999.99
      |      }
      |""".stripMargin
  )


  val model: SinglePropertyDisposals =
    SinglePropertyDisposals(
      MtdSourceEnum.hmrcHeld,
      Some("2020-07-06"),
      "Da2467289108",
      Some("2020-07-06T09:37:17Z"),
      Some("2022-02-04"),
      "2022-03-08",
      1999.99,
      "2018-04-06",
      1999.99,
      Some(1999.99),
      Some(5000.99),
      Some(1999.99),
      Some(1999.99),
      Some(1999.99),
      Some(1999.99),
      None,
      Some(1999.99),
      Some(1999.99)
    )


  "SinglePropertyDisposals" when {
    "Reads" should {
      "return a valid object" when {
        "a valid json is supplied" in {
          desJson.as[SinglePropertyDisposals] shouldBe model
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
