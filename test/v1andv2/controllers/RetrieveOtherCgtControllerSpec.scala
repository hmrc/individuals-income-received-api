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

package v1andv2.controllers

import api.controllers.{ControllerBaseSpec, ControllerTestRunner}
import api.hateoas.HateoasLinks
import api.mocks.MockIdGenerator
import api.mocks.hateoas.MockHateoasFactory
import api.mocks.services.{MockEnrolmentsAuthService, MockMtdIdLookupService}
import api.models.domain.{Nino, TaxYear}
import api.models.errors._
import api.models.hateoas.Method.{DELETE, GET, PUT}
import api.models.hateoas.RelType.{CREATE_AND_AMEND_OTHER_CGT_AND_DISPOSALS, DELETE_OTHER_CGT_AND_DISPOSALS, SELF}
import api.models.hateoas.{HateoasWrapper, Link}
import api.models.outcomes.ResponseWrapper
import play.api.libs.json.{JsObject, JsValue, Json}
import play.api.mvc.Result
import v1andv2.mocks.requestParsers.MockRetrieveOtherCgtRequestParser
import v1andv2.mocks.services.MockRetrieveOtherCgtService
import v1andv2.models.request.retrieveOtherCgt.{RetrieveOtherCgtRawData, RetrieveOtherCgtRequest}
import v1andv2.models.response.retrieveOtherCgt._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class RetrieveOtherCgtControllerSpec
    extends ControllerBaseSpec
    with ControllerTestRunner
    with MockEnrolmentsAuthService
    with MockMtdIdLookupService
    with MockRetrieveOtherCgtService
    with MockHateoasFactory
    with MockRetrieveOtherCgtRequestParser
    with HateoasLinks
    with MockIdGenerator {

  val taxYear: String = "2019-20"

  val rawData: RetrieveOtherCgtRawData = RetrieveOtherCgtRawData(
    nino = nino,
    taxYear = taxYear
  )

  val requestData: RetrieveOtherCgtRequest = RetrieveOtherCgtRequest(
    nino = Nino(nino),
    taxYear = TaxYear.fromMtd(taxYear)
  )

  val amendOtherCgtLink: Link =
    Link(
      href = s"/individuals/income-received/disposals/other-gains/$nino/$taxYear",
      method = PUT,
      rel = CREATE_AND_AMEND_OTHER_CGT_AND_DISPOSALS
    )

  val retrieveOtherCgtLink: Link =
    Link(
      href = s"/individuals/income-received/disposals/other-gains/$nino/$taxYear",
      method = GET,
      rel = SELF
    )

  val deleteOtherCgtLink: Link =
    Link(
      href = s"/individuals/income-received/disposals/other-gains/$nino/$taxYear",
      method = DELETE,
      rel = DELETE_OTHER_CGT_AND_DISPOSALS
    )

  val responseModel: RetrieveOtherCgtResponse = RetrieveOtherCgtResponse(
    submittedOn = "2021-05-07T16:18:44.403Z",
    disposals = Some(
      List(
        Disposal(
          assetType = "otherProperty",
          assetDescription = "string",
          acquisitionDate = "2021-05-07",
          disposalDate = "2021-05-07",
          disposalProceeds = 59999999999.99,
          allowableCosts = 59999999999.99,
          gain = Some(59999999999.99),
          loss = None,
          claimOrElectionCodes = Some(List("OTH")),
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

  val mtdResponse: JsObject = validResponseJson.as[JsObject] ++ Json
    .parse(
      s"""
       |{
       |   "links":[
       |      {
       |         "href":"/individuals/income-received/disposals/other-gains/$nino/$taxYear",
       |         "method":"PUT",
       |         "rel":"create-and-amend-other-capital-gains-and-disposals"
       |      },
       |      {
       |         "href":"/individuals/income-received/disposals/other-gains/$nino/$taxYear",
       |         "method":"GET",
       |         "rel":"self"
       |      },
       |      {
       |         "href":"/individuals/income-received/disposals/other-gains/$nino/$taxYear",
       |         "method":"DELETE",
       |         "rel":"delete-other-capital-gains-and-disposals"
       |      }
       |   ]
       |}
    """.stripMargin
    )
    .as[JsObject]

  "RetrieveOtherCgtController" should {
    "return a successful response with status 200 (OK)" when {
      "given a valid request" in new Test {

        MockRetrieveOtherCgtRequestParser
          .parse(rawData)
          .returns(Right(requestData))

        MockRetrieveOtherCgtService
          .retrieve(requestData)
          .returns(Future.successful(Right(ResponseWrapper(correlationId, responseModel))))

        MockHateoasFactory
          .wrap(responseModel, RetrieveOtherCgtHateoasData(nino, taxYear))
          .returns(
            HateoasWrapper(
              responseModel,
              List(
                amendOtherCgtLink,
                retrieveOtherCgtLink,
                deleteOtherCgtLink
              )))

        runOkTest(
          expectedStatus = OK,
          maybeExpectedResponseBody = Some(mtdResponse)
        )
      }
    }

    "return the error as per spec" when {
      "parser validation fails" in new Test {
        MockRetrieveOtherCgtRequestParser
          .parse(rawData)
          .returns(Left(ErrorWrapper(correlationId, NinoFormatError)))

        runErrorTest(NinoFormatError)
      }

      "the service returns an error" in new Test {
        MockRetrieveOtherCgtRequestParser
          .parse(rawData)
          .returns(Right(requestData))

        MockRetrieveOtherCgtService
          .retrieve(requestData)
          .returns(Future.successful(Left(ErrorWrapper(correlationId, RuleTaxYearNotSupportedError))))

        runErrorTest(RuleTaxYearNotSupportedError)
      }
    }
  }

  trait Test extends ControllerTest {

    val controller = new RetrieveOtherCgtController(
      authService = mockEnrolmentsAuthService,
      lookupService = mockMtdIdLookupService,
      parser = mockRetrieveOtherCgtRequestParser,
      service = mockRetrieveOtherCgtService,
      hateoasFactory = mockHateoasFactory,
      cc = cc,
      idGenerator = mockIdGenerator
    )

    override protected def callController(): Future[Result] = controller.retrieveOtherCgt(nino, taxYear)(fakeGetRequest)
  }

}
