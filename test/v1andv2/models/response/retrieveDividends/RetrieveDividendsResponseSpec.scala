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

package v1andv2.models.response.retrieveDividends

import api.hateoas.HateoasFactory
import api.models.hateoas.Method.{DELETE, GET, PUT}
import api.models.hateoas.{HateoasWrapper, Link}
import mocks.MockAppConfig
import play.api.libs.json.{JsError, Json}
import support.UnitSpec
import v1andv2.fixtures.RetrieveDividendsFixtures._

class RetrieveDividendsResponseSpec extends UnitSpec {

  "RetrieveDividendsResponse" when {
    "read from valid JSON" should {
      "produce the expected RetrieveDividendsResponse object" in {
        responseJson.as[RetrieveDividendsResponse] shouldBe responseModel
      }
    }

    "read from invalid JSON" should {
      "produce a JsError" in {
        val invalidJson = Json.parse(
          """
            |{
            |   "foreignDividend": [
            |      {
            |        "countryCode": true,
            |        "amountBeforeTax": 1232.22,
            |        "taxTakenOff": 22.22,
            |        "specialWitholdingTax": 22.22,
            |        "foreignTaxCreditRelief": true,
            |        "taxableAmount": 2321.22
            |      }
            |    ]
            |}
          """.stripMargin
        )
        invalidJson.validate[RetrieveDividendsResponse] shouldBe a[JsError]
      }
    }
    "written to JSON" should {
      "produce the expected JsObject" in {
        Json.toJson(responseModel) shouldBe responseJson
      }
    }
  }

  "LinksFactory" when {
    class Test extends MockAppConfig {
      val hateoasFactory = new HateoasFactory(mockAppConfig)
      val nino           = "someNino"
      val taxYear        = "2019-20"
      MockedAppConfig.apiGatewayContext.returns("individuals/income-received").anyNumberOfTimes()    }

    "wrapping a RetrieveForeignResponse object" should {
      "expose the correct hateoas links" in new Test {
        hateoasFactory.wrap(responseModel, RetrieveDividendsHateoasData(nino, taxYear)) shouldBe
          HateoasWrapper(
            responseModel,
            Seq(
              Link(s"/individuals/income-received/dividends/$nino/$taxYear", PUT, "create-and-amend-dividends-income"),
              Link(s"/individuals/income-received/dividends/$nino/$taxYear", GET, "self"),
              Link(s"/individuals/income-received/dividends/$nino/$taxYear", DELETE, "delete-dividends-income")
            )
          )
      }
    }
  }

}
