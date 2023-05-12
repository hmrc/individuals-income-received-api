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

package v1.models.response.addCustomEmployment

import api.hateoas.HateoasFactory
import api.models.hateoas.Method.{DELETE, GET, PUT}
import api.models.hateoas.{HateoasWrapper, Link}
import mocks.MockAppConfig
import play.api.libs.json.{JsError, JsObject, JsValue, Json}
import support.UnitSpec

class AddCustomEmploymentResponseSpec extends UnitSpec {

  val model: AddCustomEmploymentResponse = AddCustomEmploymentResponse("anId")

  "AddCustomEmploymentResponse" when {
    val json: JsValue = Json.parse("""{ "employmentId": "anId" }""")

    "read from valid JSON" should {
      "produce the expected AddCustomEmploymentResponse object" in {
        json.as[AddCustomEmploymentResponse] shouldBe model
      }
    }

    "read from invalid JSON" should {
      "produce a JsError" in {
        val json: JsValue = JsObject.empty

        json.validate[AddCustomEmploymentResponse] shouldBe a[JsError]
      }
    }

    "written to JSON" should {
      "produce the expected JsObject" in {
        Json.toJson(model) shouldBe json
      }
    }
  }

  "LinksFactory" when {
    class Test extends MockAppConfig {
      val hateoasFactory = new HateoasFactory(mockAppConfig)
      val nino           = "someNino"
      val taxYear        = "2017-18"
      val employmentId   = "anId"
      MockedAppConfig.apiGatewayContext.returns("individuals/income-received").anyNumberOfTimes()
    }

    "wrapping a AddCustomEmploymentResponse object" should {
      "expose the correct hateoas links" in new Test {
        hateoasFactory.wrap(model, AddCustomEmploymentHateoasData(nino, taxYear, employmentId)) shouldBe
          HateoasWrapper(
            model,
            Seq(
              Link(s"/individuals/income-received/employments/$nino/$taxYear", GET, "list-employments"),
              Link(s"/individuals/income-received/employments/$nino/$taxYear/$employmentId", GET, "self"),
              Link(s"/individuals/income-received/employments/$nino/$taxYear/$employmentId", PUT, "amend-custom-employment"),
              Link(s"/individuals/income-received/employments/$nino/$taxYear/$employmentId", DELETE, "delete-custom-employment")
            )
          )
      }
    }
  }

}
