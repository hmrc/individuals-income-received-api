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

package v1.models.request.amendSavings

import play.api.libs.json.{JsError, JsValue, Json}
import support.UnitSpec
import v1.models.request.savings.amend.AmendForeignInterest
import v1.fixtures.AmendSavingsFixture._

class AmendForeignInterestSpec extends UnitSpec {

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

  val mtdResponseInvalid: JsValue = Json.parse(
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

  val minimalMtdResponse: JsValue = Json.parse(
    """
      |{
      |    "countryCode": "GER",
      |    "foreignTaxCreditRelief": true
      |}
    """.stripMargin
  )

  "AmendForeignInterest" when {
    "read from valid JSON" should {
      "convert valid MTD JSON into AmendForeignInterest model with all fields" in {
        mtdResponse.as[AmendForeignInterest] shouldBe fullAmendForeignInterestModel
      }
    }

    "read from a JSON with only mandatory fields" should {
      "convert MTD JSON with only mandatory fields to the expected AmendForeignInterest model" in {
        minimalMtdResponse.as[AmendForeignInterest] shouldBe minimalAmendForeignInterestModel
      }
    }

    "read from invalid JSON" should {
      "produce a JsError" in {
        mtdResponseInvalid.validate[AmendForeignInterest] shouldBe a[JsError]
      }
    }

    "written to DES JSON" should {
      "convert a AmendForeignInterest model into valid DES JSON" in {
        Json.toJson(fullAmendForeignInterestModel) shouldBe desResponse
      }
    }
  }
}
