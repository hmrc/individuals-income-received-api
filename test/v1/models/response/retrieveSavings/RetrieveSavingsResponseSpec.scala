/*
 * Copyright 2020 HM Revenue & Customs
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

package v1.models.response.retrieveSavings

import play.api.libs.json.{JsError, JsObject, JsValue, Json}
import support.UnitSpec

class RetrieveSavingsResponseSpec extends UnitSpec {

  private val desResponse: JsValue = Json.parse(
    """
      |{
      |   "submittedOn": "2019-04-04T01:01:01Z",
      |   "securities":
      |      {
      |         "taxTakenOff": 100.0,
      |         "grossAmount": 1455.0,
      |         "netAmount": 123.22
      |      },
      |   "foreignInterest": [
      |      {
      |         "amountBeforeTax": 1232.22,
      |         "countryCode": "DEU",
      |         "taxTakenOff": 22.22,
      |         "specialWithholdingTax": 22.22,
      |         "taxableAmount": 2321.22,
      |         "foreignTaxCreditRelief": true
      |      }
      |   ]
      |}
    """.stripMargin
  )

  private val securitiesModel: Securities =
    Securities(
      taxTakenOff = Some(100.0),
      grossAmount = 1455.0,
      netAmount = Some(123.22)
    )

  private val foreignInterestsItemModel: ForeignInterestItem =
    ForeignInterestItem(
      amountBeforeTax = Some(1232.22),
      countryCode = "DEU",
      taxTakenOff = Some(22.22),
      specialWithholdingTax = Some(22.22),
      taxableAmount = 2321.22,
      foreignTaxCreditRelief = true
    )

  private val model: RetrieveSavingsResponse =
    RetrieveSavingsResponse(
      submittedOn = "2019-04-04T01:01:01Z",
      securities = Some(securitiesModel),
      foreignInterest = Some(Seq(foreignInterestsItemModel))
    )

  val mtdJson: JsValue = Json.parse(
    """
      |{
      |   "submittedOn": "2019-04-04T01:01:01Z",
      |   "securities":
      |      {
      |         "taxTakenOff": 100.0,
      |         "grossAmount": 1455.0,
      |         "netAmount": 123.22
      |      },
      |   "foreignInterest": [
      |      {
      |         "amountBeforeTax": 1232.22,
      |         "countryCode": "DEU",
      |         "taxTakenOff": 22.22,
      |         "specialWithholdingTax": 22.22,
      |         "taxableAmount": 2321.22,
      |         "foreignTaxCreditRelief": true
      |      }
      |   ]
      |}
    """.stripMargin
  )

  private val desResponseInvalid: JsValue = Json.parse(
    """
      |{
      |   "submittedOn": "2019-04-04T01:01:01Z",
      |   "securities":
      |      {
      |         "taxTakenOff": "abc",
      |         "grossAmount": 1455.0,
      |         "netAmount": 123.22
      |      },
      |   "foreignInterest": [
      |      {
      |         "amountBeforeTax": 1232.22,
      |         "countryCode": "DEU",
      |         "taxTakenOff": 22.22,
      |         "specialWithholdingTax": 22.22,
      |         "taxableAmount": 2321.22,
      |         "foreignTaxCreditRelief": true
      |      }
      |   ]
      |}
    """.stripMargin
  )

  val emptyObjectsJson: JsValue = Json.parse(
    """
      |{
      |   "submittedOn": "",
      |   "securities": [],
      |   "foreignInterest": []
      |}
    """.stripMargin
  )

  val minimumObjectsJson: JsValue = Json.parse(
    """
      |{
      |   "submittedOn": ""
      |}
    """.stripMargin
  )

  "RetrieveSavingsResponse" when {
    "read from valid JSON" should {
      "produce the expected RetrieveSavingsResponse model" in {
        desResponse.as[RetrieveSavingsResponse] shouldBe model
      }
    }

    "read from invalid JSON" should {
      "produce a JsError" in {
        desResponseInvalid.validate[RetrieveSavingsResponse] shouldBe a[JsError]
      }
    }

    "written to JSON" should {
      "produce the expected JSON" in {
        Json.toJson(model) shouldBe mtdJson
      }
    }

    "written to JSON (no securities and foreignInterest)" should {
      "produce JSON with no securities and foreignInterest" in {
        Json.toJson(RetrieveSavingsResponse.empty) shouldBe minimumObjectsJson
      }
    }
  }
}
