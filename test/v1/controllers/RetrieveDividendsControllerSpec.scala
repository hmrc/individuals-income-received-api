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

import play.api.libs.json.Json
import play.api.mvc.Result
import uk.gov.hmrc.domain.Nino
import uk.gov.hmrc.http.HeaderCarrier
import v1.fixtures.RetrieveDividendsControllerFixture
import v1.hateoas.HateoasLinks
import v1.mocks.MockIdGenerator
import v1.mocks.hateoas.MockHateoasFactory
import v1.mocks.requestParsers.MockDeleteRetrieveRequestParser
import v1.mocks.services.{MockDeleteRetrieveService, MockEnrolmentsAuthService, MockMtdIdLookupService}
import v1.models.errors._
import v1.models.hateoas.Method.{DELETE, GET, PUT}
import v1.models.hateoas.RelType.{AMEND_DIVIDENDS_INCOME, DELETE_DIVIDENDS_INCOME, SELF}
import v1.models.hateoas.{HateoasWrapper, Link}
import v1.models.outcomes.ResponseWrapper
import v1.models.request.{DeleteRetrieveRawData, DeleteRetrieveRequest}
import v1.models.response.retrieveDividends._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class RetrieveDividendsControllerSpec extends ControllerBaseSpec
  with MockEnrolmentsAuthService
  with MockMtdIdLookupService
  with MockDeleteRetrieveService
  with MockHateoasFactory
  with MockDeleteRetrieveRequestParser
  with HateoasLinks
  with MockIdGenerator {

  private val nino: String = "AA123456A"
  private val taxYear: String = "2019-20"
  private val correlationId: String = "X-123"

  private val rawData: DeleteRetrieveRawData = DeleteRetrieveRawData(
    nino = nino,
    taxYear = taxYear
  )

  private val requestData: DeleteRetrieveRequest = DeleteRetrieveRequest(
    nino = Nino(nino),
    taxYear = taxYear
  )

  private val amendDividendsLink: Link =
    Link(
      href = s"/individuals/income-received/dividends/$nino/$taxYear",
      method = PUT,
      rel = AMEND_DIVIDENDS_INCOME
    )

  private val retrieveDividendsLink: Link =
    Link(
      href = s"/individuals/income-received/dividends/$nino/$taxYear",
      method = GET,
      rel = SELF
    )

  private val deleteDividendsLink: Link =
    Link(
      href = s"/individuals/income-received/dividends/$nino/$taxYear",
      method = DELETE,
      rel = DELETE_DIVIDENDS_INCOME
    )

  private val foreignDividendItemModel = Seq(
    ForeignDividendItem(
      countryCode = "DEU",
      amountBeforeTax = Some(1232.22),
      taxTakenOff = Some(22.22),
      specialWithholdingTax = Some(27.35),
      foreignTaxCreditRelief = true,
      taxableAmount = 2321.22
    ),
    ForeignDividendItem(
      countryCode = "FRA",
      amountBeforeTax = Some(1350.55),
      taxTakenOff = Some(25.27),
      specialWithholdingTax = Some(30.59),
      foreignTaxCreditRelief = false,
      taxableAmount = 2500.99
    )
  )

  private val dividendIncomeReceivedWhilstAbroadItemModel = Seq(
    DividendIncomeReceivedWhilstAbroadItem(
      countryCode = "DEU",
      amountBeforeTax = Some(1232.22),
      taxTakenOff = Some(22.22),
      specialWithholdingTax = Some(27.35),
      foreignTaxCreditRelief = true,
      taxableAmount = 2321.22
    ),
    DividendIncomeReceivedWhilstAbroadItem(
      countryCode = "FRA",
      amountBeforeTax = Some(1350.55),
      taxTakenOff = Some(25.27),
      specialWithholdingTax = Some(30.59),
      foreignTaxCreditRelief = false,
      taxableAmount = 2500.99
    )
  )

  private val stockDividendModel = StockDividend(
    customerReference = Some("my divs"),
    grossAmount = 12321.22
  )

  private val redeemableSharesModel = RedeemableShares(
    customerReference = Some("my shares"),
    grossAmount = 12345.75
  )

  private val bonusIssuesOfSecuritiesModel = BonusIssuesOfSecurities(
    customerReference = Some("my secs"),
    grossAmount = 12500.89
  )

  private val closeCompanyLoansWrittenOffModel = CloseCompanyLoansWrittenOff(
    customerReference = Some("write off"),
    grossAmount = 13700.55
  )

  private val retrieveDividendsResponseModel = RetrieveDividendsResponse(
    submittedOn = "2020-07-06T09:37:17Z",
    foreignDividend = Some(foreignDividendItemModel),
    dividendIncomeReceivedWhilstAbroad = Some(dividendIncomeReceivedWhilstAbroadItemModel),
    stockDividend = Some(stockDividendModel),
    redeemableShares = Some(redeemableSharesModel),
    bonusIssuesOfSecurities = Some(bonusIssuesOfSecuritiesModel),
    closeCompanyLoansWrittenOff = Some(closeCompanyLoansWrittenOffModel)
  )

  private val mtdResponse = RetrieveDividendsControllerFixture.mtdResponseWithHateoas(nino, taxYear)

  trait Test {
    val hc: HeaderCarrier = HeaderCarrier()

    val controller = new RetrieveDividendsController(
      authService = mockEnrolmentsAuthService,
      lookupService = mockMtdIdLookupService,
      requestParser = mockDeleteRetrieveRequestParser,
      service = mockDeleteRetrieveService,
      hateoasFactory = mockHateoasFactory,
      cc = cc,
      idGenerator = mockIdGenerator
    )

    MockedMtdIdLookupService.lookup(nino).returns(Future.successful(Right("test-mtd-id")))
    MockedEnrolmentsAuthService.authoriseUser()
    MockIdGenerator.generateCorrelationId.returns(correlationId)
  }

  "RetrieveDividendsController" should {
    "return OK" when {
      "happy path" in new Test {

        MockDeleteRetrieveRequestParser
          .parse(rawData)
          .returns(Right(requestData))

        MockDeleteRetrieveService
          .retrieve[RetrieveDividendsResponse]()
          .returns(Future.successful(Right(ResponseWrapper(correlationId, retrieveDividendsResponseModel))))

        MockHateoasFactory
          .wrap(retrieveDividendsResponseModel, RetrieveDividendsHateoasData(nino, taxYear))
          .returns(HateoasWrapper(retrieveDividendsResponseModel,
            Seq(
              amendDividendsLink,
              retrieveDividendsLink,
              deleteDividendsLink
            )
          ))

        val result: Future[Result] = controller.retrieveDividends(nino, taxYear)(fakeGetRequest)

        status(result) shouldBe OK
        contentAsJson(result) shouldBe mtdResponse
        header("X-CorrelationId", result) shouldBe Some(correlationId)
      }
    }

    "return the error as per spec" when {
      "parser errors occur" must {
        def errorsFromParserTester(error: MtdError, expectedStatus: Int): Unit = {
          s"a ${error.code} error is returned from the parser" in new Test {

            MockDeleteRetrieveRequestParser
              .parse(rawData)
              .returns(Left(ErrorWrapper(correlationId, error, None)))

            val result: Future[Result] = controller.retrieveDividends(nino, taxYear)(fakeGetRequest)

            status(result) shouldBe expectedStatus
            contentAsJson(result) shouldBe Json.toJson(error)
            header("X-CorrelationId", result) shouldBe Some(correlationId)
          }
        }

        val input = Seq(
          (BadRequestError, BAD_REQUEST),
          (NinoFormatError, BAD_REQUEST),
          (TaxYearFormatError, BAD_REQUEST),
          (RuleTaxYearNotSupportedError, BAD_REQUEST),
          (RuleTaxYearRangeInvalidError, BAD_REQUEST)
        )

        input.foreach(args => (errorsFromParserTester _).tupled(args))
      }

      "service errors occur" must {
        def serviceErrors(mtdError: MtdError, expectedStatus: Int): Unit = {
          s"a $mtdError error is returned from the service" in new Test {

            MockDeleteRetrieveRequestParser
              .parse(rawData)
              .returns(Right(requestData))

            MockDeleteRetrieveService
              .retrieve[RetrieveDividendsResponse]()
              .returns(Future.successful(Left(ErrorWrapper(correlationId, mtdError))))

            val result: Future[Result] = controller.retrieveDividends(nino, taxYear)(fakeGetRequest)

            status(result) shouldBe expectedStatus
            contentAsJson(result) shouldBe Json.toJson(mtdError)
            header("X-CorrelationId", result) shouldBe Some(correlationId)
          }
        }

        val input = Seq(
          (NinoFormatError, BAD_REQUEST),
          (TaxYearFormatError, BAD_REQUEST),
          (NotFoundError, NOT_FOUND),
          (DownstreamError, INTERNAL_SERVER_ERROR)
        )

        input.foreach(args => (serviceErrors _).tupled(args))
      }
    }
  }
}