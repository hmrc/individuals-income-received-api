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

package v1andv2.models.request.createAmendOther

import play.api.libs.json.{JsError, JsObject, Json}
import support.UnitSpec
import v1andv2.fixtures.other.CreateAmendOtherFixtures._

class AllOtherIncomeReceivedWhilstAbroadItemSpec extends UnitSpec {

  "AllOtherIncomeReceivedWhilstAbroadItem" when {
    "read from valid JSON" should {
      "produce the expected AllOtherIncomeReceivedWhilstAbroadItem object" in {
        allOtherIncomeReceivedWhilstAbroadJson.as[AllOtherIncomeReceivedWhilstAbroadItem] shouldBe allOtherIncomeReceivedWhilstAbroadModel
      }
    }

    "read from empty JSON" should {
      "produce a JsError" in {
        val invalidJson = JsObject.empty

        invalidJson.validate[AllOtherIncomeReceivedWhilstAbroadItem] shouldBe a[JsError]
      }
    }

    "written to JSON" should {
      "produce the expected JsObject" in {
        Json.toJson(allOtherIncomeReceivedWhilstAbroadModel) shouldBe allOtherIncomeReceivedWhilstAbroadJson
      }
    }
  }

}
