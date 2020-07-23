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
import v1.hateoas.HateoasLinks
import v1.mocks.hateoas.MockHateoasFactory
import v1.mocks.requestParsers.MockAddCustomEmploymentRequestParser
import v1.mocks.services.{MockAddCustomEmploymentService, MockEnrolmentsAuthService, MockMtdIdLookupService}
import v1.models.errors._
import v1.models.hateoas.{HateoasWrapper, Link}
import v1.models.outcomes.ResponseWrapper
import v1.models.request.addCustomEmployment.{AddCustomEmploymentRawData, AddCustomEmploymentRequest, AddCustomEmploymentRequestBody}
import v1.models.response.addCustomEmployment.{AddCustomEmploymentHateoasData, AddCustomEmploymentResponse}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class AddCustomEmploymentControllerSpec
  extends ControllerBaseSpec
    with MockEnrolmentsAuthService
    with MockMtdIdLookupService
    with MockAppConfig
    with MockAddCustomEmploymentService
    with MockAddCustomEmploymentRequestParser
    with MockHateoasFactory
    with HateoasLinks {

  trait Test {
    val hc = HeaderCarrier()

    val controller = new AddCustomEmploymentController(
      authService = mockEnrolmentsAuthService,
      lookupService = mockMtdIdLookupService,
      requestParser = mockAddCustomEmploymentRequestParser,
      service = mockAddCustomEmploymentService,
      hateoasFactory = mockHateoasFactory,
      cc = cc
    )

    MockedMtdIdLookupService.lookup(nino).returns(Future.successful(Right("test-mtd-id")))
    MockedEnrolmentsAuthService.authoriseUser()
    MockedAppConfig.apiGatewayContext.returns("baseUrl").anyNumberOfTimes()

    val links: List[Link] = List(
      listEmployment(mockAppConfig, nino, taxYear, isSelf = false),
      retrieveEmployment(mockAppConfig, nino, taxYear, employmentId, isSelf = true),
      amendCustomEmployment(mockAppConfig, nino, taxYear, employmentId),
      deleteCustomEmployment(mockAppConfig, nino, taxYear, employmentId)
    )
  }

  val nino: String = "AA123456A"
  val taxYear: String = "2017-18"
  val employmentId: String = "4557ecb5-fd32-48cc-81f5-e6acd1099f3c"
  val correlationId: String = "X-123"

  val requestBodyJson: JsValue = Json.parse(
    """
      |{
      |  "employerRef": "123/AZ12334",
      |  "employerName": "AMD infotech Ltd",
      |  "startDate": "2019-01-01",
      |  "cessationDate": "2020-06-01",
      |  "payrollId": "124214112412"
      |}
    """.stripMargin
  )

  val rawData: AddCustomEmploymentRawData = AddCustomEmploymentRawData(
    nino = nino,
    taxYear = taxYear,
    body = AnyContentAsJson(requestBodyJson)
  )

  val addCustomEmploymentRequestBody: AddCustomEmploymentRequestBody = AddCustomEmploymentRequestBody(
    employerRef = Some("123/AZ12334"),
    employerName = "AMD infotech Ltd",
    startDate = "2019-01-01",
    cessationDate = Some("2020-06-01"),
    payrollId = Some("124214112412")
  )

  val requestData: AddCustomEmploymentRequest = AddCustomEmploymentRequest(
    nino = Nino(nino),
    taxYear = taxYear,
    body = addCustomEmploymentRequestBody
  )

  val responseData: AddCustomEmploymentResponse = AddCustomEmploymentResponse(
    employmentId = "4557ecb5-fd32-48cc-81f5-e6acd1099f3c"
  )

  val responseJson: JsValue = Json.parse(
    s"""
      |{
      |   "employmentId": "$employmentId",
      |   "links":[
      |      {
      |         "href": "/baseUrl/employments/$nino/$taxYear",
      |         "rel": "list-employments",
      |         "method": "GET"
      |      },
      |      {
      |         "href": "/baseUrl/employments/$nino/$taxYear/$employmentId",
      |         "rel": "self",
      |         "method": "GET"
      |      },
      |      {
      |         "href": "/baseUrl/employments/$nino/$taxYear/$employmentId",
      |         "rel": "amend-custom-employment",
      |         "method": "PUT"
      |      },
      |      {
      |         "href": "/baseUrl/employments/$nino/$taxYear/$employmentId",
      |         "rel": "delete-custom-employment",
      |         "method": "DELETE"
      |      }
      |   ]
      |}
    """.stripMargin
  )

  "AddCustomEmploymentController" should {
    "return OK" when {
      "happy path" in new Test {

        MockAddCustomEmploymentRequestParser
          .parse(rawData)
          .returns(Right(requestData))

        MockAddCustomEmploymentService
          .addEmployment(requestData)
          .returns(Future.successful(Right(ResponseWrapper(correlationId, responseData))))

        MockHateoasFactory
          .wrap(responseData, AddCustomEmploymentHateoasData(nino, taxYear, employmentId))
          .returns(HateoasWrapper(responseData, links))

        val result: Future[Result] = controller.addEmployment(nino, taxYear)(fakePostRequest(requestBodyJson))

        status(result) shouldBe OK
        contentAsJson(result) shouldBe responseJson
        header("X-CorrelationId", result) shouldBe Some(correlationId)
      }
    }

    "return the error as per spec" when {
      "parser errors occur" must {
        def errorsFromParserTester(error: MtdError, expectedStatus: Int): Unit = {
          s"a ${error.code} error is returned from the parser" in new Test {

            MockAddCustomEmploymentRequestParser
              .parse(rawData)
              .returns(Left(ErrorWrapper(Some(correlationId), error, None)))

            val result: Future[Result] = controller.addEmployment(nino, taxYear)(fakePostRequest(requestBodyJson))

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
          (RuleIncorrectOrEmptyBodyError, BAD_REQUEST),
          (RuleTaxYearNotSupportedError, BAD_REQUEST),
          (RuleTaxYearNotEndedError, BAD_REQUEST),
          (EmployerRefFormatError, BAD_REQUEST),
          (EmployerNameFormatError, BAD_REQUEST),
          (PayrollIdFormatError, BAD_REQUEST),
          (StartDateFormatError, BAD_REQUEST),
          (CessationDateFormatError, BAD_REQUEST),
          (RuleStartDateAfterTaxYearEndError, BAD_REQUEST),
          (RuleCessationDateBeforeTaxYearStartError, BAD_REQUEST),
          (RuleCessationDateBeforeStartDateError, BAD_REQUEST)
        )

        input.foreach(args => (errorsFromParserTester _).tupled(args))
      }

      "service errors occur" must {
        def serviceErrors(mtdError: MtdError, expectedStatus: Int): Unit = {
          s"a $mtdError error is returned from the service" in new Test {

            MockAddCustomEmploymentRequestParser
              .parse(rawData)
              .returns(Right(requestData))

            MockAddCustomEmploymentService
              .addEmployment(requestData)
              .returns(Future.successful(Left(ErrorWrapper(Some(correlationId), mtdError))))

            val result: Future[Result] = controller.addEmployment(nino, taxYear)(fakePostRequest(requestBodyJson))

            status(result) shouldBe expectedStatus
            contentAsJson(result) shouldBe Json.toJson(mtdError)
            header("X-CorrelationId", result) shouldBe Some(correlationId)
          }
        }

        val input = Seq(
          (NinoFormatError, BAD_REQUEST),
          (TaxYearFormatError, BAD_REQUEST),
          (RuleTaxYearNotEndedError, BAD_REQUEST),
          (NotFoundError, NOT_FOUND),
          (DownstreamError, INTERNAL_SERVER_ERROR)
        )

        input.foreach(args => (serviceErrors _).tupled(args))
      }
    }
  }
}