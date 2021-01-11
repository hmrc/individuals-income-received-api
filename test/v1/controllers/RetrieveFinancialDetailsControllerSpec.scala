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
import v1.fixtures.RetrieveFinancialDetailsControllerFixture._
import v1.hateoas.HateoasLinks
import v1.mocks.MockIdGenerator
import v1.mocks.hateoas.MockHateoasFactory
import v1.mocks.requestParsers.MockRetrieveFinancialDetailsRequestParser
import v1.mocks.services.{MockDeleteRetrieveService, MockEnrolmentsAuthService, MockMtdIdLookupService}
import v1.models.domain.MtdSourceEnum
import v1.models.errors._
import v1.models.hateoas.Method.{DELETE, GET, PUT}
import v1.models.hateoas.RelType.{AMEND_EMPLOYMENT_FINANCIAL_DETAILS, DELETE_EMPLOYMENT_FINANCIAL_DETAILS, SELF}
import v1.models.hateoas.{HateoasWrapper, Link}
import v1.models.outcomes.ResponseWrapper
import v1.models.request.retrieveFinancialDetails.{RetrieveFinancialDetailsRawData, RetrieveFinancialDetailsRequest}
import v1.models.response.retrieveFinancialDetails._
import v1.models.response.retrieveOther._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class RetrieveFinancialDetailsControllerSpec extends ControllerBaseSpec
  with MockEnrolmentsAuthService
  with MockMtdIdLookupService
  with MockDeleteRetrieveService
  with MockHateoasFactory
  with MockRetrieveFinancialDetailsRequestParser
  with HateoasLinks
  with MockIdGenerator {

  val nino: String = "AA123456A"
  val taxYear: String = "2017-18"
  val correlationId: String = "X-123"
  val employmentId: String = "4557ecb5-fd32-48cc-81f5-e6acd1099f3c"
  val source: String = "latest"

  val rawData: RetrieveFinancialDetailsRawData = RetrieveFinancialDetailsRawData(
    nino = nino,
    taxYear = taxYear,
    employmentId = employmentId,
    source = Some(source)
  )

  val requestData: RetrieveFinancialDetailsRequest = RetrieveFinancialDetailsRequest(
    nino = Nino(nino),
    taxYear = taxYear,
    employmentId = employmentId,
    source = MtdSourceEnum.latest
  )

  val amendLink: Link =
    Link(
      href = s"/individuals/income-received/employments/$nino/$taxYear/$employmentId/financial-details",
      method = PUT,
      rel = AMEND_EMPLOYMENT_FINANCIAL_DETAILS
    )

  val retrieveLink: Link =
    Link(
      href = s"/individuals/income-received/employments/$nino/$taxYear/$employmentId/financial-details",
      method = GET,
      rel = SELF
    )

  val deleteLink: Link =
    Link(
      href = s"/individuals/income-received/employments/$nino/$taxYear/$employmentId/financial-details",
      method = DELETE,
      rel = DELETE_EMPLOYMENT_FINANCIAL_DETAILS
    )

  private val mtdResponse = mtdResponseWithHateoas(nino, taxYear, employmentId)

  trait Test {
    val hc: HeaderCarrier = HeaderCarrier()

    val controller = new RetrieveFinancialDetailsController(
      authService = mockEnrolmentsAuthService,
      lookupService = mockMtdIdLookupService,
      requestParser = mockRetrieveFinancialDetailsRequestParser,
      service = mockDeleteRetrieveService,
      hateoasFactory = mockHateoasFactory,
      cc = cc,
      idGenerator = mockIdGenerator
    )

    MockedMtdIdLookupService.lookup(nino).returns(Future.successful(Right("test-mtd-id")))
    MockedEnrolmentsAuthService.authoriseUser()
    MockIdGenerator.generateCorrelationId.returns(correlationId)
  }

  "MockRetrieveFinancialDetailsController" should {
    "return OK" when {
      "happy path" in new Test {

        MockRetrieveFinancialDetailsRequestParser
          .parse(rawData)
          .returns(Right(requestData))

        MockDeleteRetrieveService
          .retrieve[RetrieveFinancialDetailsResponse]()
          .returns(Future.successful(Right(ResponseWrapper(correlationId, model))))

        MockHateoasFactory
          .wrap(model, RetrieveFinancialDetailsHateoasData(nino, taxYear, employmentId))
          .returns(HateoasWrapper(model,
            Seq(
              retrieveLink,
              amendLink,
              deleteLink
            )
          ))

        val result: Future[Result] = controller.retrieve(nino, taxYear, employmentId, Some(source))(fakeGetRequest)

        status(result) shouldBe OK
        contentAsJson(result) shouldBe mtdResponse
        header("X-CorrelationId", result) shouldBe Some(correlationId)
      }
    }

    "return the error as per spec" when {
      "parser errors occur" must {
        def errorsFromParserTester(error: MtdError, expectedStatus: Int): Unit = {
          s"a ${error.code} error is returned from the parser" in new Test {

            MockRetrieveFinancialDetailsRequestParser
              .parse(rawData)
              .returns(Left(ErrorWrapper(correlationId, error, None)))

            val result: Future[Result] = controller.retrieve(nino, taxYear, employmentId, Some(source))(fakeGetRequest)

            status(result) shouldBe expectedStatus
            contentAsJson(result) shouldBe Json.toJson(error)
            header("X-CorrelationId", result) shouldBe Some(correlationId)
          }
        }

        val input = Seq(
          (BadRequestError, BAD_REQUEST),
          (NinoFormatError, BAD_REQUEST),
          (TaxYearFormatError, BAD_REQUEST),
          (EmploymentIdFormatError, BAD_REQUEST),
          (SourceFormatError, BAD_REQUEST),
          (RuleTaxYearRangeInvalidError, BAD_REQUEST),
          (RuleTaxYearNotSupportedError, BAD_REQUEST)
        )

        input.foreach(args => (errorsFromParserTester _).tupled(args))
      }

      "service errors occur" must {
        def serviceErrors(mtdError: MtdError, expectedStatus: Int): Unit = {
          s"a $mtdError error is returned from the service" in new Test {

            MockRetrieveFinancialDetailsRequestParser
              .parse(rawData)
              .returns(Right(requestData))

            MockDeleteRetrieveService
              .retrieve[RetrieveOtherResponse]()
              .returns(Future.successful(Left(ErrorWrapper(correlationId, mtdError))))

            val result: Future[Result] = controller.retrieve(nino, taxYear, employmentId, Some(source))(fakeGetRequest)

            status(result) shouldBe expectedStatus
            contentAsJson(result) shouldBe Json.toJson(mtdError)
            header("X-CorrelationId", result) shouldBe Some(correlationId)
          }
        }

        val input = Seq(
          (NinoFormatError, BAD_REQUEST),
          (TaxYearFormatError, BAD_REQUEST),
          (EmploymentIdFormatError, BAD_REQUEST),
          (SourceFormatError, BAD_REQUEST),
          (RuleTaxYearNotSupportedError, BAD_REQUEST),
          (NotFoundError, NOT_FOUND),
          (DownstreamError, INTERNAL_SERVER_ERROR)
        )

        input.foreach(args => (serviceErrors _).tupled(args))
      }
    }
  }
}