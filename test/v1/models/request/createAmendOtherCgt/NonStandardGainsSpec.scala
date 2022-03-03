/*
 * Copyright 2022 HM Revenue & Customs
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

package v1.models.request.createAmendOtherCgt

import play.api.libs.json.{JsError, JsObject, JsValue, Json}
import support.UnitSpec

class NonStandardGainsSpec extends UnitSpec {

  val mtdJson: JsValue = Json.parse(
    """
      |{
      |  "carriedInterestGain": 101.99,
      |  "carriedInterestRttTaxPaid": 102.99,
      |  "attributedGains": 103.99,
      |  "attributedGainsRttTaxPaid": 104.99,
      |  "otherGains": 105.99,
      |  "otherGainsRttTaxPaid": 106.99
      |}
      |""".stripMargin
  )

  val mtdRequestBody: NonStandardGains = NonStandardGains(
    Some(101.99),
    Some(102.99),
    Some(103.99),
    Some(104.99),
    Some(105.99),
    Some(106.99)
  )

  val desJson: JsValue = Json.parse(
    """
      |{
      |  "carriedInterestGain":101.99,
      |  "carriedInterestRttTaxPaid":102.99,
      |  "attributedGains":103.99,
      |  "attributedGainsRttTaxPaid":104.99,
      |  "otherGains":105.99,
      |  "otherGainsRttTaxPaid":106.99
      |}
      |""".stripMargin
  )

  val emptyJson: JsValue = JsObject.empty

  val invalidJson: JsValue = Json.parse(
    """
      |{
      |  "carriedInterestGain": "fghj",
      |  "carriedInterestRttTaxPaid": 102.99,
      |  "attributedGains": 103.99,
      |  "attributedGainsRttTaxPaid": 104.99,
      |  "otherGains": 105.99,
      |  "otherGainsRttTaxPaid": 106.99
      |}
      |""".stripMargin
  )

  "NonStandardGains" when {
    "read from a valid JSON" should {
      "produce the expected object" in {
        mtdJson.as[NonStandardGains] shouldBe mtdRequestBody
      }
    }

    "read from an empty JSON" should {
      "produce an empty object" in {
        emptyJson.as[NonStandardGains] shouldBe NonStandardGains.empty
      }
    }

    "read from an invalid JSON" should {
      "produce a JsError" in {
        invalidJson.validate[NonStandardGains] shouldBe a[JsError]
      }
    }

    "written JSON" should {
      "produce the expected JsObject" in {
        Json.toJson(mtdRequestBody) shouldBe desJson
      }
    }

    "written from an empty object" should {
      "produce an empty JSON" in {
        Json.toJson(NonStandardGains.empty) shouldBe emptyJson
      }
    }
  }
}
