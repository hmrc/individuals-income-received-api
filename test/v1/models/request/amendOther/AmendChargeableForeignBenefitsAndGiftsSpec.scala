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

package v1.models.request.amendOther

import play.api.libs.json.{JsError, JsObject, Json}
import support.UnitSpec

class AmendChargeableForeignBenefitsAndGiftsSpec extends UnitSpec {

  private val json = Json.parse(
    """
      |{
      |   "transactionBenefit": 1999.99,
      |   "protectedForeignIncomeSourceBenefit": 2999.99,
      |   "protectedForeignIncomeOnwardGift": 3999.99,
      |   "benefitReceivedAsASettler": 4999.99,
      |   "onwardGiftReceivedAsASettler": 5999.99
      |}
    """.stripMargin
  )

  private val model = AmendChargeableForeignBenefitsAndGifts(
    transactionBenefit = Some(1999.99),
    protectedForeignIncomeSourceBenefit = Some(2999.99),
    protectedForeignIncomeOnwardGift = Some(3999.99),
    benefitReceivedAsASettler = Some(4999.99),
    onwardGiftReceivedAsASettler = Some(5999.99)
  )

  "AmendChargeableForeignBenefitsAndGifts" when {
    "read from valid JSON" should {
      "produce the expected AmendChargeableForeignBenefitsAndGifts object" in {
        json.as[AmendChargeableForeignBenefitsAndGifts] shouldBe model
      }
    }

    "read from empty JSON" should {
      "produce an empty AmendChargeableForeignBenefitsAndGifts object" in {
        val emptyJson = JsObject.empty

        emptyJson.as[AmendChargeableForeignBenefitsAndGifts] shouldBe AmendChargeableForeignBenefitsAndGifts.empty
      }
    }

    "read from invalid JSON" should {
      "produce a JsError" in {
        val invalidJson = Json.parse(
          """
            |{
            |   "transactionBenefit": true,
            |   "protectedForeignIncomeSourceBenefit": 2999.99,
            |   "protectedForeignIncomeOnwardGift": 3999.99,
            |   "benefitReceivedAsASettler": 4999.99,
            |   "onwardGiftReceivedAsASettler": 5999.99
            |}
          """.stripMargin
        )

        invalidJson.validate[AmendChargeableForeignBenefitsAndGifts] shouldBe a[JsError]
      }
    }

    "written to JSON" should {
      "produce the expected JsObject" in {
        Json.toJson(model) shouldBe json
      }
    }
  }
}
