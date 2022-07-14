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
import api.mocks.services.{MockEnrolmentsAuthService, MockMtdIdLookupService}
import api.models.domain.Nino
import api.models.errors._
import api.models.hateoas.Method.{GET, POST}
import api.models.hateoas.RelType.{ADD_UK_SAVINGS_INCOME, SELF}
import api.models.hateoas.{HateoasWrapper, Link}
import api.models.outcomes.ResponseWrapper
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.Result
import uk.gov.hmrc.http.HeaderCarrier
import v1.mocks.requestParsers.MockListUkSavingsAccountsRequestParser
import v1.mocks.services.MockListUkSavingsAccountsService
import v1.models.request.listUkSavingsAccounts.{ListUkSavingsAccountsRawData, ListUkSavingsAccountsRequest}
import v1.models.response.listUkSavingsAccounts.{ListUkSavingsAccountsHateoasData, ListUkSavingsAccountsResponse, UkSavingsAccount}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class ListUkSavingsAccountsControllerSpec
    extends ControllerBaseSpec
    with MockEnrolmentsAuthService
    with MockMtdIdLookupService
    with MockListUkSavingsAccountsService
    with MockHateoasFactory
    with MockListUkSavingsAccountsRequestParser
    with HateoasLinks
    with MockIdGenerator {

  val nino: String                  = "AA123456A"
  val savingsAccountId: String      = "SAVKB2UVwUTBQGJ"
  val correlationId: String         = "X-123"

  val rawData: ListUkSavingsAccountsRawData = ListUkSavingsAccountsRawData(
    nino = nino,
    savingsAccountId = Some(savingsAccountId)
  )

  val requestData: ListUkSavingsAccountsRequest = ListUkSavingsAccountsRequest(
    nino = Nino(nino),
    savingsAccountId = Some(savingsAccountId)
  )

  val addUkSavingsAccountsLink: Link =
    Link(
      href = s"/individuals/income-received/savings/uk-accounts/$nino",
      method = POST,
      rel = ADD_UK_SAVINGS_INCOME
    )

  val listUkSavingsAccountsLink: Link =
    Link(
      href = s"/individuals/income-received/savings/uk-accounts/$nino",
      method = GET,
      rel = SELF
    )

  val validListUkSavingsAccountResponse: ListUkSavingsAccountsResponse[UkSavingsAccount] = ListUkSavingsAccountsResponse(
    Some(
      Seq(
        UkSavingsAccount("000000000000001", "Bank Account 1"),
        UkSavingsAccount("000000000000002", "Bank Account 2"),
        UkSavingsAccount("000000000000003", "Bank Account 3")
      )
    )
  )

  private val mtdResponse: JsValue = Json.parse(
    s"""|{
      | "savingsAccounts":
      |  [
      |    {
      |        "savingsAccountId": "000000000000001",
      |        "accountName": "Bank Account 1"
      |    },
      |    {
      |        "savingsAccountId": "000000000000002",
      |        "accountName": "Bank Account 2"
      |    },
      |    {
      |        "savingsAccountId": "000000000000003",
      |        "accountName": "Bank Account 3"
      |    }
      | ],
      | "links": [
      |      {
      |         "href":"/individuals/income-received/savings/uk-accounts/$nino",
      |         "rel":"add-uk-savings-account",
      |         "method":"POST"
      |      },
      |      {
      |         "href":"/individuals/income-received/savings/uk-accounts/$nino",
      |         "rel":"self",
      |         "method":"GET"
      |      }
      | ]
      |}""".stripMargin)

  trait Test {
    val hc: HeaderCarrier = HeaderCarrier()

    val controller = new ListUkSavingsAccountsController(
      authService = mockEnrolmentsAuthService,
      lookupService = mockMtdIdLookupService,
      requestParser = mockListUkSavingsAccountsRequestParser,
      service = mockListUkSavingsAccountsService,
      hateoasFactory = mockHateoasFactory,
      cc = cc,
      idGenerator = mockIdGenerator
    )

    MockedMtdIdLookupService.lookup(nino).returns(Future.successful(Right("test-mtd-id")))
    MockedEnrolmentsAuthService.authoriseUser()
    MockIdGenerator.generateCorrelationId.returns(correlationId)
  }

  "listUkSavingsAccounts" should {
    "return OK" when {
      "happy path" in new Test {

        MockListUkSavingsAccountsRequestParser
          .parse(rawData)
          .returns(Right(requestData))

        MockListUkSavingsAccountsService
          .listUkSavingsAccounts(requestData)
          .returns(Future.successful(Right(ResponseWrapper(correlationId, validListUkSavingsAccountResponse))))

        MockHateoasFactory
          .wrap(validListUkSavingsAccountResponse, ListUkSavingsAccountsHateoasData(nino))
          .returns(
            HateoasWrapper(
              validListUkSavingsAccountResponse,
              Seq(
                addUkSavingsAccountsLink,
                listUkSavingsAccountsLink
              )))

        val result: Future[Result] = controller.listUkSavingsAccounts(nino, Some(savingsAccountId))(fakeGetRequest)

        status(result) shouldBe OK
        contentAsJson(result) shouldBe mtdResponse
        header("X-CorrelationId", result) shouldBe Some(correlationId)
      }
    }

    "return the error as per spec" when {
      "parser errors occur" must {
        def errorsFromParserTester(error: MtdError, expectedStatus: Int): Unit = {
          s"a ${error.code} error is returned from the parser" in new Test {

            MockListUkSavingsAccountsRequestParser
              .parse(rawData)
              .returns(Left(ErrorWrapper(correlationId, error, None)))

            val result: Future[Result] = controller.listUkSavingsAccounts(nino, Some(savingsAccountId))(fakeGetRequest)

            status(result) shouldBe expectedStatus
            contentAsJson(result) shouldBe Json.toJson(error)
            header("X-CorrelationId", result) shouldBe Some(correlationId)
          }
        }

        val input = Seq(
          (BadRequestError, BAD_REQUEST),
          (NinoFormatError, BAD_REQUEST),
          (SavingsAccountIdFormatError, BAD_REQUEST)
        )

        input.foreach(args => (errorsFromParserTester _).tupled(args))
      }

      "service errors occur" must {
        def serviceErrors(mtdError: MtdError, expectedStatus: Int): Unit = {
          s"a $mtdError error is returned from the service" in new Test {

            MockListUkSavingsAccountsRequestParser
              .parse(rawData)
              .returns(Right(requestData))

            MockListUkSavingsAccountsService
              .listUkSavingsAccounts(requestData)
              .returns(Future.successful(Left(ErrorWrapper(correlationId, mtdError))))

            val result: Future[Result] = controller.listUkSavingsAccounts(nino, Some(savingsAccountId))(fakeGetRequest)

            status(result) shouldBe expectedStatus
            contentAsJson(result) shouldBe Json.toJson(mtdError)
            header("X-CorrelationId", result) shouldBe Some(correlationId)
          }
        }

        val input = Seq(
          (NinoFormatError, BAD_REQUEST),
          (SavingsAccountIdFormatError, BAD_REQUEST),
          (NotFoundError, NOT_FOUND),
          (StandardDownstreamError, INTERNAL_SERVER_ERROR)
        )

        input.foreach(args => (serviceErrors _).tupled(args))
      }
    }
  }

}
