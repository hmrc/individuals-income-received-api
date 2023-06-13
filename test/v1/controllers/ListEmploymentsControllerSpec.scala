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
import api.models.hateoas.Method.{GET, POST}
import api.models.hateoas.RelType.{ADD_CUSTOM_EMPLOYMENT, SELF}
import api.models.hateoas.{HateoasWrapper, Link}
import api.models.outcomes.ResponseWrapper
import mocks.MockAppConfig
import play.api.mvc.Result
import v1.fixtures.ListEmploymentsControllerFixture._
import v1.mocks.requestParsers.MockListEmploymentsRequestParser
import v1.mocks.services.MockListEmploymentsService
import v1.models.request.listEmployments.{ListEmploymentsRawData, ListEmploymentsRequest}
import v1.models.response.listEmployment.{Employment, ListEmploymentHateoasData, ListEmploymentResponse}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class ListEmploymentsControllerSpec
    extends ControllerBaseSpec
    with ControllerTestRunner
    with MockAppConfig
    with MockListEmploymentsService
    with MockHateoasFactory
    with MockListEmploymentsRequestParser
    with HateoasLinks {

  val taxYear: String      = "2019-20"
  val employmentId: String = "4557ecb5-fd32-48cc-81f5-e6acd1099f3c"

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
    dateIgnored = Some(Timestamp("2020-06-17T10:53:38.000Z"))
  )

  private val customEmploymentModel = Employment(
    employmentId = "4557ecb5-fd32-48cc-81f5-e6acd1099f3c",
    employerName = "Vera Lynn"
  )

  private val listEmploymentsResponseModel = ListEmploymentResponse(
    Some(Seq(hmrcEmploymentModel, hmrcEmploymentModel)),
    Some(Seq(customEmploymentModel, customEmploymentModel))
  )

  private val hateoasResponse = ListEmploymentResponse(
    Some(
      Seq(
        HateoasWrapper(
          hmrcEmploymentModel,
          Seq(retrieveEmploymentLink)
        ),
        HateoasWrapper(
          hmrcEmploymentModel,
          Seq(retrieveEmploymentLink)
        )
      )),
    Some(
      Seq(
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
          .returns(
            HateoasWrapper(
              hateoasResponse,
              Seq(
                addCustomEmploymentLink,
                listEmploymentsLink
              )))

        runOkTest(
          expectedStatus = OK,
          maybeExpectedResponseBody = Some(mtdResponse)
        )
      }
    }

    "return the error as per spec" when {
      "the parser validation fails" in new Test {
        MockListEmploymentsRequestParser
          .parse(rawData)
          .returns(Left(ErrorWrapper(correlationId, NinoFormatError)))

        runErrorTest(NinoFormatError)
      }

      "the service returns an error" in new Test {
        MockListEmploymentsRequestParser
          .parse(rawData)
          .returns(Right(requestData))

        MockListEmploymentsService
          .listEmployments(requestData)
          .returns(Future.successful(Left(ErrorWrapper(correlationId, NotFoundError))))

        runErrorTest(NotFoundError)
      }
    }
  }

  trait Test extends ControllerTest {

    val controller = new ListEmploymentsController(
      authService = mockEnrolmentsAuthService,
      lookupService = mockMtdIdLookupService,
      parser = mockListEmploymentsRequestParser,
      service = mockListEmploymentsService,
      hateoasFactory = mockHateoasFactory,
      cc = cc,
      idGenerator = mockIdGenerator
    )

    protected def callController(): Future[Result] = controller.listEmployments(nino, taxYear)(fakeGetRequest)
  }

}
