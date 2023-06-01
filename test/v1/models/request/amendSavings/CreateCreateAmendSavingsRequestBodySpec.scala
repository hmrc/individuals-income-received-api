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

package v1.models.request.amendSavings

import play.api.libs.json.{JsError, JsObject, JsValue, Json}
import support.UnitSpec

class CreateCreateAmendSavingsRequestBodySpec extends UnitSpec {

  val mtdJson: JsValue = Json.parse(
    """
      |{
      |   "securities":
      |      {
      |         "taxTakenOff": 100.0,
      |         "grossAmount": 1455.0,
      |         "netAmount": 123.22
      |      },
      |   "foreignInterest": [
      |      {
      |         "amountBeforeTax": 1232.22,
      |         "countryCode": "GER",
      |         "taxTakenOff": 22.22,
      |         "specialWithholdingTax": 22.22,
      |         "taxableAmount": 2321.22,
      |         "foreignTaxCreditRelief": true
      |      }
      |   ]
      |}
    """.stripMargin
  )

  val securitiesModel: AmendSecurities =
    AmendSecurities(
      taxTakenOff = Some(100.0),
      grossAmount = 1455.0,
      netAmount = Some(123.22)
    )

  val foreignInterestItemModel: AmendForeignInterestItem =
    AmendForeignInterestItem(
      amountBeforeTax = Some(1232.22),
      countryCode = "GER",
      taxTakenOff = Some(22.22),
      specialWithholdingTax = Some(22.22),
      taxableAmount = 2321.22,
      foreignTaxCreditRelief = Some(true)
    )

  val requestBodyModel: CreateAmendSavingsRequestBody =
    CreateAmendSavingsRequestBody(
      securities = Some(securitiesModel),
      foreignInterest = Some(Seq(foreignInterestItemModel))
    )

  val desJson: JsValue = Json.parse(
    """
      |{
      |   "securities":
      |      {
      |         "taxTakenOff": 100.0,
      |         "grossAmount": 1455.0,
      |         "netAmount": 123.22
      |      },
      |   "foreignInterest": [
      |      {
      |         "amountBeforeTax": 1232.22,
      |         "countryCode": "GER",
      |         "taxTakenOff": 22.22,
      |         "specialWithholdingTax": 22.22,
      |         "taxableAmount": 2321.22,
      |         "foreignTaxCreditRelief": true
      |      }
      |   ]
      |}
    """.stripMargin
  )

  val emptyJson: JsValue = JsObject.empty

  val invalidJson: JsValue = Json.parse(
    """
      |{
      |   "securities":
      |      {
      |         "taxTakenOff": "abc",
      |         "grossAmount": 1455.0,
      |         "netAmount": 123.22
      |      },
      |   "foreignInterest": [
      |      {
      |         "amountBeforeTax": 1232.22,
      |         "countryCode": "GER",
      |         "taxTakenOff": 22.22,
      |         "specialWithholdingTax": 22.22,
      |         "taxableAmount": 2321.22,
      |         "foreignTaxCreditRelief": true
      |      }
      |   ]
      |}
    """.stripMargin
  )

  "AmendSavingsRequestBody" when {
    "read from valid JSON" should {
      "produce the expected AmendSavingsRequestBody object" in {
        mtdJson.as[CreateAmendSavingsRequestBody] shouldBe requestBodyModel
      }
    }

    "read from invalid JSON" should {
      "produce a JsError" in {
        invalidJson.validate[CreateAmendSavingsRequestBody] shouldBe a[JsError]
      }
    }

    "read from empty JSON" should {
      "produce an empty AmendSavingsRequestBody object" in {
        emptyJson.as[CreateAmendSavingsRequestBody] shouldBe CreateAmendSavingsRequestBody.empty
      }
    }

    "written JSON" should {
      "produce the expected JsObject" in {
        Json.toJson(requestBodyModel) shouldBe desJson
      }
    }

    "written from an empty object" should {
      "produce empty JSON" in {
        Json.toJson(CreateAmendSavingsRequestBody.empty) shouldBe emptyJson
      }
    }
  }

}
