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

package v1.models.request.createAmendNonPayeEmployment

import play.api.libs.json.Json
import support.UnitSpec

class CreateAmendNonPayeEmploymentRequestBodySpec extends UnitSpec {

  private val json = Json.obj("tips" -> 1000.12)

  private val model = CreateAmendNonPayeEmploymentRequestBody(1000.12)

  "reads" should {
    "turn JSON into a model" in {
      json.as[CreateAmendNonPayeEmploymentRequestBody] shouldBe model
    }
  }

  "writes" should {
    "turn a model into JSON" in {
      Json.toJson(model) shouldBe json
    }
  }

}
