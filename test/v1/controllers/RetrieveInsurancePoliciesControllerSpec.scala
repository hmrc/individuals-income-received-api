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
import v1.fixtures.RetrieveInsurancePoliciesControllerFixture
import v1.hateoas.HateoasLinks
import v1.mocks.MockIdGenerator
import v1.mocks.hateoas.MockHateoasFactory
import v1.mocks.requestParsers.MockDeleteRetrieveRequestParser
import v1.mocks.services.{MockDeleteRetrieveService, MockEnrolmentsAuthService, MockMtdIdLookupService}
import v1.models.errors._
import v1.models.hateoas.Method.{DELETE, GET, PUT}
import v1.models.hateoas.RelType._
import v1.models.hateoas.{HateoasWrapper, Link}
import v1.models.outcomes.ResponseWrapper
import v1.models.request.{DeleteRetrieveRawData, DeleteRetrieveRequest}
import v1.models.response.retrieveInsurancePolicies._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class RetrieveInsurancePoliciesControllerSpec extends ControllerBaseSpec
  with MockEnrolmentsAuthService
  with MockMtdIdLookupService
  with MockDeleteRetrieveService
  with MockHateoasFactory
  with MockDeleteRetrieveRequestParser
  with HateoasLinks
  with MockIdGenerator {

  val nino: String = "AA123456A"
  val taxYear: String = "2019-20"
  val correlationId: String = "X-123"

  private val rawData = DeleteRetrieveRawData(
    nino = nino,
    taxYear = taxYear
  )

  private val requestData: DeleteRetrieveRequest = DeleteRetrieveRequest(
    nino = Nino(nino),
    taxYear = taxYear
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

  private val lifeInsuranceItemModel = CommonInsurancePoliciesItem(
    customerReference = Some("INPOLY123A"),
    event = Some("Death of spouse"),
    gainAmount = 1.23,
    taxPaid = true,
    yearsHeld = Some(2),
    yearsHeldSinceLastGain = Some(1),
    deficiencyRelief = Some(1.23)
  )

  private val capitalRedemptionItemModel = CommonInsurancePoliciesItem(
    customerReference = Some("INPOLY123B"),
    event = Some("Death of spouse"),
    gainAmount = 1.24,
    taxPaid = true,
    yearsHeld = Some(3),
    yearsHeldSinceLastGain = Some(2),
    deficiencyRelief = Some(1.23)
  )

  private val lifeAnnuityItemModel = CommonInsurancePoliciesItem(
    customerReference = Some("INPOLY123C"),
    event = Some("Death of spouse"),
    gainAmount = 1.25,
    taxPaid = true,
    yearsHeld = Some(4),
    yearsHeldSinceLastGain = Some(3),
    deficiencyRelief = Some(1.23)
  )

  private val voidedIsaItemModel = VoidedIsaPoliciesItem(
    customerReference = Some("INPOLY123D"),
    event = Some("Death of spouse"),
    gainAmount = 1.26,
    taxPaidAmount = Some(1.36),
    yearsHeld = Some(5),
    yearsHeldSinceLastGain = Some(4)
  )

  private val foreignItemModel = ForeignPoliciesItem(
    customerReference = Some("INPOLY123E"),
    gainAmount = 1.27,
    taxPaidAmount = Some(1.37),
    yearsHeld = Some(6)
  )

  private val retrieveInsurancePoliciesResponseModel = RetrieveInsurancePoliciesResponse(
    submittedOn = "2020-07-06T09:37:17Z",
    lifeInsurance = Some(Seq(lifeInsuranceItemModel)),
    capitalRedemption = Some(Seq(capitalRedemptionItemModel)),
    lifeAnnuity = Some(Seq(lifeAnnuityItemModel)),
    voidedIsa = Some(Seq(voidedIsaItemModel)),
    foreign = Some(Seq(foreignItemModel))
  )

  private val mtdResponse = RetrieveInsurancePoliciesControllerFixture.mtdResponseWithHateoas(nino, taxYear)

  trait Test {
    val hc: HeaderCarrier = HeaderCarrier()

    val controller = new RetrieveInsurancePoliciesController(
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

  "RetrieveInsurancePoliciesController" should {
    "return OK" when {
      "happy path" in new Test {

        MockDeleteRetrieveRequestParser
          .parse(rawData)
          .returns(Right(requestData))

        MockDeleteRetrieveService
          .retrieve[RetrieveInsurancePoliciesResponse]()
          .returns(Future.successful(Right(ResponseWrapper(correlationId, retrieveInsurancePoliciesResponseModel))))

        MockHateoasFactory
          .wrap(retrieveInsurancePoliciesResponseModel, RetrieveInsurancePoliciesHateoasData(nino, taxYear))
          .returns(HateoasWrapper(retrieveInsurancePoliciesResponseModel,
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
              .returns(Left(ErrorWrapper(correlationId, error, None)))

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
              .retrieve[RetrieveInsurancePoliciesResponse]()
              .returns(Future.successful(Left(ErrorWrapper(correlationId, mtdError))))

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