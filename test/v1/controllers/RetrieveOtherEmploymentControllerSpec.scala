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
import api.mocks.services.{MockMtdIdLookupService, MockEnrolmentsAuthService}
import api.models.domain.{Nino, TaxYear}
import api.models.errors._
import api.models.hateoas.{HateoasWrapper, Link}
import api.models.hateoas.Method.{DELETE, GET, PUT}
import api.models.hateoas.RelType.{AMEND_OTHER_EMPLOYMENT_INCOME, DELETE_OTHER_EMPLOYMENT_INCOME, SELF}
import api.models.outcomes.ResponseWrapper
import play.api.libs.json.Json
import play.api.mvc.Result
import uk.gov.hmrc.http.HeaderCarrier
import v1.fixtures.OtherIncomeEmploymentFixture.retrieveOtherResponseModel
import v1.fixtures.RetrieveOtherEmploymentControllerFixture._
import v1.mocks.requestParsers.MockOtherEmploymentIncomeRequestParser
import v1.mocks.services.MockOtherEmploymentIncomeService
import v1.models.response.retrieveOtherEmployment._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import v1.models.request.otherEmploymentIncome.{OtherEmploymentIncomeRequestRawData, OtherEmploymentIncomeRequest}

class RetrieveOtherEmploymentControllerSpec
    extends ControllerBaseSpec
    with MockEnrolmentsAuthService
    with MockMtdIdLookupService
    with MockOtherEmploymentIncomeService
    with MockHateoasFactory
    with MockOtherEmploymentIncomeRequestParser
    with HateoasLinks
    with MockIdGenerator {

  val nino: String          = "AA123456A"
  val taxYear: String       = "2019-20"
  val correlationId: String = "X-123"

  val rawData: OtherEmploymentIncomeRequestRawData = OtherEmploymentIncomeRequestRawData(
    nino = nino,
    taxYear = taxYear
  )

  val requestData: OtherEmploymentIncomeRequest = OtherEmploymentIncomeRequest(
    nino = Nino(nino),
    taxYear = TaxYear.fromMtd(taxYear)
  )

  private val amendLink: Link = Link(
    href = s"/individuals/income-received/employments/other/$nino/$taxYear",
    method = PUT,
    rel = AMEND_OTHER_EMPLOYMENT_INCOME
  )

  private val deleteLink: Link = Link(
    href = s"/individuals/income-received/employments/other/$nino/$taxYear",
    method = DELETE,
    rel = DELETE_OTHER_EMPLOYMENT_INCOME
  )

  private val retrieveLink: Link = Link(
    href = s"/individuals/income-received/employments/other/$nino/$taxYear",
    method = GET,
    rel = SELF
  )

  trait Test {
    val hc: HeaderCarrier = HeaderCarrier()

    val controller = new RetrieveOtherEmploymentController(
      authService = mockEnrolmentsAuthService,
      lookupService = mockMtdIdLookupService,
      requestParser = mockOtherEmploymentIncomeRequestParser,
      service = mockOtherEmploymentIncomeService,
      hateoasFactory = mockHateoasFactory,
      cc = cc,
      idGenerator = mockIdGenerator
    )

    MockedMtdIdLookupService.lookup(nino).returns(Future.successful(Right("test-mtd-id")))
    MockedEnrolmentsAuthService.authoriseUser()
    MockIdGenerator.generateCorrelationId.returns(correlationId)
  }

  "RetrieveOtherEmploymentIncomeController" should {
    "return OK" when {
      "retrieve other employment income endpoint is hit" in new Test {
        MockOtherEmploymentIncomeRequestParser
          .parse(rawData)
          .returns(Right(requestData))

        MockOtherEmploymentIncomeService
          .retrieve(requestData)
          .returns(Future.successful(Right(ResponseWrapper(correlationId, retrieveOtherResponseModel))))

        MockHateoasFactory
          .wrap(retrieveOtherResponseModel, RetrieveOtherEmploymentHateoasData(nino, taxYear))
          .returns(
            HateoasWrapper(
              retrieveOtherResponseModel,
              Seq(
                amendLink,
                retrieveLink,
                deleteLink
              )))

        val result: Future[Result] = controller.retrieveOther(nino, taxYear)(fakeGetRequest)

        status(result) shouldBe OK
        contentAsJson(result) shouldBe mtdResponseWithHateoas(nino, taxYear)
        header("X-CorrelationId", result) shouldBe Some(correlationId)

      }

    }

    "return the error as per spec" when {
      "parser errors occur" must {
        def errorsFromParserTester(error: MtdError, expectedStatus: Int): Unit = {
          s"a ${error.code} error is returned from the parser" in new Test {

            MockOtherEmploymentIncomeRequestParser
              .parse(rawData)
              .returns(Left(ErrorWrapper(correlationId, error, None)))

            val result: Future[Result] = controller.retrieveOther(nino, taxYear)(fakeGetRequest)

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

            MockOtherEmploymentIncomeRequestParser
              .parse(rawData)
              .returns(Right(requestData))

            MockOtherEmploymentIncomeService
              .retrieve(requestData)
              .returns(Future.successful(Left(ErrorWrapper(correlationId, mtdError))))

            val result: Future[Result] = controller.retrieveOther(nino, taxYear)(fakeGetRequest)

            status(result) shouldBe expectedStatus
            contentAsJson(result) shouldBe Json.toJson(mtdError)
            header("X-CorrelationId", result) shouldBe Some(correlationId)
          }
        }

        val input = Seq(
          (BadRequestError, BAD_REQUEST),
          (NinoFormatError, BAD_REQUEST),
          (TaxYearFormatError, BAD_REQUEST),
          (RuleTaxYearRangeInvalidError, BAD_REQUEST),
          (RuleTaxYearNotSupportedError, BAD_REQUEST),
          (TysNotFoundError, NOT_FOUND),
          (StandardDownstreamError, INTERNAL_SERVER_ERROR)
        )

        input.foreach(args => (serviceErrors _).tupled(args))
      }
    }
  }

}
