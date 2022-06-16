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
import api.mocks.requestParsers.MockDeleteRetrieveRequestParser
import api.mocks.services.{MockDeleteRetrieveService, MockEnrolmentsAuthService, MockMtdIdLookupService}
import api.models.domain.Nino
import api.models.errors.{BadRequestError, ErrorWrapper, MtdError, NinoFormatError, NotFoundError, RuleTaxYearNotSupportedError, RuleTaxYearRangeInvalidError, StandardDownstreamError, TaxYearFormatError}
import api.models.hateoas.{HateoasWrapper, Link}
import api.models.hateoas.Method.{DELETE, GET, PUT}
import api.models.hateoas.RelType.{AMEND_DIVIDENDS_INCOME, DELETE_DIVIDENDS_INCOME, SELF}
import api.models.outcomes.ResponseWrapper
import api.models.request
import api.models.request.{DeleteRetrieveRawData, DeleteRetrieveRequest}
import play.api.libs.json.Json
import play.api.mvc.Result
import uk.gov.hmrc.http.HeaderCarrier
import v1.models.response.retrieveUkDividendsAnnualIncomeSummary.{RetrieveUkDividendsAnnualIncomeSummaryHateoasData, RetrieveUkDividendsAnnualIncomeSummaryResponse}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class RetrieveUkDividendsAnnualIncomeSummaryControllerSpec
  extends ControllerBaseSpec
    with MockEnrolmentsAuthService
    with MockMtdIdLookupService
    with MockDeleteRetrieveService
    with MockHateoasFactory
    with MockRetrieve
    with HateoasLinks
    with MockIdGenerator {

  private val nino: String          = "AA123456A"
  private val taxYear: String       = "2019-20"
  private val correlationId: String = "X-123"

  private val rawData: DeleteRetrieveRawData = DeleteRetrieveRawData(
    nino = nino,
    taxYear = taxYear
  )

  private val requestData: DeleteRetrieveRequest = request.DeleteRetrieveRequest(
    nino = Nino(nino),
    taxYear = taxYear
  )

  private val amendUkDividendsLink: Link =
    Link(
      href = s"/individuals/income-received/uk-dividends/$nino/$taxYear",
      method = PUT,
      rel = AMEND_DIVIDENDS_INCOME
    )

  private val retrieveUkDividendsLink: Link =
    Link(
      href = s"/individuals/income-received/uk-dividends/$nino/$taxYear",
      method = GET,
      rel = SELF
    )

  private val deleteUkDividendsLink: Link =
    Link(
      href = s"/individuals/income-received/uk-dividends/$nino/$taxYear",
      method = DELETE,
      rel = DELETE_DIVIDENDS_INCOME
    )

  private val retrieveUkDividendsAnnualIncomeSummaryResponseModel = RetrieveUkDividendsAnnualIncomeSummaryResponse(
    Some(100.99),
    Some(100.99)
  )


  private val mtdResponse = ""

  trait Test {
    val hc: HeaderCarrier = HeaderCarrier()

    val controller = new RetrieveUkDividendsAnnualIncomeSummaryController()(
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
          .retrieve[RetrieveUkDividendsAnnualIncomeSummaryResponse](defaultDownstreamErrorMap)
          .returns(Future.successful(Right(ResponseWrapper(correlationId, retrieveUkDividendsAnnualIncomeSummaryResponseModel))))

        MockHateoasFactory
          .wrap(retrieveUkDividendsAnnualIncomeSummaryResponseModel, RetrieveUkDividendsAnnualIncomeSummaryHateoasData(nino, taxYear))
          .returns(
            HateoasWrapper(
              retrieveUkDividendsAnnualIncomeSummaryResponseModel,
              Seq(
                amendUkDividendsLink,
                retrieveUkDividendsLink,
                deleteUkDividendsLink
              )))

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
              .retrieve[RetrieveUkDividendsAnnualIncomeSummaryResponse](defaultDownstreamErrorMap)
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
          (StandardDownstreamError, INTERNAL_SERVER_ERROR)
        )

        input.foreach(args => (serviceErrors _).tupled(args))
      }
    }
  }

}