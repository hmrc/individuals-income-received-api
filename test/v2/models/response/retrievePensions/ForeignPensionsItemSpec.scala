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

package v2.models.response.retrievePensions

import play.api.libs.json.{JsObject, JsError, Json}
import support.UnitSpec

class ForeignPensionsItemSpec extends UnitSpec {

  private val jsonWithOptionalFields = Json.parse(
    """
      |{
      |   "countryCode": "DEU",
      |   "amountBeforeTax": 100.23,
      |   "taxTakenOff": 1.23,
      |   "specialWithholdingTax": 2.23,
      |   "foreignTaxCreditRelief": false,
      |   "taxableAmount": 3.23
      |}
    """.stripMargin
  )

  private val jsonWithoutOptionalFields = Json.parse(
    """
      |{
      |   "countryCode": "DEU",
      |   "taxableAmount": 3.23
      |}
    """.stripMargin
  )

  private val modelWithOptionalFields = ForeignPensionsItem(
    countryCode = "DEU",
    amountBeforeTax = Some(100.23),
    taxTakenOff = Some(1.23),
    specialWithholdingTax = Some(2.23),
    foreignTaxCreditRelief = Some(false),
    taxableAmount = 3.23
  )

  private val modelWithoutOptionalFields = ForeignPensionsItem(
    countryCode = "DEU",
    amountBeforeTax = None,
    taxTakenOff = None,
    specialWithholdingTax = None,
    foreignTaxCreditRelief = None,
    taxableAmount = 3.23
  )

  "ForeignPensionsItem" when {
    "read from valid JSON with optional fields" should {
      "produce the expected ForeignPensionsItem object" in {
        jsonWithOptionalFields.as[ForeignPensionsItem] shouldBe modelWithOptionalFields
      }
    }

    "read from valid JSON without optional fields" should {
      "produce the expected ForeignPensionsItem object" in {
        jsonWithoutOptionalFields.as[ForeignPensionsItem] shouldBe modelWithoutOptionalFields
      }
    }

    "read from empty JSON" should {
      "produce a JsError" in {
        val invalidJson = JsObject.empty

        invalidJson.validate[ForeignPensionsItem] shouldBe a[JsError]
      }
    }

    "written to JSON with optional fields" should {
      "produce the expected JsObject" in {
        Json.toJson(modelWithOptionalFields) shouldBe jsonWithOptionalFields
      }
    }

    "written to JSON without optional fields" should {
      "produce the expected JsObject" in {
        Json.toJson(modelWithoutOptionalFields) shouldBe jsonWithoutOptionalFields
      }
    }
  }

}
