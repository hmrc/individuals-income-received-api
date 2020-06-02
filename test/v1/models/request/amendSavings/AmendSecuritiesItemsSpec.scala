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
import v1.models.request.savings.amend.AmendSecuritiesItems
import v1.fixtures.AmendSavingsFixture._

class AmendSecuritiesItemsSpec extends UnitSpec {

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

  val mtdResponseInvalid: JsValue = Json.parse(
    """
      |{
      |   "taxTakenOff": "abc",
      |   "grossAmount": 1455.0,
      |   "netAmount": 123.22
      |}
    """.stripMargin
  )

  val mtdResponseEmpty: JsValue = Json.parse("""{}""")

  "AmendSecuritiesItems" when {
    "read from valid JSON" should {
      "convert valid MTD JSON into AmendSecuritiesItems model with all fields" in {
        mtdResponse.as[AmendSecuritiesItems] shouldBe fullAmendSecuritiesItemsModel
      }
    }

    "read from empty JSON" should {
      "convert empty MTD JSON into an empty AmendSecuritiesItems object" in {
        mtdResponseEmpty.as[AmendSecuritiesItems] shouldBe minimalAmendSecuritiesItemsModel
      }
    }

    "read from invalid JSON" should {
      "produce a JsError" in {
        mtdResponseInvalid.validate[AmendSecuritiesItems] shouldBe a[JsError]
      }
    }

    "written to DES JSON" should {
      "convert a AmendSecuritiesItems model into valid DES JSON" in {
        Json.toJson(fullAmendSecuritiesItemsModel) shouldBe desResponse
      }
    }
  }
}
