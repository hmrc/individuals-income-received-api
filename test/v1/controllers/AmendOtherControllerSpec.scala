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
import v1.mocks.requestParsers.MockAmendOtherRequestParser
import v1.mocks.services.{MockAmendOtherService, MockAuditService, MockEnrolmentsAuthService, MockMtdIdLookupService}
import v1.models.audit.{AuditError, AuditEvent, AuditResponse, GenericAuditDetail}
import v1.models.errors._
import v1.models.outcomes.ResponseWrapper
import v1.models.request.amendOther._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class AmendOtherControllerSpec
  extends ControllerBaseSpec
    with MockEnrolmentsAuthService
    with MockMtdIdLookupService
    with MockAppConfig
    with MockAmendOtherRequestParser
    with MockAuditService
    with MockAmendOtherService
    with MockIdGenerator {

  val nino: String = "AA123456A"
  val taxYear: String = "2019-20"
  val correlationId: String = "a1e8057e-fbbc-47a8-a8b4-78d9f015c253"

  trait Test {
    val hc: HeaderCarrier = HeaderCarrier()

    val controller = new AmendOtherController(
      authService = mockEnrolmentsAuthService,
      lookupService = mockMtdIdLookupService,
      appConfig = mockAppConfig,
      requestParser = mockAmendOtherRequestParser,
      service = mockAmendOtherService,
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
      |   "businessReceipts": [
      |      {
      |         "grossAmount": 5000.99,
      |         "taxYear": "2018-19"
      |      },
      |      {
      |         "grossAmount": 6000.99,
      |         "taxYear": "2019-20"
      |      }
      |   ],
      |   "allOtherIncomeReceivedWhilstAbroad": [
      |      {
      |         "countryCode": "FRA",
      |         "amountBeforeTax": 1999.99,
      |         "taxTakenOff": 2.23,
      |         "specialWithholdingTax": 3.23,
      |         "foreignTaxCreditRelief": false,
      |         "taxableAmount": 4.23,
      |         "residentialFinancialCostAmount": 2999.99,
      |         "broughtFwdResidentialFinancialCostAmount": 1999.99
      |      },
      |      {
      |         "countryCode": "IND",
      |         "amountBeforeTax": 2999.99,
      |         "taxTakenOff": 3.23,
      |         "specialWithholdingTax": 4.23,
      |         "foreignTaxCreditRelief": true,
      |         "taxableAmount": 5.23,
      |         "residentialFinancialCostAmount": 3999.99,
      |         "broughtFwdResidentialFinancialCostAmount": 2999.99
      |      }
      |   ],
      |   "overseasIncomeAndGains": {
      |      "gainAmount": 3000.99
      |   },
      |   "chargeableForeignBenefitsAndGifts": {
      |      "transactionBenefit": 1999.99,
      |      "protectedForeignIncomeSourceBenefit": 2999.99,
      |      "protectedForeignIncomeOnwardGift": 3999.99,
      |      "benefitReceivedAsASettler": 4999.99,
      |      "onwardGiftReceivedAsASettler": 5999.99
      |   },
      |   "omittedForeignIncome": {
      |      "amount": 4000.99
      |   }
      |}
    """.stripMargin
  )

  val rawData: AmendOtherRawData = AmendOtherRawData(
    nino = nino,
    taxYear = taxYear,
    body = AnyContentAsJson(requestBodyJson)
  )

  val businessReceipts: Seq[AmendBusinessReceiptsItem] = Seq(
    AmendBusinessReceiptsItem(
      grossAmount = 5000.99,
      taxYear = "2018-19"
    ),
    AmendBusinessReceiptsItem(
      grossAmount = 6000.99,
      taxYear = "2019-20"
    )
  )

  val allOtherIncomeReceivedWhilstAbroad: Seq[AmendAllOtherIncomeReceivedWhilstAbroadItem] = Seq(
    AmendAllOtherIncomeReceivedWhilstAbroadItem(
      countryCode = "FRA",
      amountBeforeTax = Some(1999.99),
      taxTakenOff = Some(2.23),
      specialWithholdingTax = Some(3.23),
      foreignTaxCreditRelief = false,
      taxableAmount = 4.23,
      residentialFinancialCostAmount = Some(2999.99),
      broughtFwdResidentialFinancialCostAmount = Some(1999.99)
    ),
    AmendAllOtherIncomeReceivedWhilstAbroadItem(
      countryCode = "IND",
      amountBeforeTax = Some(2999.99),
      taxTakenOff = Some(3.23),
      specialWithholdingTax = Some(4.23),
      foreignTaxCreditRelief = true,
      taxableAmount = 5.23,
      residentialFinancialCostAmount = Some(3999.99),
      broughtFwdResidentialFinancialCostAmount = Some(2999.99)
    )
  )

  val overseasIncomeAndGains: AmendOverseasIncomeAndGains =
    AmendOverseasIncomeAndGains(
      gainAmount = 3000.99
    )

  val chargeableForeignBenefitsAndGifts: AmendChargeableForeignBenefitsAndGifts =
    AmendChargeableForeignBenefitsAndGifts(
      transactionBenefit = Some(1999.99),
      protectedForeignIncomeSourceBenefit = Some(2999.99),
      protectedForeignIncomeOnwardGift = Some(3999.99),
      benefitReceivedAsASettler = Some(4999.99),
      onwardGiftReceivedAsASettler = Some(5999.99)
    )

  val omittedForeignIncome: AmendOmittedForeignIncome =
    AmendOmittedForeignIncome(
      amount = 4000.99
    )

  val amendOtherRequestBody: AmendOtherRequestBody = AmendOtherRequestBody(
    businessReceipts = Some(businessReceipts),
    allOtherIncomeReceivedWhilstAbroad = Some(allOtherIncomeReceivedWhilstAbroad),
    overseasIncomeAndGains = Some(overseasIncomeAndGains),
    chargeableForeignBenefitsAndGifts = Some(chargeableForeignBenefitsAndGifts),
    omittedForeignIncome = Some(omittedForeignIncome)
  )

  val requestData: AmendOtherRequest = AmendOtherRequest(
    nino = Nino(nino),
    taxYear = taxYear,
    body = amendOtherRequestBody
  )

  val hateoasResponse: JsValue = Json.parse(
    s"""
       |{
       |   "links":[
       |      {
       |         "href":"/individuals/income-received/other/$nino/$taxYear",
       |         "rel":"create-and-amend-other-income",
       |         "method":"PUT"
       |      },
       |      {
       |         "href":"/individuals/income-received/other/$nino/$taxYear",
       |         "rel":"self",
       |         "method":"GET"
       |      },
       |      {
       |         "href":"/individuals/income-received/other/$nino/$taxYear",
       |         "rel":"delete-other-income",
       |         "method":"DELETE"
       |      }
       |   ]
       |}
    """.stripMargin
  )

  def event(auditResponse: AuditResponse): AuditEvent[GenericAuditDetail] =
    AuditEvent(
      auditType = "CreateAmendOtherIncome",
      transactionName = "create-amend-other-income",
      detail = GenericAuditDetail(
        userType = "Individual",
        agentReferenceNumber = None,
        params = Map("nino" -> nino, "taxYear" -> taxYear),
        request = Some(requestBodyJson),
        `X-CorrelationId` = correlationId,
        response = auditResponse
      )
    )

  "AmendOtherController" should {
    "return OK" when {
      "happy path" in new Test {

        MockAmendOtherRequestParser
          .parse(rawData)
          .returns(Right(requestData))

        MockAmendOtherService
          .amend(requestData)
          .returns(Future.successful(Right(ResponseWrapper(correlationId, ()))))

        val result: Future[Result] = controller.amendOther(nino, taxYear)(fakePutRequest(requestBodyJson))

        status(result) shouldBe OK
        contentAsJson(result) shouldBe hateoasResponse
        header("X-CorrelationId", result) shouldBe Some(correlationId)

        val auditResponse: AuditResponse = AuditResponse(OK, None, Some(hateoasResponse))
        MockedAuditService.verifyAuditEvent(event(auditResponse)).once
      }
    }

    "return the error as per spec" when {
      "parser errors occur" must {
        def errorsFromParserTester(error: MtdError, expectedStatus: Int): Unit = {
          s"a ${error.code} error is returned from the parser" in new Test {

            MockAmendOtherRequestParser
              .parse(rawData)
              .returns(Left(ErrorWrapper(correlationId, error, None)))

            val result: Future[Result] = controller.amendOther(nino, taxYear)(fakePutRequest(requestBodyJson))

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

            MockAmendOtherRequestParser
              .parse(rawData)
              .returns(Right(requestData))

            MockAmendOtherService
              .amend(requestData)
              .returns(Future.successful(Left(ErrorWrapper(correlationId, mtdError))))

            val result: Future[Result] = controller.amendOther(nino, taxYear)(fakePutRequest(requestBodyJson))

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