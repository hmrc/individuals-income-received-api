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
import v1.fixtures.ListEmploymentsControllerFixture._
import v1.hateoas.HateoasLinks
import v1.mocks.MockIdGenerator
import v1.mocks.hateoas.MockHateoasFactory
import v1.mocks.requestParsers.MockListEmploymentsRequestParser
import v1.mocks.services.{MockEnrolmentsAuthService, MockListEmploymentsService, MockMtdIdLookupService}
import v1.models.errors._
import v1.models.hateoas.Method.{GET, POST}
import v1.models.hateoas.RelType.{ADD_CUSTOM_EMPLOYMENT, SELF}
import v1.models.hateoas.{HateoasWrapper, Link}
import v1.models.outcomes.ResponseWrapper
import v1.models.request.listEmployments.{ListEmploymentsRawData, ListEmploymentsRequest}
import v1.models.response.listEmployment.{Employment, ListEmploymentHateoasData, ListEmploymentResponse}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class ListEmploymentsControllerSpec extends ControllerBaseSpec
  with MockEnrolmentsAuthService
  with MockMtdIdLookupService
  with MockListEmploymentsService
  with MockHateoasFactory
  with MockListEmploymentsRequestParser
  with HateoasLinks
  with MockIdGenerator {

  val nino: String = "AA123456A"
  val taxYear: String = "2019-20"
  val employmentId: String = "4557ecb5-fd32-48cc-81f5-e6acd1099f3c"
  val correlationId: String = "X-123"

  val rawData: ListEmploymentsRawData = ListEmploymentsRawData(
    nino = nino,
    taxYear = taxYear
  )

  val requestData: ListEmploymentsRequest = ListEmploymentsRequest(
    nino = Nino(nino),
    taxYear = taxYear
  )

  val retrieveEmploymentLink: Link =
    Link(
      href = s"/individuals/income-received/employments/$nino/$taxYear/$employmentId",
      method = GET,
      rel = SELF
    )

  val addCustomEmploymentLink: Link =
    Link(
      href = s"/individuals/income-received/employments/$nino/$taxYear",
      method = POST,
      rel = ADD_CUSTOM_EMPLOYMENT
    )

  val listEmploymentsLink: Link =
    Link(
      href = s"/individuals/income-received/employments/$nino/$taxYear",
      method = GET,
      rel = SELF
    )

  private val hmrcEmploymentModel = Employment(
    employmentId = "4557ecb5-fd32-48cc-81f5-e6acd1099f3c",
    employerName = "Vera Lynn",
    dateIgnored = Some("2020-06-17T10:53:38Z")
  )

  private val customEmploymentModel = Employment(
    employmentId = "4557ecb5-fd32-48cc-81f5-e6acd1099f3c",
    employerName = "Vera Lynn"
  )

  private val listEmploymentsResponseModel = ListEmploymentResponse(
    Some(Seq(hmrcEmploymentModel,hmrcEmploymentModel)),
    Some(Seq(customEmploymentModel,customEmploymentModel))
  )

  private val hateoasResponse = ListEmploymentResponse(
    Some(Seq(
      HateoasWrapper(
        hmrcEmploymentModel,
        Seq(retrieveEmploymentLink)
      ),
      HateoasWrapper(
        hmrcEmploymentModel,
        Seq(retrieveEmploymentLink)
      )
    )),
    Some(Seq(
      HateoasWrapper(
        customEmploymentModel,
        Seq(retrieveEmploymentLink)
      ),
      HateoasWrapper(
        customEmploymentModel,
        Seq(retrieveEmploymentLink)
      )
    ))
  )

  private val mtdResponse = mtdResponseWithCustomHateoas(nino, taxYear, employmentId)

  trait Test {
    val hc: HeaderCarrier = HeaderCarrier()

    val controller = new ListEmploymentsController(
      authService = mockEnrolmentsAuthService,
      lookupService = mockMtdIdLookupService,
      requestParser = mockListEmploymentsRequestParser,
      service = mockListEmploymentsService,
      hateoasFactory = mockHateoasFactory,
      cc = cc,
      idGenerator = mockIdGenerator
    )

    MockedMtdIdLookupService.lookup(nino).returns(Future.successful(Right("test-mtd-id")))
    MockedEnrolmentsAuthService.authoriseUser()
    MockIdGenerator.generateCorrelationId.returns(correlationId)
  }

  "ListEmploymentsController" should {
    "return OK" when {
      "happy path" in new Test {

        MockListEmploymentsRequestParser
          .parse(rawData)
          .returns(Right(requestData))

        MockListEmploymentsService
          .listEmployments(requestData)
          .returns(Future.successful(Right(ResponseWrapper(correlationId, listEmploymentsResponseModel))))

        MockHateoasFactory
          .wrapList(listEmploymentsResponseModel, ListEmploymentHateoasData(nino, taxYear))
          .returns(HateoasWrapper(hateoasResponse,
            Seq(
              addCustomEmploymentLink,
              listEmploymentsLink
            )
          ))

        val result: Future[Result] = controller.listEmployments(nino, taxYear)(fakeGetRequest)

        status(result) shouldBe OK
        contentAsJson(result) shouldBe mtdResponse
        header("X-CorrelationId", result) shouldBe Some(correlationId)
      }
    }

    "return the error as per spec" when {
      "parser errors occur" must {
        def errorsFromParserTester(error: MtdError, expectedStatus: Int): Unit = {
          s"a ${error.code} error is returned from the parser" in new Test {

            MockListEmploymentsRequestParser
              .parse(rawData)
              .returns(Left(ErrorWrapper(correlationId, error, None)))

            val result: Future[Result] = controller.listEmployments(nino, taxYear)(fakeGetRequest)

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

            MockListEmploymentsRequestParser
              .parse(rawData)
              .returns(Right(requestData))

            MockListEmploymentsService
              .listEmployments(requestData)
              .returns(Future.successful(Left(ErrorWrapper(correlationId, mtdError))))

            val result: Future[Result] = controller.listEmployments(nino, taxYear)(fakeGetRequest)

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