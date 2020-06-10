/*
 * Copyright 2020 HM Revenue & Customs
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
import v1.fixtures.insurancePolicies.RetrieveInsurancePoliciesFixture
import v1.hateoas.HateoasLinks
import v1.mocks.hateoas.MockHateoasFactory
import v1.mocks.requestParsers.MockDeleteRetrieveRequestParser
import v1.mocks.services.{MockDeleteRetrieveService, MockEnrolmentsAuthService, MockMtdIdLookupService}
import v1.models.domain.DesTaxYear
import v1.models.errors._
import v1.models.hateoas.{HateoasWrapper, Link}
import v1.models.hateoas.Method.{DELETE, GET, PUT}
import v1.models.hateoas.RelType.{AMEND_INSURANCE_POLICIES_INCOME, DELETE_INSURANCE_POLICIES_INCOME, SELF}
import v1.models.outcomes.ResponseWrapper
import v1.models.request.{DeleteRetrieveRawData, DeleteRetrieveRequest}
import v1.models.response.retrieveInsurancePolicies.{RetrieveInsurancePoliciesHateoasData, RetrieveInsurancePoliciesResponse}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class RetrieveInsurancePoliciesControllerSpec extends ControllerBaseSpec
  with MockEnrolmentsAuthService
  with MockMtdIdLookupService
  with MockDeleteRetrieveService
  with MockHateoasFactory
  with MockDeleteRetrieveRequestParser
  with HateoasLinks {

  val nino: String = "AA123456A"
  val taxYear: String = "2017-18"
  val correlationId: String = "X-123"

  private val rawData = DeleteRetrieveRawData(
    nino = nino,
    taxYear = taxYear
  )

  private val requestData: DeleteRetrieveRequest = DeleteRetrieveRequest(
    nino = Nino(nino),
    taxYear = DesTaxYear.fromMtd(taxYear)
  )

  private val amendInsurancePoliciesLink: Link =
    Link(
      href = s"/individuals/income-received/insurance-policies/$nino/$taxYear",
      method = PUT,
      rel = AMEND_INSURANCE_POLICIES_INCOME
    )

  private val retrieveInsurancePoliciesLink: Link =
    Link(
      href = s"/individuals/income-received/insurance-policies/$nino/$taxYear",
      method = GET,
      rel = SELF
    )

  private val deleteInsurancePoliciesLink: Link =
    Link(
      href = s"/individuals/income-received/insurance-policies/$nino/$taxYear",
      method = DELETE,
      rel = DELETE_INSURANCE_POLICIES_INCOME
    )

  private val retrieveInsurancePoliciesResponse = RetrieveInsurancePoliciesFixture.retrieveInsurancePoliciesResponseModel
  private val mtdResponse = RetrieveInsurancePoliciesFixture.mtdResponseWithHateoas(nino, taxYear)

  trait Test {
    val hc = HeaderCarrier()

    val controller = new RetrieveInsurancePoliciesController(
      authService = mockEnrolmentsAuthService,
      lookupService = mockMtdIdLookupService,
      requestParser = mockDeleteRetrieveRequestParser,
      service = mockDeleteRetrieveService,
      hateoasFactory = mockHateoasFactory,
      cc = cc
    )

    MockedMtdIdLookupService.lookup(nino).returns(Future.successful(Right("test-mtd-id")))
    MockedEnrolmentsAuthService.authoriseUser()
  }

  "RetrieveInsurancePoliciesController" should {
    "return OK" when {
      "happy path" in new Test {

        MockDeleteRetrieveRequestParser
          .parse(rawData)
          .returns(Right(requestData))

        MockDeleteRetrieveService
          .retrieve[RetrieveInsurancePoliciesResponse](requestData)
          .returns(Future.successful(Right(ResponseWrapper(correlationId, retrieveInsurancePoliciesResponse))))

        MockHateoasFactory
          .wrap(retrieveInsurancePoliciesResponse, RetrieveInsurancePoliciesHateoasData(nino, taxYear))
          .returns(HateoasWrapper(retrieveInsurancePoliciesResponse,
            Seq(
              amendInsurancePoliciesLink,
              retrieveInsurancePoliciesLink,
              deleteInsurancePoliciesLink
            )
          ))

        val result: Future[Result] = controller.retrieveInsurancePolicies(nino, taxYear)(fakeGetRequest)

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
              .returns(Left(ErrorWrapper(Some(correlationId), error, None)))

            val result: Future[Result] = controller.retrieveInsurancePolicies(nino, taxYear)(fakeGetRequest)

            status(result) shouldBe expectedStatus
            contentAsJson(result) shouldBe Json.toJson(error)
            header("X-CorrelationId", result) shouldBe Some(correlationId)
          }
        }

        val input = Seq(
          (BadRequestError, BAD_REQUEST),
          (NinoFormatError, BAD_REQUEST),
          (TaxYearFormatError, BAD_REQUEST),
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
              .retrieve[RetrieveInsurancePoliciesResponse](requestData)
              .returns(Future.successful(Left(ErrorWrapper(Some(correlationId), mtdError))))

            val result: Future[Result] = controller.retrieveInsurancePolicies(nino, taxYear)(fakeGetRequest)

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
