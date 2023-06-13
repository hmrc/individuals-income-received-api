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

package v1.fixtures

import api.models.domain.{MtdSourceEnum, Timestamp}
import play.api.libs.json.{JsObject, JsValue, Json}
import v1.models.response.retrieveAllResidentialPropertyCgt._

object RetrieveAllResidentialPropertyCgtControllerFixture {

  val multiplePropertyDisposals: MultiplePropertyDisposals =
    MultiplePropertyDisposals(
      MtdSourceEnum.hmrcHeld,
      Some(Timestamp("2020-07-06T09:37:17.000Z")),
      "Da2467289108",
      Some(Timestamp("2020-07-06T09:37:17.000Z")),
      Some(3),
      Some(2022),
      Some("2022-03-08"),
      Some(1999.99),
      None
    )

  val singlePropertyDisposals: SinglePropertyDisposals =
    SinglePropertyDisposals(
      MtdSourceEnum.hmrcHeld,
      Some(Timestamp("2020-07-06T09:37:17.000Z")),
      "Da2467289108",
      Some(Timestamp("2020-07-06T09:37:17.000Z")),
      Some("2022-02-04"),
      "2022-03-08",
      1999.99,
      Some("2018-04-06"),
      1999.99,
      Some(1999.99),
      Some(5000.99),
      Some(1999.99),
      Some(1999.99),
      Some(1999.99),
      Some(1999.99),
      Some(1999.99),
      None
    )

  val ppdService: PpdService =
    PpdService(
      Some(143.22),
      Some(Seq(multiplePropertyDisposals)),
      Some(Seq(singlePropertyDisposals))
    )

  val disposals: Disposals = Disposals(
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
    None,
    Some(1999.99)
  )

  val customerAddedDisposals: CustomerAddedDisposals =
    CustomerAddedDisposals(
      Timestamp("2020-07-06T09:37:17.000Z"),
      Seq(disposals)
    )

  val responseModel: RetrieveAllResidentialPropertyCgtResponse =
    RetrieveAllResidentialPropertyCgtResponse(
      Some(ppdService),
      Some(customerAddedDisposals)
    )

  val mtdJson: JsValue = Json.parse(
    """
      |{
      |  "ppdService": {
      |    "ppdYearToDate": 143.22,
      |    "multiplePropertyDisposals": [
      |      {
      |        "source": "hmrcHeld",
      |        "submittedOn": "2020-07-06T09:37:17.000Z",
      |        "ppdSubmissionId": "Da2467289108",
      |        "ppdSubmissionDate": "2020-07-06T09:37:17.000Z",
      |        "numberOfDisposals": 3,
      |        "disposalTaxYear": 2022,
      |        "completionDate": "2022-03-08",
      |        "amountOfNetGain": 1999.99
      |      }
      |    ],
      |    "singlePropertyDisposals": [
      |      {
      |        "source": "hmrcHeld",
      |        "submittedOn": "2020-07-06T09:37:17.000Z",
      |        "ppdSubmissionId": "Da2467289108",
      |        "ppdSubmissionDate": "2020-07-06T09:37:17.000Z",
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
      |        "amountOfNetGain": 1999.99
      |      }
      |    ]
      |  },
      |  "customerAddedDisposals": {
      |    "submittedOn": "2020-07-06T09:37:17.000Z",
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
      |        "amountOfNetLoss": 1999.99
      |      }
      |    ]
      |  }
      |}
      |""".stripMargin
  )

  val ifsJson: JsValue = Json.parse(
    """
      |{
      |  "ppdService": {
      |    "ppdYearToDate": 143.22,
      |    "multiplePropertyDisposals": [
      |      {
      |        "source": "HMRC HELD",
      |        "submittedOn": "2020-07-06T09:37:17Z",
      |        "ppdSubmissionId": "Da2467289108",
      |        "ppdSubmissionDate": "2020-07-06T09:37:17Z",
      |        "numberOfDisposals": 3,
      |        "disposalTaxYear": "2022",
      |        "completionDate": "2022-03-08",
      |        "amountOfNetGain": 1999.99
      |      }
      |    ],
      |    "singlePropertyDisposals": [
      |      {
      |        "source": "HMRC HELD",
      |        "submittedOn": "2020-07-06T09:37:17Z",
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
      |        "amountOfNetGain": 1999.99
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
      |        "amountOfLoss": 1999.99
      |      }
      |    ]
      |  }
      |}
      |""".stripMargin
  )

  def mtdResponseWithHateoas(nino: String, taxYear: String): JsObject = mtdJson.as[JsObject] ++ Json
    .parse(
      s"""
       |{
       |   "links":[
       |      {
       |         "href":"/individuals/income-received/disposals/residential-property/$nino/$taxYear/ppd",
       |         "method":"PUT",
       |         "rel":"create-and-amend-report-and-pay-capital-gains-tax-on-property-overrides"
       |      },
       |      {
       |         "href":"/individuals/income-received/disposals/residential-property/$nino/$taxYear/ppd",
       |         "method":"DELETE",
       |         "rel":"delete-report-and-pay-capital-gains-tax-on-property-overrides"
       |      },
       |      {
       |         "href":"/individuals/income-received/disposals/residential-property/$nino/$taxYear",
       |         "method":"PUT",
       |         "rel":"create-and-amend-cgt-residential-property-disposals"
       |      },
       |      {
       |         "href":"/individuals/income-received/disposals/residential-property/$nino/$taxYear",
       |         "method":"DELETE",
       |         "rel":"delete-cgt-residential-property-disposals"
       |      },
       |      {
       |         "href":"/individuals/income-received/disposals/residential-property/$nino/$taxYear",
       |         "method":"GET",
       |         "rel":"self"
       |      }
       |   ]
       |}
    """.stripMargin
    )
    .as[JsObject]

}
