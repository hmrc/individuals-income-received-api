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

import play.api.libs.json.{JsError, JsValue, Json}
import support.UnitSpec
import v1.fixtures.RetrieveSavingsFixture
import v1.models.response.savings.retrieveSavings.ForeignInterest

class ForeignInterestSpec extends UnitSpec {

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

  val minimalDesResponse: JsValue = Json.parse(
    """
      |{
      |    "countryCode": "GER",
      |    "foreignTaxCreditRelief": true
      |}
    """.stripMargin
  )

  "ForeignInterest" when {
    "read from valid JSON" should {
      "produce the expected ForeignInterest object" in {
        desResponse.as[ForeignInterest] shouldBe RetrieveSavingsFixture.fullForeignInterestsModel
      }
    }

    "read from a JSON with only mandatory fields" should {
      "produce a ForeignInterest object with only mandatory fields" in {
        minimalDesResponse.as[ForeignInterest] shouldBe RetrieveSavingsFixture.minimalForeignInterestsModel
      }
    }

    "read from invalid JSON" should {
      "produce a JsError" in {
        desResponseInvalid.validate[ForeignInterest] shouldBe a[JsError]
      }
    }

    "written to JSON" should {
      "produce the expected JSON object" in {
        Json.toJson(RetrieveSavingsFixture.fullForeignInterestsModel) shouldBe mtdResponse
      }
    }
  }
}

