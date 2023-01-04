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

import api.controllers.ControllerBaseSpec
import api.hateoas.HateoasLinks
import api.mocks.MockIdGenerator
import api.mocks.hateoas.MockHateoasFactory
import api.mocks.services.{MockEnrolmentsAuthService, MockMtdIdLookupService}
import api.models.domain.{Nino, TaxYear}
import api.models.errors._
import api.models.hateoas.Method.{DELETE, GET, PUT}
import api.models.hateoas.RelType.{AMEND_DIVIDENDS_INCOME, DELETE_DIVIDENDS_INCOME, SELF}
import api.models.hateoas.{HateoasWrapper, Link}
import api.models.outcomes.ResponseWrapper
import play.api.libs.json.Json
import play.api.mvc.Result
import uk.gov.hmrc.http.HeaderCarrier
import v1.fixtures.RetrieveDividendsFixtures
import v1.fixtures.RetrieveDividendsFixtures.responseModel
import v1.mocks.requestParsers.MockRetrieveDividendsRequestParser
import v1.mocks.services.MockRetrieveDividendsService
import v1.models.request.retrieveDividends.{RetrieveDividendsRawData, RetrieveDividendsRequest}
import v1.models.response.retrieveDividends._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class RetrieveDividendsControllerSpec
    extends ControllerBaseSpec
    with MockEnrolmentsAuthService
    with MockMtdIdLookupService
    with MockRetrieveDividendsService
    with MockHateoasFactory
    with MockRetrieveDividendsRequestParser
    with HateoasLinks
    with MockIdGenerator {

  private val nino: String          = "AA123456A"
  private val taxYear: String       = "2019-20"
  private val correlationId: String = "X-123"

  private val rawData: RetrieveDividendsRawData = RetrieveDividendsRawData(
    nino = nino,
    taxYear = taxYear
  )

  private val requestData: RetrieveDividendsRequest = RetrieveDividendsRequest(
    nino = Nino(nino),
    taxYear = TaxYear.fromMtd(taxYear)
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

  private val mtdResponse = RetrieveDividendsFixtures.mtdResponseWithHateoas(nino, taxYear)

  trait Test {
    val hc: HeaderCarrier = HeaderCarrier()

    val controller = new RetrieveDividendsController(
      authService = mockEnrolmentsAuthService,
      lookupService = mockMtdIdLookupService,
      requestParser = mockRetrieveDividendsRequestParser,
      service = mockRetrieveDividendsService,
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

        MockRetrieveDividendsRequestParser
          .parse(rawData)
          .returns(Right(requestData))

        MockRetrieveDividendsService
          .retrieve(requestData)
          .returns(Future.successful(Right(ResponseWrapper(correlationId, responseModel))))

        MockHateoasFactory
          .wrap(responseModel, RetrieveDividendsHateoasData(nino, taxYear))
          .returns(
            HateoasWrapper(
              responseModel,
              Seq(
                amendDividendsLink,
                retrieveDividendsLink,
                deleteDividendsLink
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

            MockRetrieveDividendsRequestParser
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

            MockRetrieveDividendsRequestParser
              .parse(rawData)
              .returns(Right(requestData))

            MockRetrieveDividendsService
              .retrieve(requestData)
              .returns(Future.successful(Left(ErrorWrapper(correlationId, mtdError))))

            val result: Future[Result] = controller.retrieveDividends(nino, taxYear)(fakeGetRequest)

            status(result) shouldBe expectedStatus
            contentAsJson(result) shouldBe Json.toJson(mtdError)
            header("X-CorrelationId", result) shouldBe Some(correlationId)
          }
        }

        val errors = Seq(
          (NinoFormatError, BAD_REQUEST),
          (TaxYearFormatError, BAD_REQUEST),
          (NotFoundError, NOT_FOUND),
          (StandardDownstreamError, INTERNAL_SERVER_ERROR)
        )

        val extraTysErrors = List(
          (RuleTaxYearNotSupportedError, BAD_REQUEST)
        )

        (errors ++ extraTysErrors).foreach(args => (serviceErrors _).tupled(args))
      }
    }
  }

}
