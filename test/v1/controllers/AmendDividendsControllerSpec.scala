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
import v1.mocks.requestParsers.MockAmendDividendsRequestParser
import v1.mocks.services.{MockAmendDividendsService, MockAuditService, MockEnrolmentsAuthService, MockMtdIdLookupService}
import v1.models.audit.{AuditError, AuditEvent, AuditResponse, GenericAuditDetail}
import v1.models.errors._
import v1.models.outcomes.ResponseWrapper
import v1.models.request.amendDividends._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class AmendDividendsControllerSpec
  extends ControllerBaseSpec
    with MockEnrolmentsAuthService
    with MockMtdIdLookupService
    with MockAppConfig
    with MockAuditService
    with MockAmendDividendsService
    with MockAmendDividendsRequestParser
    with MockIdGenerator {

  val nino: String = "AA123456A"
  val taxYear: String = "2019-20"
  val correlationId: String = "a1e8057e-fbbc-47a8-a8b478d9f015c253"

  trait Test {
    val hc: HeaderCarrier = HeaderCarrier()

    val controller = new AmendDividendsController(
      authService = mockEnrolmentsAuthService,
      lookupService = mockMtdIdLookupService,
      appConfig = mockAppConfig,
      requestParser = mockAmendDividendsRequestParser,
      service = mockAmendDividendsService,
      auditService = mockAuditService,
      cc = cc,
      idGenerator = mockIdGenerator
    )

    MockedMtdIdLookupService.lookup(nino).returns(Future.successful(Right("test-mtd-id")))
    MockedEnrolmentsAuthService.authoriseUser()
    MockedAppConfig.apiGatewayContext.returns("individuals/income-received").anyNumberOfTimes()
    MockIdGenerator.generateCorrelationId.returns(correlationId)
  }

  val requestBodyJson: JsValue = Json.parse(
    """
      |{
      |   "foreignDividend": [
      |      {
      |        "countryCode": "DEU",
      |        "amountBeforeTax": 1232.22,
      |        "taxTakenOff": 22.22,
      |        "specialWithholdingTax": 27.35,
      |        "foreignTaxCreditRelief": true,
      |        "taxableAmount": 2321.22
      |      },
      |      {
      |        "countryCode": "FRA",
      |        "amountBeforeTax": 1350.55,
      |        "taxTakenOff": 25.27,
      |        "specialWithholdingTax": 30.59,
      |        "foreignTaxCreditRelief": false,
      |        "taxableAmount": 2500.99
      |      }
      |   ],
      |   "dividendIncomeReceivedWhilstAbroad": [
      |      {
      |        "countryCode": "DEU",
      |        "amountBeforeTax": 1232.22,
      |        "taxTakenOff": 22.22,
      |        "specialWithholdingTax": 27.35,
      |        "foreignTaxCreditRelief": true,
      |        "taxableAmount": 2321.22
      |      },
      |      {
      |        "countryCode": "FRA",
      |        "amountBeforeTax": 1350.55,
      |        "taxTakenOff": 25.27,
      |        "specialWithholdingTax": 30.59,
      |        "foreignTaxCreditRelief": false,
      |        "taxableAmount": 2500.99
      |       }
      |   ],
      |   "stockDividend": {
      |      "customerReference": "my divs",
      |      "grossAmount": 12321.22
      |   },
      |   "redeemableShares": {
      |      "customerReference": "my shares",
      |      "grossAmount": 12345.75
      |   },
      |   "bonusIssuesOfSecurities": {
      |      "customerReference": "my secs",
      |      "grossAmount": 12500.89
      |   },
      |   "closeCompanyLoansWrittenOff": {
      |      "customerReference": "write off",
      |      "grossAmount": 13700.55
      |   }
      |}
    """.stripMargin
  )

  val rawData: AmendDividendsRawData = AmendDividendsRawData(
    nino = nino,
    taxYear = taxYear,
    body = AnyContentAsJson(requestBodyJson)
  )

  val foreignDividend: Seq[AmendForeignDividendItem] = Seq(
    AmendForeignDividendItem(
      countryCode = "DEU",
      amountBeforeTax = Some(1232.22),
      taxTakenOff = Some(22.22),
      specialWithholdingTax = Some(27.35),
      foreignTaxCreditRelief = true,
      taxableAmount = 2321.22
    ),
    AmendForeignDividendItem(
      countryCode = "FRA",
      amountBeforeTax = Some(1350.55),
      taxTakenOff = Some(25.27),
      specialWithholdingTax = Some(30.59),
      foreignTaxCreditRelief = false,
      taxableAmount = 2500.99
    )
  )

  val dividendIncomeReceivedWhilstAbroad: Seq[AmendDividendIncomeReceivedWhilstAbroadItem] = Seq(
    AmendDividendIncomeReceivedWhilstAbroadItem(
      countryCode = "DEU",
      amountBeforeTax = Some(1232.22),
      taxTakenOff = Some(22.22),
      specialWithholdingTax = Some(27.35),
      foreignTaxCreditRelief = true,
      taxableAmount = 2321.22
    ),
    AmendDividendIncomeReceivedWhilstAbroadItem(
      countryCode = "FRA",
      amountBeforeTax = Some(1350.55),
      taxTakenOff = Some(25.27),
      specialWithholdingTax = Some(30.59),
      foreignTaxCreditRelief = false,
      taxableAmount = 2500.99
    )
  )

  val stockDividend: AmendCommonDividends = AmendCommonDividends(
    customerReference = Some ("my divs"),
    grossAmount = 12321.22
  )

  val redeemableShares: AmendCommonDividends = AmendCommonDividends(
    customerReference = Some ("my shares"),
    grossAmount = 12345.75
  )

  val bonusIssuesOfSecurities: AmendCommonDividends = AmendCommonDividends(
    customerReference = Some ("my secs"),
    grossAmount = 12500.89
  )

  val closeCompanyLoansWrittenOff: AmendCommonDividends = AmendCommonDividends(
    customerReference = Some ("write off"),
    grossAmount = 13700.55
  )

  val amendDividendsRequestBody: AmendDividendsRequestBody = AmendDividendsRequestBody(
    foreignDividend = Some(foreignDividend),
    dividendIncomeReceivedWhilstAbroad = Some(dividendIncomeReceivedWhilstAbroad),
    stockDividend = Some(stockDividend),
    redeemableShares = Some(redeemableShares),
    bonusIssuesOfSecurities = Some(bonusIssuesOfSecurities),
    closeCompanyLoansWrittenOff = Some(closeCompanyLoansWrittenOff)
  )

  val requestData: AmendDividendsRequest = AmendDividendsRequest(
    nino = Nino(nino),
    taxYear = taxYear,
    body = amendDividendsRequestBody
  )

  val hateoasResponse: JsValue = Json.parse(
    s"""
      |{
      |   "links":[
      |      {
      |         "href":"/individuals/income-received/dividends/$nino/$taxYear",
      |         "rel":"create-and-amend-dividends-income",
      |         "method":"PUT"
      |      },
      |      {
      |         "href":"/individuals/income-received/dividends/$nino/$taxYear",
      |         "rel":"self",
      |         "method":"GET"
      |      },
      |      {
      |         "href":"/individuals/income-received/dividends/$nino/$taxYear",
      |         "rel":"delete-dividends-income",
      |         "method":"DELETE"
      |      }
      |   ]
      |}
    """.stripMargin
  )

  def event(auditRequest: Option[JsValue], auditResponse: AuditResponse): AuditEvent[GenericAuditDetail] =
    AuditEvent(
      auditType = "CreateAmendDividendsIncome",
      transactionName = "create-amend-dividends-income",
      detail = GenericAuditDetail(
        userType = "Individual",
        agentReferenceNumber = None,
        params = Map("nino" -> nino, "taxYear" -> taxYear),
        request = auditRequest,
        `X-CorrelationId` = correlationId,
        response = auditResponse
      )
    )

  "AmendDividendsController" should {
    "return OK" when {
      "happy path" in new Test {

        MockAmendDividendsRequestParser
          .parse(rawData)
          .returns(Right(requestData))

        MockAmendDividendsService
          .amendDividends(requestData)
          .returns(Future.successful(Right(ResponseWrapper(correlationId, ()))))

        val result: Future[Result] = controller.amendDividends(nino, taxYear)(fakePutRequest(requestBodyJson))

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

            MockAmendDividendsRequestParser
              .parse(rawData)
              .returns(Left(ErrorWrapper(correlationId, error, None)))

            val result: Future[Result] = controller.amendDividends(nino, taxYear)(fakePutRequest(requestBodyJson))

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
          (ValueFormatError, BAD_REQUEST),
          (CustomerRefFormatError, BAD_REQUEST)
        )

        input.foreach(args => (errorsFromParserTester _).tupled(args))
      }

      "service errors occur" must {
        def serviceErrors(mtdError: MtdError, expectedStatus: Int): Unit = {
          s"a $mtdError error is returned from the service" in new Test {

            MockAmendDividendsRequestParser
              .parse(rawData)
              .returns(Right(requestData))

            MockAmendDividendsService
              .amendDividends(requestData)
              .returns(Future.successful(Left(ErrorWrapper(correlationId, mtdError))))

            val result: Future[Result] = controller.amendDividends(nino, taxYear)(fakePutRequest(requestBodyJson))

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