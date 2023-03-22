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

package v1.controllers

import api.controllers.{ControllerBaseSpec, ControllerTestRunner}
import api.hateoas.HateoasLinks
import api.mocks.MockIdGenerator
import api.mocks.hateoas.MockHateoasFactory
import api.mocks.services.{MockAuditService, MockEnrolmentsAuthService, MockMtdIdLookupService, MockNrsProxyService}
import api.models.audit.{AuditEvent, AuditResponse}
import api.models.domain.{Nino, TaxYear}
import api.models.errors._
import api.models.hateoas.Method.{DELETE, GET, PUT}
import api.models.hateoas.{HateoasWrapper, Link}
import api.models.outcomes.ResponseWrapper
import mocks.MockAppConfig
import play.api.Configuration
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.{AnyContentAsJson, Result}
import v1.mocks.requestParsers.MockCreateAmendOtherCgtRequestParser
import v1.mocks.services._
import v1.models.audit.CreateAmendOtherCgtAuditDetail
import v1.models.request.createAmendOtherCgt._
import v1.models.response.createAmendOtherCgt.CreateAmendOtherCgtHateoasData

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class CreateAmendOtherCgtControllerSpec
    extends ControllerBaseSpec
    with ControllerTestRunner
    with MockEnrolmentsAuthService
    with MockMtdIdLookupService
    with MockAppConfig
    with MockCreateAmendOtherCgtService
    with MockNrsProxyService
    with MockAuditService
    with MockHateoasFactory
    with MockCreateAmendOtherCgtRequestParser
    with HateoasLinks
    with MockIdGenerator {

  val taxYear: String = "2019-20"

  val validRequestJson: JsValue = Json.parse(
    """
      |{
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

  val rawData: CreateAmendOtherCgtRawData = CreateAmendOtherCgtRawData(
    nino = nino,
    taxYear = taxYear,
    body = AnyContentAsJson.apply(validRequestJson)
  )

  val requestModel: CreateAmendOtherCgtRequestBody = CreateAmendOtherCgtRequestBody(
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

  val requestData: CreateAmendOtherCgtRequest = CreateAmendOtherCgtRequest(
    nino = Nino(nino),
    taxYear = TaxYear.fromMtd(taxYear),
    body = requestModel
  )

  val mtdResponse: JsValue = Json.parse(
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

  val hateoasLinks: List[Link] = List(
    Link(
      href = s"/individuals/income-received/disposals/other-gains/$nino/$taxYear",
      method = PUT,
      rel = "create-and-amend-other-capital-gains-and-disposals"
    ),
    Link(
      href = s"/individuals/income-received/disposals/other-gains/$nino/$taxYear",
      method = GET,
      rel = "self"
    ),
    Link(
      href = s"/individuals/income-received/disposals/other-gains/$nino/$taxYear",
      method = DELETE,
      rel = "delete-other-capital-gains-and-disposals"
    )
  )

  val auditData: JsValue = Json.parse(s"""
                                         |{
                                         |  "nino":"$nino",
                                         |  "taxYear": "$taxYear"
                                         |  }""".stripMargin)

  "CreateAmendOtherCgtController" should {
    "return OK" when {
      "happy path" in new Test {
        MockedAppConfig.apiGatewayContext.returns("individuals/income-received").anyNumberOfTimes()

        MockCreateAmendOtherCgtRequestParser
          .parse(rawData)
          .returns(Right(requestData))

        MockNrsProxyService
          .submitAsync(nino, "itsa-cgt-disposal-other", validRequestJson)
          .returns(())

        MockCreateAmendOtherCgtService
          .createAmend(requestData)
          .returns(Future.successful(Right(ResponseWrapper(correlationId, ()))))

        MockHateoasFactory
          .wrap((), CreateAmendOtherCgtHateoasData(nino, taxYear))
          .returns(HateoasWrapper((), hateoasLinks))

        runOkTestWithAudit(expectedStatus = OK, Some(mtdResponse), Some(validRequestJson), Some(auditData))
      }
    }

    "return the error as per spec" when {
      "the parser validation fails" in new Test {
        MockCreateAmendOtherCgtRequestParser
          .parse(rawData)
          .returns(Left(ErrorWrapper(correlationId, NinoFormatError)))

        runErrorTestWithAudit(NinoFormatError, maybeAuditRequestBody = Some(validRequestJson))
      }

      "service returns an error" in new Test {
        MockCreateAmendOtherCgtRequestParser
          .parse(rawData)
          .returns(Right(requestData))

        MockNrsProxyService
          .submitAsync(nino, "itsa-cgt-disposal-other", validRequestJson)
          .returns(())

        MockCreateAmendOtherCgtService
          .createAmend(requestData)
          .returns(Future.successful(Left(ErrorWrapper(correlationId, RuleTaxYearNotSupportedError))))

        runErrorTestWithAudit(RuleTaxYearNotSupportedError, maybeAuditRequestBody = Some(validRequestJson))
      }
    }
  }

  trait Test extends ControllerTest with AuditEventChecking[CreateAmendOtherCgtAuditDetail] {

    val controller = new CreateAmendOtherCgtController(
      authService = mockEnrolmentsAuthService,
      lookupService = mockMtdIdLookupService,
      appConfig = mockAppConfig,
      parser = mockCreateAmendOtherCgtRequestParser,
      service = mockCreateAmendOtherCgtService,
      nrsProxyService = mockNrsProxyService,
      hateoasFactory = mockHateoasFactory,
      auditService = mockAuditService,
      cc = cc,
      idGenerator = mockIdGenerator
    )

    protected def callController(): Future[Result] = controller.createAmendOtherCgt(nino, taxYear)(fakePutRequest(validRequestJson))

    def event(auditResponse: AuditResponse, requestBody: Option[JsValue]): AuditEvent[CreateAmendOtherCgtAuditDetail] =
      AuditEvent(
        auditType = "CreateAmendOtherCgtDisposalsAndGains",
        transactionName = "Create-Amend-Other-Cgt-Disposals-And-Gains",
        detail = CreateAmendOtherCgtAuditDetail(
          userType = "Individual",
          agentReferenceNumber = None,
          nino,
          taxYear,
          requestBody.getOrElse(Json.parse("""{}""")),
          correlationId,
          response = auditResponse
        )
      )

    MockedAppConfig.featureSwitches.returns(Configuration("allowTemporalValidationSuspension.enabled" -> true)).anyNumberOfTimes()
  }

}
