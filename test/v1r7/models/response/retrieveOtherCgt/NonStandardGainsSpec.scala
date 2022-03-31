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

package v1r7.models.response.retrieveOtherCgt

import play.api.libs.json.{JsError, JsObject, JsValue, Json}
import support.UnitSpec

class NonStandardGainsSpec extends UnitSpec {

  val validResponseJson: JsValue = Json.parse(
    """
      |{
      |   "carriedInterestGain":19999999999.99,
      |   "carriedInterestRttTaxPaid":19999999999.99,
      |   "attributedGains":19999999999.99,
      |   "attributedGainsRttTaxPaid":19999999999.99,
      |   "otherGains":19999999999.99,
      |   "otherGainsRttTaxPaid":19999999999.99
      |}
     """.stripMargin
  )

  val minimumValidResponseJson: JsValue = JsObject.empty

  val invalidJson: JsValue = Json.parse(
    """
      |{
      |   "carriedInterestGain":true
      |}
     """.stripMargin
  )

  val responseModel: NonStandardGains = NonStandardGains(
    carriedInterestGain = Some(19999999999.99),
    carriedInterestRttTaxPaid = Some(19999999999.99),
    attributedGains = Some(19999999999.99),
    attributedGainsRttTaxPaid = Some(19999999999.99),
    otherGains = Some(19999999999.99),
    otherGainsRttTaxPaid = Some(19999999999.99)
  )

  val minimumResponseModel: NonStandardGains = NonStandardGains(
    carriedInterestGain = None,
    carriedInterestRttTaxPaid = None,
    attributedGains = None,
    attributedGainsRttTaxPaid = None,
    otherGains = None,
    otherGainsRttTaxPaid = None
  )

  "NonStandardGains" when {
    "read from valid JSON" should {
      "produce the expected response model" in {
        validResponseJson.as[NonStandardGains] shouldBe responseModel
      }
    }

    "read from the minimum valid JSON" should {
      "produce the expected response model" in {
        minimumValidResponseJson.as[NonStandardGains] shouldBe minimumResponseModel
      }
    }

    "read from invalid JSON" should {
      "produce a JsError" in {
        invalidJson.validate[NonStandardGains] shouldBe a[JsError]
      }
    }

    "written to JSON" should {
      "produce the expected JSON" in {
        Json.toJson(responseModel) shouldBe validResponseJson
      }
    }
  }

}
