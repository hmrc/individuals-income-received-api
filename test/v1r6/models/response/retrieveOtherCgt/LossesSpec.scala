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

package v1r6.models.response.retrieveOtherCgt

import play.api.libs.json.{JsError, JsObject, JsValue, Json}
import support.UnitSpec

class LossesSpec extends UnitSpec {

  val validResponseJson: JsValue = Json.parse(
    """
      |{
      |   "broughtForwardLossesUsedInCurrentYear":29999999999.99,
      |   "setAgainstInYearGains":29999999999.99,
      |   "setAgainstInYearGeneralIncome":29999999999.99,
      |   "setAgainstEarlierYear":29999999999.99
      |}
     """.stripMargin
  )

  val minimumValidResponseJson: JsValue = JsObject.empty

  val invalidJson: JsValue = Json.parse(
    """
      |{
      |   "broughtForwardLossesUsedInCurrentYear":true
      |}
     """.stripMargin
  )

  val responseModel: Losses = Losses(
    broughtForwardLossesUsedInCurrentYear = Some(29999999999.99),
    setAgainstInYearGains = Some(29999999999.99),
    setAgainstInYearGeneralIncome = Some(29999999999.99),
    setAgainstEarlierYear = Some(29999999999.99)
  )

  val minimumResponseModel: Losses = Losses(
    broughtForwardLossesUsedInCurrentYear = None,
    setAgainstInYearGains = None,
    setAgainstInYearGeneralIncome = None,
    setAgainstEarlierYear = None
  )

  "Losses" when {
    "read from valid JSON" should {
      "produce the expected response model" in {
        validResponseJson.as[Losses] shouldBe responseModel
      }
    }

    "read from the minimum valid JSON" should {
      "produce the expected response model" in {
        minimumValidResponseJson.as[Losses] shouldBe minimumResponseModel
      }
    }

    "read from invalid JSON" should {
      "produce a JsError" in {
        invalidJson.validate[RetrieveOtherCgtResponse] shouldBe a[JsError]
      }
    }

    "written to JSON" should {
      "produce the expected JSON" in {
        Json.toJson(responseModel) shouldBe validResponseJson
      }
    }
  }
}
