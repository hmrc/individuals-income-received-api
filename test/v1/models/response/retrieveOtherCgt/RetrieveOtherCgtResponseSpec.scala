/*
 * Copyright 2022 HM Revenue & Customs
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

package v1.models.response.retrieveOtherCgt

import mocks.MockAppConfig
import play.api.libs.json.{JsError, JsObject, JsValue, Json}
import support.UnitSpec
import api.hateoas.HateoasFactory
import api.models.hateoas.{HateoasWrapper, Link}
import api.models.hateoas.Method.{DELETE, GET, PUT}

class RetrieveOtherCgtResponseSpec extends UnitSpec {

  val validResponseJson: JsValue = Json.parse(
    """
      |{
      |   "submittedOn":"2021-05-07T16:18:44.403Z",
      |   "disposals":[
      |      {
      |         "assetType":"otherProperty",
      |         "assetDescription":"string",
      |         "acquisitionDate":"2021-05-07",
      |         "disposalDate":"2021-05-07",
      |         "disposalProceeds":59999999999.99,
      |         "allowableCosts":59999999999.99,
      |         "gain":59999999999.99,
      |         "claimOrElectionCodes":[
      |            "OTH"
      |         ],
      |         "gainAfterRelief":59999999999.99,
      |         "rttTaxPaid":59999999999.99
      |      }
      |   ],
      |   "nonStandardGains":{
      |      "carriedInterestGain":19999999999.99,
      |      "carriedInterestRttTaxPaid":19999999999.99,
      |      "attributedGains":19999999999.99,
      |      "attributedGainsRttTaxPaid":19999999999.99,
      |      "otherGains":19999999999.99,
      |      "otherGainsRttTaxPaid":19999999999.99
      |   },
      |   "losses":{
      |      "broughtForwardLossesUsedInCurrentYear":29999999999.99,
      |      "setAgainstInYearGains":29999999999.99,
      |      "setAgainstInYearGeneralIncome":29999999999.99,
      |      "setAgainstEarlierYear":29999999999.99
      |   },
      |   "adjustments":-39999999999.99
      |}
     """.stripMargin
  )

  val minimumValidResponseJson: JsValue = Json.parse(
    """
      |{
      |   "submittedOn":"2021-05-07T16:18:44.403Z",
      |   "disposals":[
      |      {
      |         "assetType":"otherProperty",
      |         "assetDescription":"string",
      |         "acquisitionDate":"2021-05-07",
      |         "disposalDate":"2021-05-07",
      |         "disposalProceeds":59999999999.99,
      |         "allowableCosts":59999999999.99
      |      }
      |   ]
      |}
     """.stripMargin
  )

  val invalidJson: JsValue = JsObject.empty

  val responseModel: RetrieveOtherCgtResponse = RetrieveOtherCgtResponse(
    submittedOn = "2021-05-07T16:18:44.403Z",
    disposals = Some(
      Seq(
        Disposal(
          assetType = "otherProperty",
          assetDescription = "string",
          acquisitionDate = "2021-05-07",
          disposalDate = "2021-05-07",
          disposalProceeds = 59999999999.99,
          allowableCosts = 59999999999.99,
          gain = Some(59999999999.99),
          loss = None,
          claimOrElectionCodes = Some(Seq("OTH")),
          gainAfterRelief = Some(59999999999.99),
          lossAfterRelief = None,
          rttTaxPaid = Some(59999999999.99)
        )
      )),
    nonStandardGains = Some(
      NonStandardGains(
        carriedInterestGain = Some(19999999999.99),
        carriedInterestRttTaxPaid = Some(19999999999.99),
        attributedGains = Some(19999999999.99),
        attributedGainsRttTaxPaid = Some(19999999999.99),
        otherGains = Some(19999999999.99),
        otherGainsRttTaxPaid = Some(19999999999.99)
      )
    ),
    losses = Some(
      Losses(
        broughtForwardLossesUsedInCurrentYear = Some(29999999999.99),
        setAgainstInYearGains = Some(29999999999.99),
        setAgainstInYearGeneralIncome = Some(29999999999.99),
        setAgainstEarlierYear = Some(29999999999.99)
      )
    ),
    adjustments = Some(-39999999999.99)
  )

  val minimumResponseModel: RetrieveOtherCgtResponse = RetrieveOtherCgtResponse(
    submittedOn = "2021-05-07T16:18:44.403Z",
    disposals = Some(
      Seq(
        Disposal(
          assetType = "otherProperty",
          assetDescription = "string",
          acquisitionDate = "2021-05-07",
          disposalDate = "2021-05-07",
          disposalProceeds = 59999999999.99,
          allowableCosts = 59999999999.99,
          gain = None,
          loss = None,
          claimOrElectionCodes = None,
          gainAfterRelief = None,
          lossAfterRelief = None,
          rttTaxPaid = None
        )
      )),
    nonStandardGains = None,
    losses = None,
    adjustments = None
  )

  "RetrieveOtherCgtResponse" when {
    "read from valid JSON" should {
      "produce the expected response model" in {
        validResponseJson.as[RetrieveOtherCgtResponse] shouldBe responseModel
      }
    }

    "read from the minimum valid JSON" should {
      "produce the expected response model" in {
        minimumValidResponseJson.as[RetrieveOtherCgtResponse] shouldBe minimumResponseModel
      }
    }

    "read from invalid JSON" should {
      "produce a JsError" in {
        invalidJson.validate[RetrieveOtherCgtResponse] shouldBe a[JsError]
      }
    }

    "written to JSON" should {
      "produce the expected JSON" in {
        Json.toJson(responseModel) shouldBe validResponseJson
      }
    }
  }

  "RetrieveOtherCgtLinksFactory" when {
    class Test extends MockAppConfig {
      val hateoasFactory  = new HateoasFactory(mockAppConfig)
      val nino            = "AA111111A"
      val taxYear: String = "2020-21"
      MockedAppConfig.apiGatewayContext.returns("individuals/income-received").anyNumberOfTimes
    }

    "wrapping a response model" should {
      "expose the correct links" in new Test {
        hateoasFactory.wrap(responseModel, RetrieveOtherCgtHateoasData(nino, taxYear)) shouldBe
          HateoasWrapper(
            responseModel,
            Seq(
              Link(s"/individuals/income-received/disposals/other-gains/$nino/$taxYear", PUT, "create-and-amend-other-capital-gains-and-disposals"),
              Link(s"/individuals/income-received/disposals/other-gains/$nino/$taxYear", DELETE, "delete-other-capital-gains-and-disposals"),
              Link(s"/individuals/income-received/disposals/other-gains/$nino/$taxYear", GET, "self")
            )
          )
      }
    }
  }

}
