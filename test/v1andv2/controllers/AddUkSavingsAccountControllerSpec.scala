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

package v1andv2.controllers

import api.controllers.{ControllerBaseSpec, ControllerTestRunner}
import api.hateoas.HateoasLinks
import api.mocks.hateoas.MockHateoasFactory
import api.models.audit._
import api.models.auth.UserDetails
import api.models.domain.Nino
import api.models.errors._
import api.models.hateoas.{HateoasWrapper, Link}
import api.models.outcomes.ResponseWrapper
import mocks.MockAppConfig
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.{AnyContentAsJson, Result}
import v1andv2.mocks.requestParsers.MockAddUkSavingsAccountRequestParser
import v1andv2.mocks.services.MockAddUkSavingsAccountService
import v1andv2.models.request.addUkSavingsAccount.{AddUkSavingsAccountRawData, AddUkSavingsAccountRequest, AddUkSavingsAccountRequestBody}
import v1andv2.models.response.addUkSavingsAccount.{AddUkSavingsAccountHateoasData, AddUkSavingsAccountResponse}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class AddUkSavingsAccountControllerSpec
    extends ControllerBaseSpec
    with ControllerTestRunner
    with MockAppConfig
    with MockAddUkSavingsAccountService
    with MockAddUkSavingsAccountRequestParser
    with MockHateoasFactory
    with HateoasLinks {

  val savingsAccountId: String = "SAVKB2UVwUTBQGJ"
  val mtdId: String            = "test-mtd-id"

  "AddUkSavingsAccountController" should {
    "return OK" when {
      "happy path" in new Test {

        MockAddUkSavingsAccountRequestParser
          .parse(rawData)
          .returns(Right(requestData))

        MockAddUkSavingsAccountService
          .addUkSavingsAccountService(requestData)
          .returns(Future.successful(Right(ResponseWrapper(correlationId, responseData))))

        MockHateoasFactory
          .wrap(responseData, AddUkSavingsAccountHateoasData(nino, savingsAccountId))
          .returns(HateoasWrapper(responseData, links))

        runOkTest(expectedStatus = OK, maybeExpectedResponseBody = Some(responseJson))
      }
    }
    "return the error as per spec" when {

      "the parser validation fails" in new Test {
        MockAddUkSavingsAccountRequestParser
          .parse(rawData)
          .returns(Left(ErrorWrapper(correlationId, NinoFormatError, None)))

        runErrorTest(NinoFormatError)
      }

      "the service returns an error" in new Test {
        MockAddUkSavingsAccountRequestParser
          .parse(rawData)
          .returns(Right(requestData))

        MockAddUkSavingsAccountService
          .addUkSavingsAccountService(requestData)
          .returns(Future.successful(Left(ErrorWrapper(correlationId, RuleMaximumSavingsAccountsLimitError))))

        runErrorTest(RuleMaximumSavingsAccountsLimitError)
      }
    }
  }

  trait Test extends ControllerTest with AuditEventChecking[FlattenedGenericAuditDetail] {

    private val controller = new AddUkSavingsAccountController(
      authService = mockEnrolmentsAuthService,
      lookupService = mockMtdIdLookupService,
      parser = mockAddUkSavingsAccountRequestParser,
      service = mockAddUkSavingsAccountService,
      hateoasFactory = mockHateoasFactory,
      auditService = mockAuditService,
      cc = cc,
      idGenerator = mockIdGenerator
    )

    protected def callController(): Future[Result] = controller.addUkSavingsAccount(nino)(fakePostRequest(requestBodyJson))

    protected def event(auditResponse: AuditResponse, maybeRequestBody: Option[JsValue]): AuditEvent[FlattenedGenericAuditDetail] =
      AuditEvent(
        auditType = "AddUkSavingsAccount",
        transactionName = "add-uk-savings-account",
        detail = FlattenedGenericAuditDetail(
          versionNumber = Some("1.0"),
          userDetails = UserDetails(mtdId, "Individual", None),
          params = Map("nino" -> nino),
          request = maybeRequestBody,
          `X-CorrelationId` = correlationId,
          auditResponse = auditResponse
        )
      )

    MockedAppConfig.apiGatewayContext.returns("individuals/income-received").anyNumberOfTimes()

    val links: List[Link] = List(
      listUkSavings(mockAppConfig, nino)
    )

    val requestBodyJson: JsValue = Json.parse("""
      |{
      |   "accountName": "Shares savings account"
      |}
      |""".stripMargin)

    val rawData: AddUkSavingsAccountRawData = AddUkSavingsAccountRawData(
      nino = nino,
      body = AnyContentAsJson(requestBodyJson)
    )

    val requestData: AddUkSavingsAccountRequest = AddUkSavingsAccountRequest(
      nino = Nino(nino),
      body = AddUkSavingsAccountRequestBody("Shares savings account")
    )

    val responseData: AddUkSavingsAccountResponse = AddUkSavingsAccountResponse(
      savingsAccountId = savingsAccountId
    )

    val responseJson: JsValue = Json.parse(s"""
      |{
      |    "savingsAccountId": "$savingsAccountId",
      |    "links":[
      |      {
      |         "href":"/individuals/income-received/savings/uk-accounts/$nino",
      |         "method":"GET",
      |         "rel":"list-all-uk-savings-account"
      |      }
      |   ]
      |}
      |""".stripMargin)

  }

}
