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

package v1.models.response.retrieveSavings

import play.api.libs.json.{JsError, JsValue, Json}
import support.UnitSpec

class ForeignInterestItemSpec extends UnitSpec {

  val desResponse: JsValue = Json.parse(
    """
      |{
      |    "amountBeforeTax": 1232.22,
      |    "countryCode": "DEU",
      |    "taxTakenOff": 22.22,
      |    "specialWithholdingTax": 22.22,
      |    "taxableAmount": 2321.22,
      |    "foreignTaxCreditRelief": true
      |}
    """.stripMargin
  )

  val model: ForeignInterestItem =
    ForeignInterestItem(
      amountBeforeTax = Some(1232.22),
      countryCode = "DEU",
      taxTakenOff = Some(22.22),
      specialWithholdingTax = Some(22.22),
      taxableAmount = 2321.22,
      foreignTaxCreditRelief = true
    )

  val mtdResponse: JsValue = Json.parse(
    """
      |{
      |    "amountBeforeTax": 1232.22,
      |    "countryCode": "DEU",
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
      |    "countryCode": "DEU",
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
      |    "countryCode": "DEU",
      |    "taxableAmount": 100,
      |    "foreignTaxCreditRelief": true
      |}
    """.stripMargin
  )

  val minimumModel: ForeignInterestItem = ForeignInterestItem(
    amountBeforeTax = None,
    countryCode = "DEU",
    taxTakenOff = None,
    specialWithholdingTax = None,
    taxableAmount = 100,
    foreignTaxCreditRelief = true
  )

  val minimumMtdResponse: JsValue = Json.parse(
    """
      |{
      |    "countryCode": "DEU",
      |    "taxableAmount": 100,
      |    "foreignTaxCreditRelief": true
      |}
    """.stripMargin
  )

  "ForeignInterest" when {
    "read from valid JSON" should {
      "produce the expected ForeignInterest object" in {
        desResponse.as[ForeignInterestItem] shouldBe model
      }
    }

    "read from a JSON with only mandatory fields" should {
      "produce the expected ForeignInterest object" in {
        minimumDesResponse.as[ForeignInterestItem] shouldBe minimumModel
      }
    }

    "read from invalid JSON" should {
      "produce a JsError" in {
        desResponseInvalid.validate[ForeignInterestItem] shouldBe a[JsError]
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
