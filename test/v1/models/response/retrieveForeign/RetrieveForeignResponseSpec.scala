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

package v1.models.response.retrieveForeign

import mocks.MockAppConfig
import play.api.libs.json.{JsError, Json}
import support.UnitSpec
import v1.hateoas.HateoasFactory
import v1.models.hateoas.Method.{DELETE, GET, PUT}
import v1.models.hateoas.{HateoasWrapper, Link}

class RetrieveForeignResponseSpec extends UnitSpec {

  private val fullRetrieveForeignResponseJson = Json.parse(
    """
      |{
      |   "submittedOn": "2019-04-04T01:01:01Z",
      |   "foreignEarnings": {
      |     "customerReference": "FOREIGNINCME123A",
      |     "earningsNotTaxableUK": 1999.99
      |   },
      |   "unremittableForeignIncome": [
      |     {
      |       "countryCode": "FRA",
      |       "amountInForeignCurrency": 1999.99,
      |       "amountTaxPaid": 1999.99
      |     },
      |     {
      |       "countryCode": "IND",
      |       "amountInForeignCurrency": 2999.99,
      |       "amountTaxPaid": 2999.99
      |     }
      |   ]
      |}
    """.stripMargin
  )

  val fullForeignEarningsModel: ForeignEarnings = ForeignEarnings(
    customerReference = Some("FOREIGNINCME123A"),
    earningsNotTaxableUK = 1999.99
  )

  val fullUnremittableForeignIncomeModel1: UnremittableForeignIncome = UnremittableForeignIncome(
    countryCode =  "FRA",
    amountInForeignCurrency = 1999.99,
    amountTaxPaid = Some(1999.99)
  )

  val fullUnremittableForeignIncomeModel2: UnremittableForeignIncome = UnremittableForeignIncome(
    countryCode =  "IND",
    amountInForeignCurrency = 2999.99,
    amountTaxPaid = Some(2999.99)
  )

  private val fullRetrieveResponseBodyModel = RetrieveForeignResponse(
    submittedOn = "2019-04-04T01:01:01Z",
    foreignEarnings = Some(fullForeignEarningsModel),
    unremittableForeignIncome = Some(Seq(
      fullUnremittableForeignIncomeModel1,
      fullUnremittableForeignIncomeModel2
    ))
  )

  private val minRetrieveResponseBodyModel = RetrieveForeignResponse("2019-04-04T01:01:01Z", None, None)

  "RetrieveForeignResponse" when {
    "read from valid JSON" should {
      "produce the expected RetrieveForeignResponseBody model" in {
        fullRetrieveForeignResponseJson.as[RetrieveForeignResponse] shouldBe fullRetrieveResponseBodyModel
      }
    }

    "read from empty JSON" should {
      "produce a model with 'foreignEarnings' and 'unremittableForeignIncome' as None" in {
        val emptyJson = Json.parse("""{"submittedOn": "2019-04-04T01:01:01Z"}""")

        emptyJson.as[RetrieveForeignResponse] shouldBe minRetrieveResponseBodyModel
      }
    }

    "read from invalid JSON" should {
      "produce a JsError" in {
        val invalidJson = Json.parse(
          """
            |{
            |   "foreignEarnings": {
            |     "customerReference": false,
            |     "earningsNotTaxableUK": 1999.99
            |   }
            |}
          """.stripMargin
        )

        invalidJson.validate[RetrieveForeignResponse] shouldBe a[JsError]
      }
    }

    "written to JSON" should {
      "produce the expected JSON object" in {
        Json.toJson(fullRetrieveResponseBodyModel) shouldBe fullRetrieveForeignResponseJson
      }
    }
  }

  "LinksFactory" when {
    class Test extends MockAppConfig {
      val hateoasFactory = new HateoasFactory(mockAppConfig)
      val nino = "someNino"
      val taxYear = "2019-20"
      MockedAppConfig.apiGatewayContext.returns("individuals/income-received").anyNumberOfTimes
    }

    "wrapping a RetrieveForeignResponse object" should {
      "expose the correct hateoas links" in new Test {
        hateoasFactory.wrap(fullRetrieveResponseBodyModel, RetrieveForeignHateoasData(nino, taxYear)) shouldBe
          HateoasWrapper(
            fullRetrieveResponseBodyModel,
            Seq(
              Link(s"/individuals/income-received/foreign/$nino/$taxYear", GET, "self"),
              Link(s"/individuals/income-received/foreign/$nino/$taxYear", PUT, "create-and-amend-foreign-income"),
              Link(s"/individuals/income-received/foreign/$nino/$taxYear", DELETE, "delete-foreign-income")
            )
          )
      }
    }
  }
}
