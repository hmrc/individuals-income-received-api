/*
 * Copyright 2023 HM Revenue & Customs
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

import api.controllers.{ControllerBaseSpec, ControllerTestRunner}
import api.hateoas.HateoasLinks
import api.mocks.hateoas.MockHateoasFactory
import api.models.domain.{Nino, Timestamp}
import api.models.errors._
import api.models.hateoas.Method.{DELETE, GET, POST, PUT}
import api.models.hateoas.RelType._
import api.models.hateoas.{HateoasWrapper, Link}
import api.models.outcomes.ResponseWrapper
import play.api.mvc.Result
import v1.fixtures.RetrieveEmploymentControllerFixture._
import v1.mocks.requestParsers.MockRetrieveEmploymentRequestParser
import v1.mocks.services.MockRetrieveEmploymentService
import v1.models.request.retrieveEmployment.{RetrieveEmploymentRawData, RetrieveEmploymentRequest}
import v1.models.response.retrieveEmployment.{RetrieveEmploymentHateoasData, RetrieveEmploymentResponse}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class RetrieveEmploymentControllerSpec
    extends ControllerBaseSpec
    with ControllerTestRunner
    with MockRetrieveEmploymentService
    with MockHateoasFactory
    with MockRetrieveEmploymentRequestParser
    with HateoasLinks {

  val taxYear: String      = "2019-20"
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
      method = POST,
      rel = IGNORE_EMPLOYMENT
    )

  private val unignoreEmploymentLink: Link =
    Link(
      href = s"/individuals/income-received/employments/$nino/$taxYear/$employmentId/unignore",
      method = POST,
      rel = UNIGNORE_EMPLOYMENT
    )

  private val hmrcEnteredEmploymentWithoutDateIgnoredResponseModel = RetrieveEmploymentResponse(
    employerRef = Some("123/AB56797"),
    employerName = "Employer Name Ltd.",
    startDate = Some("2020-06-17"),
    cessationDate = Some("2020-06-17"),
    payrollId = Some("123345657"),
    occupationalPension = Some(false),
    dateIgnored = None,
    submittedOn = None
  )

  private val hmrcEnteredEmploymentWithDateIgnoredResponseModel = RetrieveEmploymentResponse(
    employerRef = Some("123/AB56797"),
    employerName = "Employer Name Ltd.",
    startDate = Some("2020-06-17"),
    cessationDate = Some("2020-06-17"),
    payrollId = Some("123345657"),
    occupationalPension = Some(false),
    dateIgnored = Some(Timestamp("2020-06-17T10:53:38.000Z")),
    submittedOn = None
  )

  private val customEnteredEmploymentResponseModel = RetrieveEmploymentResponse(
    employerRef = Some("123/AB56797"),
    employerName = "Employer Name Ltd.",
    startDate = Some("2020-06-17"),
    cessationDate = Some("2020-06-17"),
    payrollId = Some("123345657"),
    occupationalPension = Some(false),
    dateIgnored = None,
    submittedOn = Some(Timestamp("2020-06-17T10:53:38.000Z"))
  )

  private val mtdHmrcEnteredResponseWithoutDateIgnored = mtdHmrcEnteredResponseWithHateoasAndNoDateIgnored(nino, taxYear, employmentId)

  private val mtdHmrcEnteredResponseWithDateIgnored = mtdHmrcEnteredResponseWithHateoasAndDateIgnored(nino, taxYear, employmentId)

  private val mtdCustomEnteredResponse = mtdCustomEnteredResponseWithHateoas(nino, taxYear, employmentId)

  "RetrieveEmploymentController" should {
    "return OK" when {
      "happy path for retrieving hmrc entered employment with no date ignored present" in new Test {
        MockRetrieveCustomEmploymentRequestParser
          .parse(rawData)
          .returns(Right(requestData))

        MockRetrieveEmploymentService
          .retrieve(requestData)
          .returns(Future.successful(Right(ResponseWrapper(correlationId, hmrcEnteredEmploymentWithoutDateIgnoredResponseModel))))

        MockHateoasFactory
          .wrap(
            hmrcEnteredEmploymentWithoutDateIgnoredResponseModel,
            RetrieveEmploymentHateoasData(nino, taxYear, employmentId, hmrcEnteredEmploymentWithoutDateIgnoredResponseModel)
          )
          .returns(
            HateoasWrapper(
              hmrcEnteredEmploymentWithoutDateIgnoredResponseModel,
              Seq(
                listEmploymentLink,
                retrieveEmploymentLink,
                ignoreEmploymentLink
              )))

        runOkTest(expectedStatus = OK, maybeExpectedResponseBody = Some(mtdHmrcEnteredResponseWithoutDateIgnored))
      }

      "happy path for retrieving hmrc entered employment with date ignored present" in new Test {
        MockRetrieveCustomEmploymentRequestParser
          .parse(rawData)
          .returns(Right(requestData))

        MockRetrieveEmploymentService
          .retrieve(requestData)
          .returns(Future.successful(Right(ResponseWrapper(correlationId, hmrcEnteredEmploymentWithDateIgnoredResponseModel))))

        MockHateoasFactory
          .wrap(
            hmrcEnteredEmploymentWithDateIgnoredResponseModel,
            RetrieveEmploymentHateoasData(nino, taxYear, employmentId, hmrcEnteredEmploymentWithDateIgnoredResponseModel)
          )
          .returns(
            HateoasWrapper(
              hmrcEnteredEmploymentWithDateIgnoredResponseModel,
              Seq(
                listEmploymentLink,
                retrieveEmploymentLink,
                unignoreEmploymentLink
              )))

        runOkTest(expectedStatus = OK, maybeExpectedResponseBody = Some(mtdHmrcEnteredResponseWithDateIgnored))
      }

      "happy path for retrieving custom entered employment" in new Test {
        MockRetrieveCustomEmploymentRequestParser
          .parse(rawData)
          .returns(Right(requestData))

        MockRetrieveEmploymentService
          .retrieve(requestData)
          .returns(Future.successful(Right(ResponseWrapper(correlationId, customEnteredEmploymentResponseModel))))

        MockHateoasFactory
          .wrap(
            customEnteredEmploymentResponseModel,
            RetrieveEmploymentHateoasData(nino, taxYear, employmentId, customEnteredEmploymentResponseModel))
          .returns(
            HateoasWrapper(
              customEnteredEmploymentResponseModel,
              Seq(
                listEmploymentLink,
                retrieveEmploymentLink,
                amendCustomEmploymentLink,
                deleteCustomEmploymentLink
              )))

        runOkTest(expectedStatus = OK, maybeExpectedResponseBody = Some(mtdCustomEnteredResponse))
      }
    }

    "return the error as per spec" when {
      "the parser validation fails" in new Test {
        MockRetrieveCustomEmploymentRequestParser
          .parse(rawData)
          .returns(Left(ErrorWrapper(correlationId, NinoFormatError, None)))

        runErrorTest(NinoFormatError)
      }

      "the service returns an error" in new Test {
        MockRetrieveCustomEmploymentRequestParser
          .parse(rawData)
          .returns(Right(requestData))

        MockRetrieveEmploymentService
          .retrieve(requestData)
          .returns(Future.successful(Left(ErrorWrapper(correlationId, NotFoundError))))

        runErrorTest(NotFoundError)
      }
    }
  }

  trait Test extends ControllerTest {

    val controller = new RetrieveEmploymentController(
      authService = mockEnrolmentsAuthService,
      lookupService = mockMtdIdLookupService,
      parser = mockRetrieveCustomEmploymentRequestParser,
      service = mockRetrieveEmploymentService,
      hateoasFactory = mockHateoasFactory,
      cc = cc,
      idGenerator = mockIdGenerator
    )

    protected def callController(): Future[Result] = controller.retrieveEmployment(nino, taxYear, employmentId)(fakeGetRequest)

  }

}
