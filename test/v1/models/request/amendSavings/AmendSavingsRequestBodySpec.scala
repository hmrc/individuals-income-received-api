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
import v1.fixtures.AmendSavingsFixture._
import v1.models.request.savings.amend.AmendSavingsRequestBody

class AmendSavingsRequestBodySpec extends UnitSpec {

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
      |         "taxableAmount": 2321.22,
      |         "foreignTaxCreditRelief": true
      |      }
      |   ]
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
      |         "taxableAmount": 2321.22,
      |         "foreignTaxCreditRelief": true
      |      }
      |   ]
      |}
    """.stripMargin
  )

  val desMinimalFieldsRetrieveSavingsResponse: JsValue = Json.parse(
    """
      |{
      |   "foreignInterest": [
      |      {
      |         "countryCode": "GER",
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
      |         "taxableAmount": 2321.22,
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

  val desResponseEmpty: JsValue = Json.parse("""{}""")

  val mtdResponseEmpty: JsValue = Json.parse("""{}""")

  val mtdResponseInvalid: JsValue = Json.parse(
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
    "read from valid MTD JSON" should {
      "convert valid MTD JSON into AmendSavingsRequestBody model with all fields" in {
        mtdResponse.as[AmendSavingsRequestBody] shouldBe amendSavingsRequestModel
      }
    }

    "read from valid MTD JSON with no securities" should {
      "convert MTD JSON into a model with securities as None" in {
        mtdResponseNoSecurities.as[AmendSavingsRequestBody] shouldBe requestModelNoSecurities
      }
    }

    "read from valid MTD JSON with no foreignInterest" should {
      "convert MTD JSON into a model with foreignInterest as None" in {
        mtdResponseNoForeignInterest.as[AmendSavingsRequestBody] shouldBe requestModelNoForeignInterest
      }
    }

    "read from valid MTD JSON with only AmendForeignInterest mandatory fields" should {
      "convert MTD JSON into a model with foreignInterest having only mandatory fields" in {
        mtdResponseMinimalForeignInterest.as[AmendSavingsRequestBody] shouldBe requestModelMinimalForeignInterest
      }
    }

    "read from invalid JSON" should {
      "produce a JsError" in {
        mtdResponseInvalid.validate[AmendSavingsRequestBody] shouldBe a[JsError]
      }
    }

    "read from empty MTD JSON" should {
      "convert MTD JSON into a model with securities and foreignInterest as None" in {
        mtdResponseEmpty.as[AmendSavingsRequestBody] shouldBe minimalAmendSavingsRequestModel
      }
    }

    "read from a MTD JSON containing only mandatory fields" should {
      "convert MTD JSON into a model with securities and foreignInterest having only mandatory fields" in {
        mtdMinimalFieldsRetrieveSavingsResponse.as[AmendSavingsRequestBody] shouldBe requestModelWithMinimalFields
      }
    }

    "read from a MTD JSON with multiple foreignInterest items " should {
      "convert MTD JSON into a model a model with multiple foreignInterest items" in {
        mtdResponseWithMultipleForeignInterest.as[AmendSavingsRequestBody] shouldBe requestModelMultipleForeignInterest
      }
    }

    "written to DES JSON" should {
      "convert a AmendSavingsRequestBody model into valid DES JSON" in {
        Json.toJson(amendSavingsRequestModel) shouldBe desResponse
      }
    }

    "written to DES JSON (no securities)" should {
      "convert model into DES JSON with no securities" in {
        Json.toJson(requestModelNoSecurities) shouldBe desResponseNoSecurities
      }
    }

    "written to DES JSON (mandatory fields in foreignInterest)" should {
      "convert model into DES JSON with mandatory fields in foreignInterest" in {
        Json.toJson(requestModelMinimalForeignInterest) shouldBe desResponseMinimalForeignInterest
      }
    }

    "written to DES JSON (only mandatory fields)" should {
      "convert model into DES JSON with only mandatory fields" in {
        Json.toJson(requestModelWithMinimalFields) shouldBe desMinimalFieldsRetrieveSavingsResponse
      }
    }

    "written to DES JSON (multiple foreignInterest items)" should {
      "convert model into DES JSON with multiple foreignInterest items" in {
        Json.toJson(requestModelMultipleForeignInterest) shouldBe desResponseWithMultipleForeignInterest
      }
    }

    "written to DES JSON (no securities and foreignInterest)" should {
      "convert model into DES JSON with no securities and foreignInterest" in {
        Json.toJson(minimalAmendSavingsRequestModel) shouldBe desResponseEmpty
      }
    }

    "written to DES JSON (no foreignInterest)" should {
      "convert model into DES JSON with no foreignInterest" in {
        Json.toJson(requestModelNoForeignInterest) shouldBe desResponseNoForeignInterest
      }
    }
  }
}
