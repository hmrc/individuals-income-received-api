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

import play.api.libs.json.Json
import play.api.mvc.Result
import uk.gov.hmrc.http.HeaderCarrier
import v1r7.connectors.DownstreamUri.Api1661Uri
import v1r7.fixtures.RetrieveNonPayeEmploymentControllerFixture._
import v1r7.hateoas.HateoasLinks
import v1r7.mocks.MockIdGenerator
import v1r7.mocks.hateoas.MockHateoasFactory
import v1r7.mocks.requestParsers.MockRetrieveNonPayeEmploymentRequestParser
import v1r7.mocks.services.{MockDeleteRetrieveService, MockEnrolmentsAuthService, MockMtdIdLookupService}
import v1r7.models.domain.{MtdSourceEnum, Nino}
import v1r7.models.errors._
import v1r7.models.hateoas.Method.{DELETE, GET, PUT}
import v1r7.models.hateoas.RelType._
import v1r7.models.hateoas.{HateoasWrapper, Link}
import v1r7.models.outcomes.ResponseWrapper
import v1r7.models.request.retrieveNonPayeEmploymentIncome.{RetrieveNonPayeEmploymentIncomeRawData, RetrieveNonPayeEmploymentIncomeRequest}
import v1r7.models.response.retrieveNonPayeEmploymentIncome.{RetrieveNonPayeEmploymentIncomeHateoasData, RetrieveNonPayeEmploymentIncomeResponse}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class RetrieveNonPayeEmploymentControllerSpec extends ControllerBaseSpec
  with MockEnrolmentsAuthService
  with MockMtdIdLookupService
  with MockDeleteRetrieveService
  with MockHateoasFactory
  with MockRetrieveNonPayeEmploymentRequestParser
  with HateoasLinks
  with MockIdGenerator {

  val nino: String = "AA123456A"
  val taxYear: String = "2019-20"
  val correlationId: String = "X-123"

  def rawData(source: Option[String] = None): RetrieveNonPayeEmploymentIncomeRawData =
    RetrieveNonPayeEmploymentIncomeRawData(
      nino = nino,
      taxYear = taxYear,
      source
    )

  val requestData: RetrieveNonPayeEmploymentIncomeRequest =
    RetrieveNonPayeEmploymentIncomeRequest(
      nino = Nino(nino),
      taxYear = taxYear,
      MtdSourceEnum.latest
    )

  private val amendLink: Link = Link(
    href = s"/individuals/income-received/employments/non-paye/$nino/$taxYear",
    method = PUT,
    rel = AMEND_NON_PAYE_EMPLOYMENT_INCOME
  )

  private val deleteLink: Link = Link(
    href = s"/individuals/income-received/employments/non-paye/$nino/$taxYear",
    method = DELETE,
    rel = DELETE_NON_PAYE_EMPLOYMENT_INCOME
  )

  private val retrieveLink: Link = Link(
    href = s"/individuals/income-received/employments/non-paye/$nino/$taxYear",
    method = GET,
    rel = SELF
  )

  val desErrorMap: Map[String, MtdError] =
    Map(
      "INVALID_TAXABLE_ENTITY_ID" -> NinoFormatError,
      "INVALID_TAX_YEAR" -> TaxYearFormatError,
      "INVALID_VIEW" -> DownstreamError,
      "INVALID_CORRELATIONID" -> DownstreamError,
      "NO_DATA_FOUND" -> NotFoundError,
      "TAX_YEAR_NOT_SUPPORTED" -> RuleTaxYearNotSupportedError,
      "SERVER_ERROR" -> DownstreamError,
      "SERVICE_UNAVAILABLE" -> DownstreamError
    )

  trait Test {
    val hc: HeaderCarrier = HeaderCarrier()

    val controller = new RetrieveNonPayeEmploymentController(
      authService = mockEnrolmentsAuthService,
      lookupService = mockMtdIdLookupService,
      requestParser = mockRetrieveNonPayeEmploymentRequestParser,
      service = mockDeleteRetrieveService,
      hateoasFactory = mockHateoasFactory,
      cc = cc,
      idGenerator = mockIdGenerator
    )

    MockedMtdIdLookupService.lookup(nino).returns(Future.successful(Right("test-mtd-id")))
    MockedEnrolmentsAuthService.authoriseUser()
    MockIdGenerator.generateCorrelationId.returns(correlationId)
  }

  "RetrieveNonPayeEmploymentIncomeController" should {
    "return OK" when {
      "endpoint is hit without a source" in new Test {
        MockDeleteRetrieveRequestParser
          .parse(rawData())
          .returns(Right(requestData))

        MockDeleteRetrieveService
          .retrieve(
            Api1661Uri[RetrieveNonPayeEmploymentIncomeResponse](s"income-tax/income/employments/non-paye/$nino/$taxYear?view=LATEST"),
            desErrorMap)
          .returns(Future.successful(Right(ResponseWrapper(correlationId, responseModel))))

        MockHateoasFactory
          .wrap(responseModel, RetrieveNonPayeEmploymentIncomeHateoasData(nino, taxYear))
          .returns(HateoasWrapper(responseModel,
            Seq(
              amendLink,
              retrieveLink,
              deleteLink
            )
          ))

        val result: Future[Result] = controller.retrieveNonPayeEmployment(nino, taxYear, None)(fakeGetRequest)

        status(result) shouldBe OK
        contentAsJson(result) shouldBe mtdResponseWithHateoas(nino, taxYear)
        header("X-CorrelationId", result) shouldBe Some(correlationId)
      }

      def endpointIsHitWithASource(source: String, desSource: String): Unit =
        s"endpoint is hit with source $source" in new Test {
          MockDeleteRetrieveRequestParser
            .parse(rawData(Some(source)))
            .returns(Right(requestData))

          MockDeleteRetrieveService
            .retrieve(
              Api1661Uri[RetrieveNonPayeEmploymentIncomeResponse](s"income-tax/income/employments/non-paye/$nino/$taxYear?view=$desSource"),
              desErrorMap)
            .returns(Future.successful(Right(ResponseWrapper(correlationId, responseModel))))

          MockHateoasFactory
            .wrap(responseModel, RetrieveNonPayeEmploymentIncomeHateoasData(nino, taxYear))
            .returns(HateoasWrapper(responseModel,
              Seq(
                amendLink,
                retrieveLink,
                deleteLink
              )
            ))

          val result: Future[Result] = controller.retrieveNonPayeEmployment(nino, taxYear, Some(source))(fakeGetRequest)

          status(result) shouldBe OK
          contentAsJson(result) shouldBe mtdResponseWithHateoas(nino, taxYear)
          header("X-CorrelationId", result) shouldBe Some(correlationId)
        }

      val input = Seq(
        ("latest", "LATEST"),
        ("hmrcHeld", "HMRC-HELD"),
        ("user", "CUSTOMER")
      )

      input.foreach(args => (endpointIsHitWithASource _).tupled(args))
    }

    "return the error as per spec" when {
      "parser errors occur" must {
        def errorsFromParserTester(error: MtdError, expectedStatus: Int): Unit = {
          s"a ${error.code} error is returned from the parser" in new Test {

            MockDeleteRetrieveRequestParser
              .parse(rawData())
              .returns(Left(ErrorWrapper(correlationId, error, None)))

            val result: Future[Result] = controller.retrieveNonPayeEmployment(nino, taxYear, None)(fakeGetRequest)

            status(result) shouldBe expectedStatus
            contentAsJson(result) shouldBe Json.toJson(error)
            header("X-CorrelationId", result) shouldBe Some(correlationId)
          }
        }

        val input = Seq(
          (BadRequestError, BAD_REQUEST),
          (NinoFormatError, BAD_REQUEST),
          (TaxYearFormatError, BAD_REQUEST),
          (SourceFormatError, BAD_REQUEST),
          (RuleTaxYearNotSupportedError, BAD_REQUEST),
          (RuleTaxYearRangeInvalidError, BAD_REQUEST)
        )

        input.foreach(args => (errorsFromParserTester _).tupled(args))
      }

      "service errors occur" must {
        def serviceErrors(mtdError: MtdError, expectedStatus: Int): Unit = {
          s"a $mtdError error is returned from the service" in new Test {

            MockDeleteRetrieveRequestParser
              .parse(rawData())
              .returns(Right(requestData))

            MockDeleteRetrieveService
              .retrieve[RetrieveNonPayeEmploymentIncomeResponse](desErrorMap)
              .returns(Future.successful(Left(ErrorWrapper(correlationId, mtdError))))

            val result: Future[Result] = controller.retrieveNonPayeEmployment(nino, taxYear, None)(fakeGetRequest)

            status(result) shouldBe expectedStatus
            contentAsJson(result) shouldBe Json.toJson(mtdError)
            header("X-CorrelationId", result) shouldBe Some(correlationId)
          }
        }

        val input = Seq(
          (NinoFormatError, BAD_REQUEST),
          (TaxYearFormatError, BAD_REQUEST),
          (RuleTaxYearNotSupportedError, BAD_REQUEST),
          (NotFoundError, NOT_FOUND),
          (DownstreamError, INTERNAL_SERVER_ERROR),
          (MtdError("OTHER", "other"), INTERNAL_SERVER_ERROR)
        )

        input.foreach(args => (serviceErrors _).tupled(args))
      }
    }
  }
}