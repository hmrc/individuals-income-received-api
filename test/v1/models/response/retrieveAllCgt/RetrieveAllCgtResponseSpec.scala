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

package v1.models.response.retrieveAllCgt

import play.api.libs.json.{JsValue, Json}
import support.UnitSpec
import v1.models.domain.MtdSourceEnum

class RetrieveAllCgtResponseSpec extends UnitSpec {

  private val mtdJson: JsValue = Json.parse(
    """
      |{
      |  "ppdService": {
      |    "multiplePropertyDisposals": [
      |      {
      |        "source": "hmrcHeld",
      |        "submittedOn": "2020-07-06",
      |        "ppdSubmissionId": "Da2467289108",
      |        "ppdSubmissionDate": "2020-07-06T09:37:17Z",
      |        "numberOfDisposals": 3,
      |        "disposalTaxYear": 2022,
      |        "completionDate": "2022-03-08",
      |        "amountOfNetGain": 1999.99,
      |        "amountOfNetLoss": 1999.99,
      |        "ppdReturnCharge": 1999.99
      |      }
      |    ],
      |    "singlePropertyDisposals": [
      |      {
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
      |        "amountOfNetGain": 1999.99,
      |        "amountOfNetLoss": 1999.99,
      |        "ppdReturnCharge": 1999.99
      |      }
      |    ]
      |  },
      |  "customerAddedDisposals": {
      |    "submittedOn": "2020-07-06T09:37:17Z",
      |    "disposals": [
      |      {
      |        "customerReference": "CGTDISPOSAL01",
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
      |        "amountOfNetGain": 1999.99,
      |        "amountOfNetLoss": 1999.99
      |      }
      |    ]
      |  }
      |}
      |""".stripMargin
  )

  private val desJson: JsValue = Json.parse(
    """
      |{
      |  "ppdService": {
      |    "multiplePropertyDisposals": [
      |      {
      |        "source": "HMRC HELD",
      |        "submittedOn": "2020-07-06",
      |        "ppdSubmissionId": "Da2467289108",
      |        "ppdSubmissionDate": "2020-07-06T09:37:17Z",
      |        "numberOfDisposals": 3,
      |        "disposalTaxYear": 2022,
      |        "completionDate": "2022-03-08",
      |        "amountOfNetGain": 1999.99,
      |        "amountOfLoss": 1999.99,
      |        "ppdReturnCharge": 1999.99
      |      }
      |    ],
      |    "singlePropertyDisposals": [
      |      {
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
      |        "amountOfNetGain": 1999.99,
      |        "amountOfLoss": 1999.99,
      |        "ppdReturnCharge": 1999.99
      |      }
      |    ]
      |  },
      |  "customerAddedDisposals": {
      |    "submittedOn": "2020-07-06T09:37:17Z",
      |    "disposals": [
      |      {
      |        "customerReference": "CGTDISPOSAL01",
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
      |        "amountOfNetGain": 1999.99,
      |        "amountOfLoss": 1999.99
      |      }
      |    ]
      |  }
      |}
      |""".stripMargin
  )

  private val multiplePropertyDisposals: MultiplePropertyDisposals =
    MultiplePropertyDisposals(
      MtdSourceEnum.hmrcHeld,
      Some("2020-07-06"),
      "Da2467289108",
      Some("2020-07-06T09:37:17Z"),
      Some(3),
      Some(2022),
      Some("2022-03-08"),
      Some(1999.99),
      Some(1999.99),
      Some(1999.99)
    )

  private val singlePropertyDisposals: SinglePropertyDisposals =
    SinglePropertyDisposals(
      MtdSourceEnum.hmrcHeld,
      Some("2020-07-06"),
      "Da2467289108",
      Some("2020-07-06T09:37:17Z"),
      "2022-02-04",
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
      Some(1999.99),
      Some(1999.99),
      Some(1999.99)
    )

  private val ppdService: PpdServiceObject =
    PpdServiceObject(
      Some(Seq(multiplePropertyDisposals)),
      Some(Seq(singlePropertyDisposals))
    )

  private val disposals: Disposals = Disposals(
    Some("CGTDISPOSAL01"),
    "2022-02-04",
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
    Some(1999.99),
    Some(1999.99)
  )

  private val customerAddedDisposals: CustomerAddedDisposals =
    CustomerAddedDisposals(
      "2020-07-06T09:37:17Z",
      Seq(disposals)
    )

  private val model: RetrieveAllCgtResponse =
    RetrieveAllCgtResponse(
      Some(ppdService),
      Some(customerAddedDisposals)
    )


  "RetrieveAllCgtResponse" when {
    "Reads" should {
      "return a valid object" when {
        "a valid json is supplied" in {
          desJson.as[RetrieveAllCgtResponse] shouldBe model
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
