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

import play.api.libs.json.{JsError, JsValue, Json}
import support.UnitSpec

class AmendForeignInterestItemSpec extends UnitSpec {

  val desResponse: JsValue = Json.parse(
    """
      |{
      |    "amountBeforeTax": 1232.22,
      |    "countryCode": "GER",
      |    "taxTakenOff": 22.22,
      |    "specialWithholdingTax": 22.22,
      |    "taxableAmount": 2321.22,
      |    "foreignTaxCreditRelief": true
      |}
    """.stripMargin
  )

  val model: AmendForeignInterestItem =
    AmendForeignInterestItem(
      amountBeforeTax = Some(1232.22),
      countryCode = "GER",
      taxTakenOff = Some(22.22),
      specialWithholdingTax = Some(22.22),
      taxableAmount = 2321.22,
      foreignTaxCreditRelief = Some(true)
    )

  val mtdResponse: JsValue = Json.parse(
    """
      |{
      |    "amountBeforeTax": 1232.22,
      |    "countryCode": "GER",
      |    "taxTakenOff": 22.22,
      |    "specialWithholdingTax": 22.22,
      |    "taxableAmount": 2321.22,
      |    "foreignTaxCreditRelief": true
      |}
    """.stripMargin
  )

  val desResponseInvalid: JsValue = Json.parse(
    """
      |{
      |    "amountBeforeTax": "ABC",
      |    "countryCode": "GER",
      |    "taxTakenOff": 22.22,
      |    "specialWithholdingTax": 22.22,
      |    "taxableAmount": 2321.22,
      |    "foreignTaxCreditRelief": true
      |}
    """.stripMargin
  )

  val minimumDesResponse: JsValue = Json.parse(
    """
      |{
      |    "countryCode": "GER",
      |    "taxableAmount": 100,
      |    "foreignTaxCreditRelief": true
      |}
    """.stripMargin
  )

  val minimumModel: AmendForeignInterestItem = AmendForeignInterestItem(
    amountBeforeTax = None,
    countryCode = "GER",
    taxTakenOff = None,
    specialWithholdingTax = None,
    taxableAmount = 100,
    foreignTaxCreditRelief = Some(true)
  )

  val minimumMtdResponse: JsValue = Json.parse(
    """
      |{
      |    "countryCode": "GER",
      |    "taxableAmount": 100,
      |    "foreignTaxCreditRelief": true
      |}
    """.stripMargin
  )

  "ForeignInterest" when {
    "read from valid JSON" should {
      "produce the expected ForeignInterest object" in {
        desResponse.as[AmendForeignInterestItem] shouldBe model
      }
    }

    "read from a JSON with only mandatory fields" should {
      "produce the expected ForeignInterest object" in {
        minimumDesResponse.as[AmendForeignInterestItem] shouldBe minimumModel
      }
    }

    "read from invalid JSON" should {
      "produce a JsError" in {
        desResponseInvalid.validate[AmendForeignInterestItem] shouldBe a[JsError]
      }
    }

    "written to JSON" should {
      "produce the expected JSON object" in {
        Json.toJson(model) shouldBe mtdResponse
      }

      "not write empty fields" in {
        Json.toJson(minimumModel) shouldBe minimumMtdResponse
      }
    }
  }

}
