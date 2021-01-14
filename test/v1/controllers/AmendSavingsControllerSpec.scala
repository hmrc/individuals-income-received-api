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

package v1.controllers

import mocks.MockAppConfig
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.{AnyContentAsJson, Result}
import uk.gov.hmrc.domain.Nino
import uk.gov.hmrc.http.HeaderCarrier
import v1.mocks.MockIdGenerator
import v1.mocks.requestParsers.MockAmendSavingsRequestParser
import v1.mocks.services.{MockAmendSavingsService, MockAuditService, MockEnrolmentsAuthService, MockMtdIdLookupService}
import v1.models.audit.{AuditError, AuditEvent, AuditResponse, GenericAuditDetail}
import v1.models.errors._
import v1.models.outcomes.ResponseWrapper
import v1.models.request.amendSavings._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class AmendSavingsControllerSpec
  extends ControllerBaseSpec
    with MockEnrolmentsAuthService
    with MockMtdIdLookupService
    with MockAppConfig
    with MockAmendSavingsService
    with MockAuditService
    with MockAmendSavingsRequestParser
    with MockIdGenerator {

  val nino: String = "AA123456A"
  val taxYear: String = "2019-20"
  val correlationId: String = "X-123"

  def event(auditRequest: Option[JsValue], auditResponse: AuditResponse): AuditEvent[GenericAuditDetail] =
    AuditEvent(
      auditType = "CreateAmendSavingsIncome",
      transactionName = "create-amend-savings-income",
      detail = GenericAuditDetail(
        userType = "Individual",
        agentReferenceNumber = None,
        params = Map("nino" -> nino, "taxYear" -> taxYear),
        request = auditRequest,
        `X-CorrelationId` = correlationId,
        response = auditResponse
      )
    )

  trait Test {
    val hc: HeaderCarrier = HeaderCarrier()

    val controller = new AmendSavingsController(
      authService = mockEnrolmentsAuthService,
      lookupService = mockMtdIdLookupService,
      appConfig = mockAppConfig,
      requestParser = mockAmendSavingsRequestParser,
      service = mockAmendSavingsService,
      auditService = mockAuditService,
      cc = cc,
      idGenerator = mockIdGenerator
    )

    MockedMtdIdLookupService.lookup(nino).returns(Future.successful(Right("test-mtd-id")))
    MockedEnrolmentsAuthService.authoriseUser()
    MockedAppConfig.apiGatewayContext.returns("baseUrl").anyNumberOfTimes()
    MockIdGenerator.generateCorrelationId.returns(correlationId)
  }

  val requestBodyJson: JsValue = Json.parse(
    """
      |{
      |  "securities":
      |      {
      |        "taxTakenOff": 100.11,
      |        "grossAmount": 100.22,
      |        "netAmount": 100.33
      |      },
      |  "foreignInterest":   [
      |     {
      |        "amountBeforeTax": 101.11,
      |        "countryCode": "FRA",
      |        "taxTakenOff": 102.22,
      |        "specialWithholdingTax": 103.33,
      |        "taxableAmount": 104.44,
      |        "foreignTaxCreditRelief": true
      |      },
      |      {
      |        "amountBeforeTax": 201.11,
      |        "countryCode": "DEU",
      |        "taxTakenOff": 202.22,
      |        "specialWithholdingTax": 203.33,
      |        "taxableAmount": 204.44,
      |        "foreignTaxCreditRelief": true
      |      }
      |   ]
      |}
    """.stripMargin
  )

  val rawData: AmendSavingsRawData = AmendSavingsRawData(
    nino = nino,
    taxYear = taxYear,
    body = AnyContentAsJson(requestBodyJson)
  )

  val security: AmendSecurities = AmendSecurities(
    taxTakenOff = Some(100.11),
    grossAmount = 200.22,
    netAmount = Some(300.33)
  )

  val foreignInterests: Seq[AmendForeignInterestItem] = Seq(
    AmendForeignInterestItem(
      amountBeforeTax = Some(101.11),
      countryCode = "FRA",
      taxTakenOff = Some(102.22),
      specialWithholdingTax = Some(103.33),
      taxableAmount = 104.44,
      foreignTaxCreditRelief = false
    ),
    AmendForeignInterestItem(
      amountBeforeTax = Some(201.11),
      countryCode = "GER",
      taxTakenOff = Some(202.22),
      specialWithholdingTax = Some(203.33),
      taxableAmount = 204.44,
      foreignTaxCreditRelief = false
    )
  )

  val amendSavingsRequestBody: AmendSavingsRequestBody = AmendSavingsRequestBody(
    securities = Some(security),
    foreignInterest = Some(foreignInterests)
  )

  val requestData: AmendSavingsRequest = AmendSavingsRequest(
    nino = Nino(nino),
    taxYear = taxYear,
    body = amendSavingsRequestBody
  )

  val hateoasResponse: JsValue = Json.parse(
    s"""
      |{
      |   "links":[
      |      {
      |         "href":"/baseUrl/savings/$nino/$taxYear",
      |         "rel":"create-and-amend-savings-income",
      |         "method":"PUT"
      |      },
      |      {
      |         "href":"/baseUrl/savings/$nino/$taxYear",
      |         "rel":"self",
      |         "method":"GET"
      |      },
      |      {
      |         "href":"/baseUrl/savings/$nino/$taxYear",
      |         "rel":"delete-savings-income",
      |         "method":"DELETE"
      |      }
      |   ]
      |}
    """.stripMargin
  )

  "AmendSavingsController" should {
    "return OK" when {
      "happy path" in new Test {

        MockAmendSavingsRequestParser
          .parse(rawData)
          .returns(Right(requestData))

        MockAmendSavingsService
          .amendSaving(requestData)
          .returns(Future.successful(Right(ResponseWrapper(correlationId, ()))))

        val result: Future[Result] = controller.amendSaving(nino, taxYear)(fakePutRequest(requestBodyJson))

        status(result) shouldBe OK
        contentAsJson(result) shouldBe hateoasResponse
        header("X-CorrelationId", result) shouldBe Some(correlationId)

        val auditResponse: AuditResponse = AuditResponse(OK, None, Some(hateoasResponse))
        MockedAuditService.verifyAuditEvent(event(Some(requestBodyJson), auditResponse)).once
      }
    }

    "return the error as per spec" when {
      "parser errors occur" must {
        def errorsFromParserTester(error: MtdError, expectedStatus: Int): Unit = {
          s"a ${error.code} error is returned from the parser" in new Test {

            MockAmendSavingsRequestParser
              .parse(rawData)
              .returns(Left(ErrorWrapper(correlationId, error, None)))

            val result: Future[Result] = controller.amendSaving(nino, taxYear)(fakePutRequest(requestBodyJson))

            status(result) shouldBe expectedStatus
            contentAsJson(result) shouldBe Json.toJson(error)
            header("X-CorrelationId", result) shouldBe Some(correlationId)

            val auditResponse: AuditResponse = AuditResponse(expectedStatus, Some(Seq(AuditError(error.code))), None)
            MockedAuditService.verifyAuditEvent(event(Some(requestBodyJson), auditResponse)).once
          }
        }

        val input = Seq(
          (BadRequestError, BAD_REQUEST),
          (NinoFormatError, BAD_REQUEST),
          (TaxYearFormatError, BAD_REQUEST),
          (RuleTaxYearRangeInvalidError, BAD_REQUEST),
          (RuleTaxYearNotSupportedError, BAD_REQUEST),
          (RuleIncorrectOrEmptyBodyError, BAD_REQUEST),
          (CountryCodeFormatError, BAD_REQUEST),
          (CountryCodeRuleError, BAD_REQUEST),
          (ValueFormatError, BAD_REQUEST)
        )

        input.foreach(args => (errorsFromParserTester _).tupled(args))
      }

      "service errors occur" must {
        def serviceErrors(mtdError: MtdError, expectedStatus: Int): Unit = {
          s"a $mtdError error is returned from the service" in new Test {

            MockAmendSavingsRequestParser
              .parse(rawData)
              .returns(Right(requestData))

            MockAmendSavingsService
              .amendSaving(requestData)
              .returns(Future.successful(Left(ErrorWrapper(correlationId, mtdError))))

            val result: Future[Result] = controller.amendSaving(nino, taxYear)(fakePutRequest(requestBodyJson))

            status(result) shouldBe expectedStatus
            contentAsJson(result) shouldBe Json.toJson(mtdError)
            header("X-CorrelationId", result) shouldBe Some(correlationId)

            val auditResponse: AuditResponse = AuditResponse(expectedStatus, Some(Seq(AuditError(mtdError.code))), None)
            MockedAuditService.verifyAuditEvent(event(Some(requestBodyJson), auditResponse)).once
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