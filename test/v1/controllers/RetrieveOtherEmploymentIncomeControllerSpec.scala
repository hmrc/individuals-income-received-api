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
import v1.fixtures.RetrieveOtherEmploymentControllerFixture._
import v1.hateoas.HateoasLinks
import v1.mocks.MockIdGenerator
import v1.mocks.hateoas.MockHateoasFactory
import v1.mocks.requestParsers.MockDeleteRetrieveRequestParser
import v1.mocks.services.{MockDeleteRetrieveService, MockEnrolmentsAuthService, MockMtdIdLookupService}
import v1.models.domain.{ShareOptionSchemeType, SharesAwardedOrReceivedSchemeType}
import v1.models.errors._
import v1.models.hateoas.Method.{DELETE, GET, PUT}
import v1.models.hateoas.RelType._
import v1.models.hateoas.{HateoasWrapper, Link}
import v1.models.outcomes.ResponseWrapper
import v1.models.request.{DeleteRetrieveRawData, DeleteRetrieveRequest}
import v1.models.response.retrieveOtherEmployment._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class RetrieveOtherEmploymentIncomeControllerSpec extends ControllerBaseSpec
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

  val rawData: DeleteRetrieveRawData = DeleteRetrieveRawData(
    nino = nino,
    taxYear = taxYear
  )

  val requestData: DeleteRetrieveRequest = DeleteRetrieveRequest(
    nino = Nino(nino),
    taxYear = taxYear
  )

  private val shareOption = ShareOptionItem(
    employerName = "Company Ltd",
    employerRef = Some("123/AB456"),
    schemePlanType = ShareOptionSchemeType.EMI,
    dateOfOptionGrant = "2019-11-20",
    dateOfEvent = "2019-12-22",
    optionNotExercisedButConsiderationReceived = true,
    amountOfConsiderationReceived = 23122.22,
    noOfSharesAcquired = 1,
    classOfSharesAcquired = "FIRST",
    exercisePrice = 12.22,
    amountPaidForOption = 123.22,
    marketValueOfSharesOnExcise = 1232.22,
    profitOnOptionExercised = 1232.33,
    employersNicPaid = 2312.22,
    taxableAmount = 2132.22
  )

  private val sharesAwardedOrReceived = SharesAwardedOrReceivedItem(
    employerName = "Company Ltd",
    employerRef = Some("123/AB456"),
    schemePlanType = SharesAwardedOrReceivedSchemeType.SIP,
    dateSharesCeasedToBeSubjectToPlan = "2019-11-10",
    noOfShareSecuritiesAwarded = 11,
    classOfShareAwarded = "FIRST",
    dateSharesAwarded = "2019-12-20",
    sharesSubjectToRestrictions = true,
    electionEnteredIgnoreRestrictions = false,
    actualMarketValueOfSharesOnAward = 2123.22,
    unrestrictedMarketValueOfSharesOnAward = 123.22,
    amountPaidForSharesOnAward = 123.22,
    marketValueAfterRestrictionsLifted = 1232.22,
    taxableAmount = 12321.22
  )

  private val commonOtherEmployment = CommonOtherEmployment(
    customerReference = Some("customer reference"),
    amountDeducted = 1223.22
  )

  private val taxableLumpSumsAndCertainIncome = TaxableLumpSumsAndCertainIncomeItem(
    amount = 5000.99,
    taxPaid = Some(3333.33),
    taxTakenOffInEmployment = true
  )

  private val benefitFromEmployerFinancedRetirementScheme = BenefitFromEmployerFinancedRetirementSchemeItem(
    amount = 5000.99,
    exemptAmount = Some(2345.99),
    taxPaid = Some(3333.33),
    taxTakenOffInEmployment = true
  )

  private val redundancyCompensationPaymentsOverExemption = RedundancyCompensationPaymentsOverExemptionItem(
    amount = 5000.99,
    taxPaid = Some(3333.33),
    taxTakenOffInEmployment = true
  )

  private val redundancyCompensationPaymentsUnderExemption = RedundancyCompensationPaymentsUnderExemptionItem(
    amount = Some(5000.99)
  )

  private val lumpSums = LumpSums(
    employerName = "BPDTS Ltd",
    employerRef = "123/AB456",
    taxableLumpSumsAndCertainIncome = Some(taxableLumpSumsAndCertainIncome),
    benefitFromEmployerFinancedRetirementScheme = Some(benefitFromEmployerFinancedRetirementScheme),
    redundancyCompensationPaymentsOverExemption = Some(redundancyCompensationPaymentsOverExemption),
    redundancyCompensationPaymentsUnderExemption = Some(redundancyCompensationPaymentsUnderExemption)
  )

  private val retrieveOtherResponseModel: RetrieveOtherEmploymentResponse = RetrieveOtherEmploymentResponse(
    submittedOn = "2020-07-06T09:37:17Z",
    shareOption = Some(Seq(shareOption)),
    sharesAwardedOrReceived = Some(Seq(sharesAwardedOrReceived)),
    disability = Some(commonOtherEmployment),
    foreignService = Some(commonOtherEmployment),
    lumpSums = Some(Seq(lumpSums))
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
    rel =  SELF
  )

  trait Test {
    val hc: HeaderCarrier = HeaderCarrier()

    val controller = new RetrieveOtherEmploymentController(
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

  "RetrieveOtherEmploymentIncomeController" should {
    "return OK" when {
      "retrieve other employment income endpoint is hit" in new Test {
        MockDeleteRetrieveRequestParser
          .parse(rawData)
          .returns(Right(requestData))

        MockDeleteRetrieveService
          .retrieve[RetrieveOtherEmploymentResponse]()
          .returns(Future.successful(Right(ResponseWrapper(correlationId, retrieveOtherResponseModel))))

        MockHateoasFactory
          .wrap(retrieveOtherResponseModel, RetrieveOtherEmploymentHateoasData(nino, taxYear))
          .returns(HateoasWrapper(retrieveOtherResponseModel,
            Seq(
              amendLink,
              retrieveLink,
              deleteLink
            )
          ))

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

            MockDeleteRetrieveRequestParser
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

            MockDeleteRetrieveRequestParser
              .parse(rawData)
              .returns(Right(requestData))

            MockDeleteRetrieveService
              .retrieve[RetrieveOtherEmploymentResponse]()
              .returns(Future.successful(Left(ErrorWrapper(correlationId, mtdError))))

            val result: Future[Result] = controller.retrieveOther(nino, taxYear)(fakeGetRequest)

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