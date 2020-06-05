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
import v1.models.response.savings.retrieveSavings.SecuritiesItems

class SecuritiesItemsSpec extends UnitSpec {

  val desResponse: JsValue = Json.parse(
    """
      |{
      |   "taxTakenOff": 100.0,
      |   "grossAmount": 1455.0,
      |   "netAmount": 123.22
      |}
    """.stripMargin
  )

  val mtdResponse: JsValue = Json.parse(
    """
      |{
      |   "taxTakenOff": 100.0,
      |   "grossAmount": 1455.0,
      |   "netAmount": 123.22
      |}
    """.stripMargin
  )

  val desResponseInvalid: JsValue = Json.parse(
    """
      |{
      |   "taxTakenOff": "abc",
      |   "grossAmount": 1455.0,
      |   "netAmount": 123.22
      |}
    """.stripMargin
  )

  val desResponseEmpty: JsValue = Json.parse("""{}""")

  "SecuritiesItems" when {
    "read from valid JSON" should {
      "produce the expected SecuritiesItems object" in {
        desResponse.as[SecuritiesItems] shouldBe RetrieveSavingsFixture.fullSecuritiesItemsModel
      }
    }

    "read from empty JSON" should {
      "produce an empty SecuritiesItems object" in {
        desResponseEmpty.as[SecuritiesItems] shouldBe RetrieveSavingsFixture.minimalSecuritiesItemsModel
      }
    }

    "read from invalid JSON" should {
      "produce a JsError" in {
        desResponseInvalid.validate[SecuritiesItems] shouldBe a[JsError]
      }
    }

    "written to JSON" should {
      "produce the expected JSON object" in {
        Json.toJson(RetrieveSavingsFixture.fullSecuritiesItemsModel) shouldBe mtdResponse
      }
    }
  }
}