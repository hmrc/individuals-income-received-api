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

package v1.controllers

import api.controllers.ControllerBaseSpec
import api.hateoas.HateoasLinks
import api.mocks.MockIdGenerator
import api.mocks.hateoas.MockHateoasFactory
import api.mocks.services.{MockAuditService, MockEnrolmentsAuthService, MockMtdIdLookupService, MockNrsProxyService}
import api.models.audit.{AuditError, AuditEvent, AuditResponse}
import api.models.domain.{Nino, TaxYear}
import api.models.errors._
import api.models.outcomes.ResponseWrapper
import mocks.MockAppConfig
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.{AnyContentAsJson, Result}
import uk.gov.hmrc.http.HeaderCarrier
import v1.mocks.requestParsers.MockCreateAmendCgtResidentialPropertyDisposalsRequestParser
import v1.mocks.services._
import v1.models.audit.CreateAmendCgtResidentialPropertyDisposalsAuditDetail
import v1.models.request.createAmendCgtResidentialPropertyDisposals._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class CreateAmendCgtResidentialPropertyDisposalsControllerSpec
    extends ControllerBaseSpec
    with MockEnrolmentsAuthService
    with MockMtdIdLookupService
    with MockAppConfig
    with MockCreateAmendCgtResidentialPropertyDisposalsService
    with MockAuditService
    with MockNrsProxyService
    with MockHateoasFactory
    with MockCreateAmendCgtResidentialPropertyDisposalsRequestParser
    with HateoasLinks
    with MockIdGenerator {

  val nino: String          = "AA123456A"
  val taxYear: String       = "2020-21"
  val correlationId: String = "X-123"

  val validRequestJson: JsValue = Json.parse(
    """
      |{
      |   "disposals":[
      |      {
      |         "customerReference": "CGTDISPOSAL01",
      |         "disposalDate": "2021-03-24",
      |         "completionDate": "2021-03-26",
      |         "disposalProceeds": 1999.99,
      |         "acquisitionDate": "2021-03-22",
      |         "acquisitionAmount": 1999.99,
      |         "improvementCosts": 1999.99,
      |         "additionalCosts": 1999.99,
      |         "prfAmount": 1999.99,
      |         "otherReliefAmount": 1999.99,
      |         "lossesFromThisYear": 1999.99,
      |         "lossesFromPreviousYear": 1999.99,
      |         "amountOfNetGain": 1999.99,
      |         "amountOfNetLoss": 1999.99
      |      }
      |   ]
      |}
     """.stripMargin
  )

  val rawData: CreateAmendCgtResidentialPropertyDisposalsRawData = CreateAmendCgtResidentialPropertyDisposalsRawData(
    nino = nino,
    taxYear = taxYear,
    body = AnyContentAsJson.apply(validRequestJson)
  )

  val requestModel: CreateAmendCgtResidentialPropertyDisposalsRequestBody = CreateAmendCgtResidentialPropertyDisposalsRequestBody(
    disposals = Seq(
      Disposal(
        customerReference = Some("CGTDISPOSAL01"),
        disposalDate = "2021-03-24",
        completionDate = "2021-03-26",
        disposalProceeds = 1999.99,
        acquisitionDate = "2021-03-22",
        acquisitionAmount = 1999.99,
        improvementCosts = Some(1999.99),
        additionalCosts = Some(1999.99),
        prfAmount = Some(1999.99),
        otherReliefAmount = Some(1999.99),
        lossesFromThisYear = Some(1999.99),
        lossesFromPreviousYear = Some(1999.99),
        amountOfNetGain = Some(1999.99),
        amountOfNetLoss = Some(1999.99)
      )
    )
  )

  val requestData: CreateAmendCgtResidentialPropertyDisposalsRequest = CreateAmendCgtResidentialPropertyDisposalsRequest(
    nino = Nino(nino),
    taxYear = TaxYear.fromMtd(taxYear),
    body = requestModel
  )

  val mtdResponse: JsValue = Json.parse(
    s"""
       |{
       |   "links":[
       |      {
       |         "href":"/individuals/income-received/disposals/residential-property/$nino/$taxYear",
       |         "method":"PUT",
       |         "rel":"create-and-amend-cgt-residential-property-disposals"
       |      },
       |      {
       |         "href":"/individuals/income-received/disposals/residential-property/$nino/$taxYear",
       |         "method":"GET",
       |         "rel":"self"
       |      },
       |      {
       |         "href":"/individuals/income-received/disposals/residential-property/$nino/$taxYear",
       |         "method":"DELETE",
       |         "rel":"delete-cgt-residential-property-disposals"
       |      }
       |   ]
       |}
    """.stripMargin
  )

  trait Test {
    val hc: HeaderCarrier = HeaderCarrier()

    val controller = new CreateAmendCgtResidentialPropertyDisposalsController(
      authService = mockEnrolmentsAuthService,
      lookupService = mockMtdIdLookupService,
      appConfig = mockAppConfig,
      requestParser = mockCreateAmendCgtResidentialPropertyDisposalsRequestParser,
      service = mockCreateAmendCgtResidentialPropertyDisposalsService,
      auditService = mockAuditService,
      nrsProxyService = mockNrsProxyService,
      cc = cc,
      idGenerator = mockIdGenerator
    )

    MockedAppConfig.apiGatewayContext.returns("individuals/income-received").anyNumberOfTimes()
    MockedMtdIdLookupService.lookup(nino).returns(Future.successful(Right("test-mtd-id")))
    MockedEnrolmentsAuthService.authoriseUser()
    MockIdGenerator.generateCorrelationId.returns(correlationId)
  }

  def event(auditResponse: AuditResponse): AuditEvent[CreateAmendCgtResidentialPropertyDisposalsAuditDetail] =
    AuditEvent(
      auditType = "CreateAmendCgtResidentialPropertyDisposals",
      transactionName = "Create-Amend-Cgt-Residential-Property-Disposals",
      detail = CreateAmendCgtResidentialPropertyDisposalsAuditDetail(
        userType = "Individual",
        agentReferenceNumber = None,
        nino,
        taxYear,
        validRequestJson,
        correlationId,
        response = auditResponse
      )
    )

  "CreateAmendCgtResidentialPropertyDisposalsController" should {
    "return OK" when {
      "happy path" in new Test {

        MockCreateAmendCgtResidentialPropertyDisposalsRequestParser
          .parse(rawData)
          .returns(Right(requestData))

        MockCreateAmendCgtResidentialPropertyDisposalsService
          .createAndAmend(requestData)
          .returns(Future.successful(Right(ResponseWrapper(correlationId, Unit))))

        MockNrsProxyService.submitAsync(nino, "itsa-cgt-disposal", validRequestJson)

        val result: Future[Result] = controller.createAmendCgtResidentialPropertyDisposals(nino, taxYear)(fakePutRequest(validRequestJson))

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

            MockCreateAmendCgtResidentialPropertyDisposalsRequestParser
              .parse(rawData)
              .returns(Left(ErrorWrapper(correlationId, error, None)))

            val result: Future[Result] = controller.createAmendCgtResidentialPropertyDisposals(nino, taxYear)(fakePutRequest(validRequestJson))

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
          (RuleTaxYearRangeInvalidError, BAD_REQUEST),
          (RuleTaxYearNotSupportedError, BAD_REQUEST),
          (ValueFormatError, BAD_REQUEST),
          (DateFormatError, BAD_REQUEST),
          (CustomerRefFormatError, BAD_REQUEST),
          (RuleCompletionDateBeforeDisposalDateError, BAD_REQUEST),
          (RuleAcquisitionDateAfterDisposalDateError, BAD_REQUEST),
          (RuleCompletionDateError, BAD_REQUEST),
          (RuleDisposalDateError, BAD_REQUEST),
          (RuleIncorrectOrEmptyBodyError, BAD_REQUEST),
          (RuleGainLossError, BAD_REQUEST),
          (RuleLossesGreaterThanGainError, BAD_REQUEST)
        )

        input.foreach(args => (errorsFromParserTester _).tupled(args))
      }

      "service errors occur" must {
        def serviceErrors(mtdError: MtdError, expectedStatus: Int): Unit = {
          s"a $mtdError error is returned from the service" in new Test {

            MockCreateAmendCgtResidentialPropertyDisposalsRequestParser
              .parse(rawData)
              .returns(Right(requestData))

            MockCreateAmendCgtResidentialPropertyDisposalsService
              .createAndAmend(requestData)
              .returns(Future.successful(Left(ErrorWrapper(correlationId, mtdError))))

            MockNrsProxyService.submitAsync(nino, "itsa-cgt-disposal", validRequestJson)

            val result: Future[Result] = controller.createAmendCgtResidentialPropertyDisposals(nino, taxYear)(fakePutRequest(validRequestJson))

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
          (RuleTaxYearNotSupportedError, BAD_REQUEST),
          (StandardDownstreamError, INTERNAL_SERVER_ERROR)
        )

        input.foreach(args => (serviceErrors _).tupled(args))
      }
    }
  }

}
