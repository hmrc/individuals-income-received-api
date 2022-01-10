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

package v1r7.controllers

import mocks.MockAppConfig
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.{AnyContentAsJson, Result}
import uk.gov.hmrc.http.HeaderCarrier
import v1r7.hateoas.HateoasLinks
import v1r7.mocks.MockIdGenerator
import v1r7.mocks.hateoas.MockHateoasFactory
import v1r7.mocks.requestParsers.MockCreateAmendOtherCgtRequestParser
import v1r7.mocks.services.{MockAuditService, MockCreateAmendOtherCgtService, MockEnrolmentsAuthService, MockMtdIdLookupService, MockNrsProxyService}
import v1r7.models.audit.{AuditError, AuditEvent, AuditResponse, CreateAmendOtherCgtAuditDetail}
import v1r7.models.domain.Nino
import v1r7.models.errors._
import v1r7.models.outcomes.ResponseWrapper
import v1r7.models.request.createAmendOtherCgt._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class CreateAmendOtherCgtControllerSpec extends ControllerBaseSpec
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

  val nino: String = "AA123456A"
  val taxYear: String = "2019-20"
  val correlationId: String = "X-123"

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
    disposals = Some(Seq(
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
    taxYear = taxYear,
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

  trait Test {
    val hc: HeaderCarrier = HeaderCarrier()

    val controller = new CreateAmendOtherCgtController(
      authService = mockEnrolmentsAuthService,
      lookupService = mockMtdIdLookupService,
      appConfig = mockAppConfig,
      requestParser = mockCreateAmendOtherCgtRequestParser,
      service = mockCreateAmendOtherCgtService,
      nrsProxyService = mockNrsProxyService,
      auditService = mockAuditService,
      cc = cc,
      idGenerator = mockIdGenerator
    )

    MockedAppConfig.apiGatewayContext.returns("individuals/income-received").anyNumberOfTimes()
    MockedMtdIdLookupService.lookup(nino).returns(Future.successful(Right("test-mtd-id")))
    MockedEnrolmentsAuthService.authoriseUser()
    MockIdGenerator.generateCorrelationId.returns(correlationId)
  }

  def event(auditResponse: AuditResponse): AuditEvent[CreateAmendOtherCgtAuditDetail] =
    AuditEvent(
      auditType = "CreateAmendOtherCgtDisposalsAndGains",
      transactionName = "Create-Amend-Other-Cgt-Disposals-And-Gains",
      detail = CreateAmendOtherCgtAuditDetail(
        userType = "Individual",
        agentReferenceNumber = None,
        nino,
        taxYear,
        validRequestJson,
        correlationId,
        response = auditResponse
      )
    )

  "CreateAmendOtherCgtController" should {
    "return OK" when {
      "happy path" in new Test {

        MockCreateAmendOtherCgtRequestParser
          .parse(rawData)
          .returns(Right(requestData))

        MockCreateAmendOtherCgtService
          .createAmend(requestData)
          .returns(Future.successful(Right(ResponseWrapper(correlationId, Unit))))

        MockNrsProxyService
          .submitAsync(nino, "itsa-cgt-disposal-other", validRequestJson)
          .returns(())

        val result: Future[Result] = controller.createAmendOtherCgt(nino, taxYear)(fakePutRequest(validRequestJson))

        status(result) shouldBe OK
        contentAsJson(result) shouldBe mtdResponse
        header("X-CorrelationId", result) shouldBe Some(correlationId)

        val auditResponse: AuditResponse = AuditResponse(OK, Right(Some(mtdResponse)))
        MockedAuditService.verifyAuditEvent(event(auditResponse)).once
      }
    }

    "return the error as per spec" when {
      "parser errors occur" must {
        def errorsFromParserTester(error: MtdError, expectedStatus: Int): Unit = {
          s"a ${error.code} error is returned from the parser" in new Test {

            MockCreateAmendOtherCgtRequestParser
              .parse(rawData)
              .returns(Left(ErrorWrapper(correlationId, error, None)))

            val result: Future[Result] = controller.createAmendOtherCgt(nino, taxYear)(fakePutRequest(validRequestJson))

            status(result) shouldBe expectedStatus
            contentAsJson(result) shouldBe Json.toJson(error)
            header("X-CorrelationId", result) shouldBe Some(correlationId)

            val auditResponse: AuditResponse = AuditResponse(expectedStatus, Some(Seq(AuditError(error.code))), None)
            MockedAuditService.verifyAuditEvent(event(auditResponse)).once
          }
        }

        val input = Seq(
          (BadRequestError, BAD_REQUEST),
          (NinoFormatError, BAD_REQUEST),
          (TaxYearFormatError, BAD_REQUEST),
          (RuleTaxYearNotSupportedError, BAD_REQUEST),
          (RuleTaxYearRangeInvalidError, BAD_REQUEST),
          (RuleIncorrectOrEmptyBodyError, BAD_REQUEST),
          (RuleGainLossError, BAD_REQUEST),
          (ValueFormatError, BAD_REQUEST),
          (DateFormatError, BAD_REQUEST),
          (AssetDescriptionFormatError, BAD_REQUEST),
          (AssetTypeFormatError, BAD_REQUEST),
          (ClaimOrElectionCodesFormatError, BAD_REQUEST),
          (RuleDisposalDateError, BAD_REQUEST),
          (RuleAcquisitionDateError, BAD_REQUEST),
          (RuleGainAfterReliefLossAfterReliefError, BAD_REQUEST)
        )

        input.foreach(args => (errorsFromParserTester _).tupled(args))
      }

      "service errors occur" must {
        def serviceErrors(mtdError: MtdError, expectedStatus: Int): Unit = {
          s"a $mtdError error is returned from the service" in new Test {

            MockCreateAmendOtherCgtRequestParser
              .parse(rawData)
              .returns(Right(requestData))

            MockCreateAmendOtherCgtService
              .createAmend(requestData)
              .returns(Future.successful(Left(ErrorWrapper(correlationId, mtdError))))

            MockNrsProxyService
              .submitAsync(nino, "itsa-cgt-disposal-other", validRequestJson)
              .returns(())

            val result: Future[Result] = controller.createAmendOtherCgt(nino, taxYear)(fakePutRequest(validRequestJson))

            status(result) shouldBe expectedStatus
            contentAsJson(result) shouldBe Json.toJson(mtdError)
            header("X-CorrelationId", result) shouldBe Some(correlationId)

            val auditResponse: AuditResponse = AuditResponse(expectedStatus, Some(Seq(AuditError(mtdError.code))), None)
            MockedAuditService.verifyAuditEvent(event(auditResponse)).once
          }
        }

        val input = Seq(
          (NinoFormatError, BAD_REQUEST),
          (TaxYearFormatError, BAD_REQUEST),
          (DownstreamError, INTERNAL_SERVER_ERROR)
        )

        input.foreach(args => (serviceErrors _).tupled(args))
      }
    }
  }
}
