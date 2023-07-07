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

package v1.models.response.retrieveFinancialDetails

import api.hateoas.HateoasFactory
import api.models.hateoas.Method.{DELETE, GET, PUT}
import api.models.hateoas.{HateoasWrapper, Link}
import mocks.MockAppConfig
import play.api.libs.json.{JsError, JsObject, Json}
import support.UnitSpec
import v1.fixtures.RetrieveFinancialDetailsControllerFixture._

class RetrieveEmploymentAndFinancialDetailsResponseSpec extends UnitSpec {

  "RetrieveFinancialDetailsResponse" when {
    "read from valid JSON" should {
      "produce the expected 'RetrieveFinancialDetailsResponse' object" in {
        downstreamJson.as[RetrieveEmploymentAndFinancialDetailsResponse] shouldBe model
      }
    }

    "read from invalid JSON" should {
      "produce a JsError" in {
        JsObject.empty.validate[RetrieveEmploymentAndFinancialDetailsResponse] shouldBe a[JsError]
      }
    }

    "written to JSON" should {
      "produce the expected JsObject" in {
        Json.toJson(model) shouldBe mtdJson
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

    "wrapping a RetrieveFinancialDetailsResponse object" should {
      "expose the correct hateoas links" in new Test {
        hateoasFactory.wrap(model, RetrieveFinancialDetailsHateoasData(nino, taxYear, employmentId)) shouldBe
          HateoasWrapper(
            model,
            Seq(
              Link(s"/individuals/income-received/employments/$nino/$taxYear/$employmentId/financial-details", GET, "self"),
              Link(
                s"/individuals/income-received/employments/$nino/$taxYear/$employmentId/financial-details",
                PUT,
                "create-and-amend-employment-financial-details"),
              Link(
                s"/individuals/income-received/employments/$nino/$taxYear/$employmentId/financial-details",
                DELETE,
                "delete-employment-financial-details")
            )
          )
      }
    }
  }

}
