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

import mocks.MockAppConfig
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.{AnyContentAsJson, Result}
import uk.gov.hmrc.domain.Nino
import uk.gov.hmrc.http.HeaderCarrier
import v1.mocks.requestParsers.MockAmendOtherEmploymentRequestParser
import v1.mocks.services.{MockAmendOtherEmploymentService, MockEnrolmentsAuthService, MockMtdIdLookupService}
import v1.models.domain.DesTaxYear
import v1.models.errors._
import v1.models.outcomes.ResponseWrapper
import v1.models.request.amendOtherEmployment._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class AmendOtherEmploymentControllerSpec
  extends ControllerBaseSpec
    with MockEnrolmentsAuthService
    with MockMtdIdLookupService
    with MockAppConfig
    with MockAmendOtherEmploymentRequestParser
    with MockAmendOtherEmploymentService {

  trait Test {
    val hc = HeaderCarrier()

    val controller = new AmendOtherEmploymentController(
      authService = mockEnrolmentsAuthService,
      lookupService = mockMtdIdLookupService,
      appConfig = mockAppConfig,
      requestParser = mockAmendOtherEmploymentRequestParser,
      service = mockAmendOtherEmploymentService,
      cc = cc
    )

    MockedMtdIdLookupService.lookup(nino).returns(Future.successful(Right("test-mtd-id")))
    MockedEnrolmentsAuthService.authoriseUser()
    MockedAppConfig.apiGatewayContext.returns("baseUrl").anyNumberOfTimes()
  }

  val nino: String = "AA123456A"
  val taxYear: String = "2019-20"
  val correlationId: String = "X-123"

  private val requestBodyJson: JsValue = Json.parse(
    """
      |{
      |  "shareOption": [
      |      {
      |        "employerName": "Company Ltd",
      |        "employerRef" : "123/AB456",
      |        "schemePlanType": "EMI",
      |        "dateOfOptionGrant": "2019-11-20",
      |        "dateOfEvent": "2019-11-20",
      |        "optionNotExercisedButConsiderationReceived": true,
      |        "amountOfConsiderationReceived": 23122.22,
      |        "noOfSharesAcquired": 1,
      |        "classOfSharesAcquired": "FIRST",
      |        "exercisePrice": 12.22,
      |        "amountPaidForOption": 123.22,
      |        "marketValueOfSharesOnExcise": 1232.22,
      |        "profitOnOptionExercised": 1232.33,
      |        "employersNicPaid": 2312.22,
      |        "taxableAmount" : 2132.22
      |      },
      |      {
      |        "employerName": "SecondCom Ltd",
      |        "employerRef" : "123/AB456",
      |        "schemePlanType": "CSOP",
      |        "dateOfOptionGrant": "2020-09-12",
      |        "dateOfEvent": "2020-09-12",
      |        "optionNotExercisedButConsiderationReceived": false,
      |        "amountOfConsiderationReceived": 5000.99,
      |        "noOfSharesAcquired": 200,
      |        "classOfSharesAcquired": "Ordinary shares",
      |        "exercisePrice": 1000.99,
      |        "amountPaidForOption": 5000.99,
      |        "marketValueOfSharesOnExcise": 5500.99,
      |        "profitOnOptionExercised": 3333.33,
      |        "employersNicPaid": 2000.22,
      |        "taxableAmount" : 2555.55
      |      }
      |  ],
      |  "sharesAwardedOrReceived": [
      |       {
      |        "employerName": "Company Ltd",
      |        "employerRef" : "123/AB456",
      |        "schemePlanType": "SIP",
      |        "dateSharesCeasedToBeSubjectToPlan": "2019-11-10",
      |        "noOfShareSecuritiesAwarded": 11,
      |        "classOfShareAwarded": "FIRST",
      |        "dateSharesAwarded" : "2019-11-20",
      |        "sharesSubjectToRestrictions": true,
      |        "electionEnteredIgnoreRestrictions": false,
      |        "actualMarketValueOfSharesOnAward": 2123.22,
      |        "unrestrictedMarketValueOfSharesOnAward": 123.22,
      |        "amountPaidForSharesOnAward": 123.22,
      |        "marketValueAfterRestrictionsLifted": 1232.22,
      |        "taxableAmount": 12321.22
      |       },
      |       {
      |        "employerName": "SecondCom Ltd",
      |        "employerRef" : "123/AB456",
      |        "schemePlanType": "Other",
      |        "dateSharesCeasedToBeSubjectToPlan": "2020-09-12",
      |        "noOfShareSecuritiesAwarded": 299,
      |        "classOfShareAwarded": "Ordinary shares",
      |        "dateSharesAwarded" : "2020-09-12",
      |        "sharesSubjectToRestrictions": false,
      |        "electionEnteredIgnoreRestrictions": true,
      |        "actualMarketValueOfSharesOnAward": 5000.99,
      |        "unrestrictedMarketValueOfSharesOnAward": 5432.21,
      |        "amountPaidForSharesOnAward": 6000.99,
      |        "marketValueAfterRestrictionsLifted": 3333.33,
      |        "taxableAmount": 98765.99
      |       }
      |  ],
      |  "disability":
      |    {
      |      "customerReference": "OTHEREmp123A",
      |      "amountDeducted": 5000.99
      |    },
      |  "foreignService":
      |    {
      |      "customerReference": "OTHEREmp999A",
      |      "amountDeducted": 7000.99
      |    }
      |}
    """.stripMargin
  )

  val rawData: AmendOtherEmploymentRawData = AmendOtherEmploymentRawData(
    nino = nino,
    taxYear = taxYear,
    body = AnyContentAsJson(requestBodyJson)
  )

  val shareOptionItem: Seq[AmendShareOptionItem] = Seq(
    AmendShareOptionItem(
      employerName = "Company Ltd",
      employerRef = Some("123/AB456"),
      schemePlanType = "EMI",
      dateOfOptionGrant = "2019-11-20",
      dateOfEvent = "2019-11-20",
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
    ),
    AmendShareOptionItem(
      employerName = "SecondCom Ltd",
      employerRef = Some("123/AB456"),
      schemePlanType = "CSOP",
      dateOfOptionGrant = "2020-09-12",
      dateOfEvent = "2020-09-12",
      optionNotExercisedButConsiderationReceived = false,
      amountOfConsiderationReceived = 5000.99,
      noOfSharesAcquired = 200,
      classOfSharesAcquired = "Ordinary shares",
      exercisePrice = 1000.99,
      amountPaidForOption = 5000.99,
      marketValueOfSharesOnExcise = 5500.99,
      profitOnOptionExercised = 3333.33,
      employersNicPaid = 2000.22,
      taxableAmount = 2555.55
    )
  )

  val sharesAwardedOrReceivedItem: Seq[AmendSharesAwardedOrReceivedItem] = Seq(
    AmendSharesAwardedOrReceivedItem(
      employerName = "Company Ltd",
      employerRef = Some("123/AB456"),
      schemePlanType = "SIP",
      dateSharesCeasedToBeSubjectToPlan = "2019-11-10",
      noOfShareSecuritiesAwarded = 11,
      classOfShareAwarded = "FIRST",
      dateSharesAwarded = "2019-11-20",
      sharesSubjectToRestrictions = true,
      electionEnteredIgnoreRestrictions = false,
      actualMarketValueOfSharesOnAward = 2123.22,
      unrestrictedMarketValueOfSharesOnAward = 123.22,
      amountPaidForSharesOnAward = 123.22,
      marketValueAfterRestrictionsLifted = 1232.22,
      taxableAmount = 12321.22
    ),
    AmendSharesAwardedOrReceivedItem(
      employerName = "SecondCom Ltd",
      employerRef = Some("123/AB456"),
      schemePlanType = "Other",
      dateSharesCeasedToBeSubjectToPlan = "2020-09-12",
      noOfShareSecuritiesAwarded = 299,
      classOfShareAwarded = "Ordinary shares",
      dateSharesAwarded = "2020-09-12",
      sharesSubjectToRestrictions = false,
      electionEnteredIgnoreRestrictions = true,
      actualMarketValueOfSharesOnAward = 5000.99,
      unrestrictedMarketValueOfSharesOnAward = 5432.21,
      amountPaidForSharesOnAward = 6000.99,
      marketValueAfterRestrictionsLifted = 3333.33,
      taxableAmount = 98765.99
    )
  )

  val disability: AmendCommonOtherEmployment =
    AmendCommonOtherEmployment(
      customerReference = Some("OTHEREmp123A"),
      amountDeducted = 5000.99
    )

  val foreignService: AmendCommonOtherEmployment =
    AmendCommonOtherEmployment(
      customerReference = Some("OTHEREmp999A"),
      amountDeducted = 7000.99
    )

  val amendOtherEmploymentRequestBody: AmendOtherEmploymentRequestBody = AmendOtherEmploymentRequestBody(
    shareOption = Some(shareOptionItem),
    sharesAwardedOrReceived = Some(sharesAwardedOrReceivedItem),
    disability = Some(disability),
    foreignService = Some(foreignService)
  )

  val requestData: AmendOtherEmploymentRequest = AmendOtherEmploymentRequest(
    nino = Nino(nino),
    taxYear = DesTaxYear.fromMtd(taxYear),
    body = amendOtherEmploymentRequestBody
  )

  val hateoasResponse: JsValue = Json.parse(
    s"""
       |{
       |   "links":[
       |      {
       |         "href":"/baseUrl/employments/other/$nino/$taxYear",
       |         "rel":"amend-employments-other-income",
       |         "method":"PUT"
       |      },
       |      {
       |         "href":"/baseUrl/employments/other/$nino/$taxYear",
       |         "rel":"self",
       |         "method":"GET"
       |      },
       |      {
       |         "href":"/baseUrl/employments/other/$nino/$taxYear",
       |         "rel":"delete-employments-other-income",
       |         "method":"DELETE"
       |      }
       |   ]
       |}
    """.stripMargin
  )

  "AmendOtherEmploymentController" should {
    "return OK" when {
      "happy path" in new Test {

        MockAmendOtherEmploymentRequestParser
          .parse(rawData)
          .returns(Right(requestData))

        MockAmendOtherEmploymentService
          .amend(requestData)
          .returns(Future.successful(Right(ResponseWrapper(correlationId, ()))))

        val result: Future[Result] = controller.amendOtherEmployment(nino, taxYear)(fakePutRequest(requestBodyJson))

        status(result) shouldBe OK
        contentAsJson(result) shouldBe hateoasResponse
        header("X-CorrelationId", result) shouldBe Some(correlationId)
      }
    }

    "return the error as per spec" when {
      "parser errors occur" must {
        def errorsFromParserTester(error: MtdError, expectedStatus: Int): Unit = {
          s"a ${error.code} error is returned from the parser" in new Test {

            MockAmendOtherEmploymentRequestParser
              .parse(rawData)
              .returns(Left(ErrorWrapper(Some(correlationId), error, None)))

            val result: Future[Result] = controller.amendOtherEmployment(nino, taxYear)(fakePutRequest(requestBodyJson))

            status(result) shouldBe expectedStatus
            contentAsJson(result) shouldBe Json.toJson(error)
            header("X-CorrelationId", result) shouldBe Some(correlationId)
          }
        }

        val input = Seq(
          (BadRequestError, BAD_REQUEST),
          (NinoFormatError, BAD_REQUEST),
          (TaxYearFormatError, BAD_REQUEST),
          (RuleTaxYearRangeInvalidError, BAD_REQUEST),
          (RuleTaxYearNotSupportedError, BAD_REQUEST),
          (RuleIncorrectOrEmptyBodyError, BAD_REQUEST),
          (EmployerRefFormatError, BAD_REQUEST),
          (EmployerNameFormatError, BAD_REQUEST),
          (DateFormatError, BAD_REQUEST),
          (ClassOfSharesAwardedFormatError, BAD_REQUEST),
          (ClassOfSharesAcquiredFormatError, BAD_REQUEST),
          (SchemePlanTypeFormatError, BAD_REQUEST),
          (ValueFormatError, BAD_REQUEST)
        )

        input.foreach(args => (errorsFromParserTester _).tupled(args))
      }

      "service errors occur" must {
        def serviceErrors(mtdError: MtdError, expectedStatus: Int): Unit = {
          s"a $mtdError error is returned from the service" in new Test {

            MockAmendOtherEmploymentRequestParser
              .parse(rawData)
              .returns(Right(requestData))

            MockAmendOtherEmploymentService
              .amend(requestData)
              .returns(Future.successful(Left(ErrorWrapper(Some(correlationId), mtdError))))

            val result: Future[Result] = controller.amendOtherEmployment(nino, taxYear)(fakePutRequest(requestBodyJson))

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