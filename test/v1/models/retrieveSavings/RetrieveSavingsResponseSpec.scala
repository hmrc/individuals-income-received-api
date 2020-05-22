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
import v1.fixtures.RetrieveSavingsFixture._

class RetrieveSavingsResponseSpec extends UnitSpec {

  val desResponse: JsValue = Json.parse(
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

  val mtdResponse: JsValue = Json.parse(
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

  val desResponseNoSecurities: JsValue = Json.parse(
    """
      |{
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

  val desResponseNoForeignInterest: JsValue = Json.parse(
    """
      |{
      |   "securities":
      |      {
      |         "taxTakenOff": 100.0,
      |         "grossAmount": 1455.0,
      |         "netAmount": 123.22
      |      }
      |}
    """.stripMargin
  )

  val desResponseMinimalForeignInterest: JsValue = Json.parse(
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
      |         "countryCode": "GER",
      |         "foreignTaxCreditRelief": true
      |      }
      |   ]
      |}
    """.stripMargin
  )


  val desResponseEmptySecurities: JsValue = Json.parse(
    """
      |{
      |   "securities": {  },
      |
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

  val desResponseEmptyForeignInterest: JsValue = Json.parse(
    """
      |{
      |   "securities":
      |      {
      |         "taxTakenOff": 100.0,
      |         "grossAmount": 1455.0,
      |         "netAmount": 123.22
      |      },
      |   "foreignInterest": [ ]
      |}
    """.stripMargin
  )

  val desMinimalFieldsRetrieveSavingsResponse: JsValue = Json.parse(
    """
      |{
      |   "foreignInterest": [
      |      {
      |         "countryCode": "GER",
      |         "foreignTaxCreditRelief": true
      |      }
      |   ]
      |}
    """.stripMargin
  )

  val desResponseWithMultipleForeignInterest: JsValue = Json.parse(
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
      |      },
      |      {
      |         "amountBeforeTax": 1232.22,
      |         "countryCode": "FRA",
      |         "taxTakenOff": 22.22,
      |         "specialWithholdingTax": 22.22,
      |         "taxableAmount": 2321.22,
      |         "foreignTaxCreditRelief": true
      |      }
      |   ]
      |}
    """.stripMargin
  )

  val mtdResponseWithMultipleForeignInterest: JsValue = Json.parse(
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
      |      },
      |      {
      |         "amountBeforeTax": 1232.22,
      |         "countryCode": "FRA",
      |         "taxTakenOff": 22.22,
      |         "specialWithholdingTax": 22.22,
      |         "taxableAmount": 2321.22,
      |         "foreignTaxCreditRelief": true
      |      }
      |   ]
      |}
    """.stripMargin
  )

  val mtdMinimalFieldsRetrieveSavingsResponse: JsValue = Json.parse(
    """
      |{
      |   "foreignInterest": [
      |      {
      |         "countryCode": "GER",
      |         "foreignTaxCreditRelief": true
      |      }
      |   ]
      |}
    """.stripMargin
  )

  val mtdResponseNoSecurities: JsValue = Json.parse(
    """
      |{
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

  val mtdResponseNoForeignInterest: JsValue = Json.parse(
    """
      |{
      |   "securities":
      |      {
      |         "taxTakenOff": 100.0,
      |         "grossAmount": 1455.0,
      |         "netAmount": 123.22
      |      }
      |}
    """.stripMargin
  )

  val mtdResponseMinimalForeignInterest: JsValue = Json.parse(
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
      |         "countryCode": "GER",
      |         "foreignTaxCreditRelief": true
      |      }
      |   ]
      |}
    """.stripMargin
  )

  val desResponseEmpty: JsValue = Json.parse("""{}""")

  val mtdResponseEmpty: JsValue = Json.parse("""{}""")

  val desResponseInvalid: JsValue = Json.parse(
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

  "RetrieveSavingsResponse" when {
    "read from valid JSON" should {
      "produce the expected RetrieveSavingsResponse object" in {
        desResponse.as[RetrieveSavingsResponse] shouldBe retrieveSavingsResponseModel
      }
    }

    "read from valid JSON with no securities" should {
      "produce the expected RetrieveSavingsResponse object" in {
        desResponseNoSecurities.as[RetrieveSavingsResponse] shouldBe responseModelNoSecurities
      }
    }
    "read from valid JSON with empty securities" should {
      "produce the expected RetrieveSavingsResponse object" in {
        desResponseEmptySecurities.as[RetrieveSavingsResponse] shouldBe  responseModelNoSecurities
      }
    }

    "read from valid JSON with empty ForeignInterest" should {
      "produce the expected RetrieveSavingsResponse object" in {
        desResponseEmptyForeignInterest.as[RetrieveSavingsResponse] shouldBe  responseModelNoForeignInterest
      }
    }

    "read from valid JSON with no foreignInterest" should {
      "produce the expected RetrieveSavingsResponse object" in {
        desResponseNoForeignInterest.as[RetrieveSavingsResponse] shouldBe responseModelNoForeignInterest
      }
    }

    "read from valid JSON with only mandatory fields in ForeignInterest" should {
      "produce the expected RetrieveSavingsResponse object" in {
        desResponseMinimalForeignInterest.as[RetrieveSavingsResponse] shouldBe responseModelMinimalForeignInterest
      }
    }

    "read from invalid JSON" should {
      "produce a JsError" in {
        desResponseInvalid.validate[RetrieveSavingsResponse] shouldBe a[JsError]
      }
    }

    "read from empty JSON" should {
      "produce the expected RetrieveSavingsResponse object" in {
        desResponseEmpty.as[RetrieveSavingsResponse] shouldBe minimalRetrieveSavingsResponseModel
      }
    }

    "read from a JSON containing only mandatory fields" should {
      "produce the expected RetrieveSavingsResponse object" in {
        desMinimalFieldsRetrieveSavingsResponse.as[RetrieveSavingsResponse] shouldBe responseModelWithMinimalFields
      }
    }

    "read from a JSON with multiple foreignInterest items " should {
      "produce the expected RetrieveSavingsResponse object" in {
        desResponseWithMultipleForeignInterest.as[RetrieveSavingsResponse] shouldBe responseModelMultipleForeignInterest
      }
    }

    "written to JSON" should {
      "produce the expected JSON" in {
        Json.toJson(retrieveSavingsResponseModel) shouldBe mtdResponse
      }
    }

    "written to JSON (no securities)" should {
      "produce the expected JSON" in {
        Json.toJson(responseModelNoSecurities) shouldBe mtdResponseNoSecurities
      }
    }

    "written to JSON (mandatory fields in foreignInterest)" should {
      "produce the expected JSON" in {
        Json.toJson(responseModelMinimalForeignInterest) shouldBe mtdResponseMinimalForeignInterest
      }
    }

    "written to JSON (only mandatory fields)" should {
      "produce the expected JSON" in {
        Json.toJson(responseModelWithMinimalFields) shouldBe mtdMinimalFieldsRetrieveSavingsResponse
      }
    }

    "written to JSON (multiple foreignInterest items)" should {
      "produce the expected JSON" in {
        Json.toJson(responseModelMultipleForeignInterest) shouldBe mtdResponseWithMultipleForeignInterest
      }
    }

    "written to JSON (no securities and foreignInterest)" should {
      "produce the expected JSON" in {
        Json.toJson(minimalRetrieveSavingsResponseModel) shouldBe mtdResponseEmpty
      }
    }

    "written to JSON (no foreignInterest)" should {
      "produce the expected JSON" in {
        Json.toJson(responseModelNoForeignInterest) shouldBe mtdResponseNoForeignInterest
      }
    }
  }
}
