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
import v1.fixtures.RetrieveEmploymentControllerFixture._
import v1.hateoas.HateoasLinks
import v1.mocks.MockIdGenerator
import v1.mocks.hateoas.MockHateoasFactory
import v1.mocks.requestParsers.MockRetrieveEmploymentRequestParser
import v1.mocks.services.{MockDeleteRetrieveService, MockEnrolmentsAuthService, MockMtdIdLookupService}
import v1.models.errors._
import v1.models.hateoas.{HateoasWrapper, Link}
import v1.models.hateoas.Method._
import v1.models.request.retrieveEmployment.{RetrieveEmploymentRawData, RetrieveEmploymentRequest}
import v1.models.hateoas.RelType._
import v1.models.outcomes.ResponseWrapper
import v1.models.response.retrieveEmployment.{RetrieveEmploymentHateoasData, RetrieveEmploymentResponse}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future


class RetrieveEmploymentControllerSpec extends ControllerBaseSpec
  with MockEnrolmentsAuthService
  with MockMtdIdLookupService
  with MockDeleteRetrieveService
  with MockHateoasFactory
  with MockRetrieveEmploymentRequestParser
  with HateoasLinks
  with MockIdGenerator {

  val nino: String = "AA123456A"
  val taxYear: String = "2019-20"
  val correlationId: String = "X-123"
  val employmentId: String = "4557ecb5-fd32-48cc-81f5-e6acd1099f3c"

  val rawData: RetrieveEmploymentRawData = RetrieveEmploymentRawData(
    nino = nino,
    taxYear = taxYear,
    employmentId = employmentId
  )

  val requestData: RetrieveEmploymentRequest = RetrieveEmploymentRequest(
    nino = Nino(nino),
    taxYear = taxYear,
    employmentId = employmentId
  )

  private val listEmploymentLink: Link =
    Link(
      href = s"/individuals/income-received/employments/$nino/$taxYear",
      method = GET,
      rel = LIST_EMPLOYMENTS
    )

  private val retrieveEmploymentLink: Link =
    Link(
      href = s"/individuals/income-received/employments/$nino/$taxYear/$employmentId",
      method = GET,
      rel = SELF
    )

  private val amendCustomEmploymentLink: Link =
    Link(
      href = s"/individuals/income-received/employments/$nino/$taxYear/$employmentId",
      method = PUT,
      rel = AMEND_CUSTOM_EMPLOYMENT
    )

  private val deleteCustomEmploymentLink: Link =
    Link(
      href = s"/individuals/income-received/employments/$nino/$taxYear/$employmentId",
      method = DELETE,
      rel = DELETE_CUSTOM_EMPLOYMENT
    )

  private val ignoreEmploymentLink: Link =
    Link(
      href = s"/individuals/income-received/employments/$nino/$taxYear/$employmentId/ignore",
      method = PUT,
      rel = IGNORE_EMPLOYMENT
    )

  private val hmrcEnteredEmploymentResponseModel = RetrieveEmploymentResponse(
    employerRef = Some("123/AB56797"),
    employerName = "Employer Name Ltd.",
    startDate = "2020-06-17",
    cessationDate = Some("2020-06-17"),
    payrollId = Some("123345657"),
    dateIgnored = Some("2020-06-17T10:53:38Z"),
    None
  )

  private val customEnteredEmploymentResponseModel = RetrieveEmploymentResponse(
    employerRef = Some("123/AB56797"),
    employerName = "Employer Name Ltd.",
    startDate = "2020-06-17",
    cessationDate = Some("2020-06-17"),
    payrollId = Some("123345657"),
    None,
    submittedOn = Some("2020-06-17T10:53:38Z")
  )

  private val mtdHmrcEnteredResponse = mtdHmrcEnteredResponseWithHateoas(nino, taxYear, employmentId)

  private val mtdCustomEnteredResponse = mtdCustomEnteredResponseWithHateoas(nino, taxYear, employmentId)

  trait Test {
    val hc: HeaderCarrier = HeaderCarrier()

    val controller = new RetrieveEmploymentController(
      authService = mockEnrolmentsAuthService,
      lookupService = mockMtdIdLookupService,
      requestParser = mockRetrieveCustomEmploymentRequestParser,
      service = mockDeleteRetrieveService,
      hateoasFactory = mockHateoasFactory,
      cc = cc,
      idGenerator = mockIdGenerator
    )

    MockedMtdIdLookupService.lookup(nino).returns(Future.successful(Right("test-mtd-id")))
    MockedEnrolmentsAuthService.authoriseUser()
    MockIdGenerator.generateCorrelationId.returns(correlationId)
  }

  "RetrieveEmploymentController" should {
    "return OK" when {
      "happy path for retrieving hmrc entered employment" in new Test {

        MockRetrieveCustomEmploymentRequestParser
          .parse(rawData)
          .returns(Right(requestData))

        MockDeleteRetrieveService
          .retrieve[RetrieveEmploymentResponse]()
          .returns(Future.successful(Right(ResponseWrapper(correlationId, hmrcEnteredEmploymentResponseModel))))

        MockHateoasFactory
          .wrap(hmrcEnteredEmploymentResponseModel, RetrieveEmploymentHateoasData(nino, taxYear, employmentId, hmrcEnteredEmploymentResponseModel))
          .returns(HateoasWrapper(hmrcEnteredEmploymentResponseModel,
            Seq(
              listEmploymentLink,
              retrieveEmploymentLink,
              ignoreEmploymentLink
            )
          ))

        val result: Future[Result] = controller.retrieveEmployment(nino, taxYear, employmentId)(fakeGetRequest)

        status(result) shouldBe OK
        contentAsJson(result) shouldBe mtdHmrcEnteredResponse
        header("X-CorrelationId", result) shouldBe Some(correlationId)
      }
    }

    "return OK" when {
      "happy path for retrieving custom entered employment" in new Test {

        MockRetrieveCustomEmploymentRequestParser
          .parse(rawData)
          .returns(Right(requestData))

        MockDeleteRetrieveService
          .retrieve[RetrieveEmploymentResponse]()
          .returns(Future.successful(Right(ResponseWrapper(correlationId, customEnteredEmploymentResponseModel))))

        MockHateoasFactory
          .wrap(customEnteredEmploymentResponseModel, RetrieveEmploymentHateoasData(nino, taxYear, employmentId, customEnteredEmploymentResponseModel))
          .returns(HateoasWrapper(customEnteredEmploymentResponseModel,
            Seq(
              listEmploymentLink,
              retrieveEmploymentLink,
              amendCustomEmploymentLink,
              deleteCustomEmploymentLink
            )
          ))

        val result: Future[Result] = controller.retrieveEmployment(nino, taxYear, employmentId)(fakeGetRequest)

        status(result) shouldBe OK
        contentAsJson(result) shouldBe mtdCustomEnteredResponse
        header("X-CorrelationId", result) shouldBe Some(correlationId)
      }
    }

    "return the error as per spec" when {
      "parser errors occur" must {
        def errorsFromParserTester(error: MtdError, expectedStatus: Int): Unit = {
          s"a ${error.code} error is returned from the parser" in new Test {

            MockRetrieveCustomEmploymentRequestParser
              .parse(rawData)
              .returns(Left(ErrorWrapper(correlationId, error, None)))

            val result: Future[Result] = controller.retrieveEmployment(nino, taxYear, employmentId)(fakeGetRequest)

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
          (RuleTaxYearNotSupportedError, BAD_REQUEST),
          (RuleTaxYearRangeInvalidError, BAD_REQUEST)
        )

        input.foreach(args => (errorsFromParserTester _).tupled(args))
      }

      "service errors occur" must {
        def serviceErrors(mtdError: MtdError, expectedStatus: Int): Unit = {
          s"a $mtdError error is returned from the service" in new Test {

            MockRetrieveCustomEmploymentRequestParser
              .parse(rawData)
              .returns(Right(requestData))

            MockDeleteRetrieveService
              .retrieve[RetrieveEmploymentResponse]()
              .returns(Future.successful(Left(ErrorWrapper(correlationId, mtdError))))

            val result: Future[Result] = controller.retrieveEmployment(nino, taxYear,employmentId)(fakeGetRequest)

            status(result) shouldBe expectedStatus
            contentAsJson(result) shouldBe Json.toJson(mtdError)
            header("X-CorrelationId", result) shouldBe Some(correlationId)
          }
        }

        val input = Seq(
          (NinoFormatError, BAD_REQUEST),
          (TaxYearFormatError, BAD_REQUEST),
          (EmploymentIdFormatError, BAD_REQUEST),
          (NotFoundError, NOT_FOUND),
          (DownstreamError, INTERNAL_SERVER_ERROR)
        )

        input.foreach(args => (serviceErrors _).tupled(args))
      }
    }
  }
}