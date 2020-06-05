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

package v1.models.request.insurancePolicies.amend

import play.api.libs.json.Json
import support.UnitSpec
import v1.fixtures.insurancePolicies.AmendInsurancePoliciesFixture._

class VoidedIsaSpec extends UnitSpec {

  "VoidedIsa" should {
    "process Json correctly" when {
      "a full valid request is sent" in {
        fullVoidedIsaJson.as[VoidedIsa] shouldBe fullVoidedIsaModel
      }

      "a minimal valid request is sent" in {
        minVoidedIsaJson.as[VoidedIsa] shouldBe minVoidedIsaModel
      }
    }

    "write json correctly" when {
      "a full valid model is provided" in {
        Json.toJson(fullVoidedIsaModel) shouldBe fullVoidedIsaJson
      }

      "a  minimal valid model is provided" in {
        Json.toJson(minVoidedIsaModel) shouldBe minVoidedIsaJson
      }
    }
  }
}
