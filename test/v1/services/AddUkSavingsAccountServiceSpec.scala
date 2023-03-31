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

package v1.services

import api.controllers.EndpointLogContext
import api.models.domain.Nino
import api.models.errors._
import api.models.outcomes.ResponseWrapper
import api.services.ServiceSpec
import v1.mocks.connectors.MockAddUkSavingsAccountConnector
import v1.models.request.addUkSavingsAccount.{AddUkSavingsAccountRequest, AddUkSavingsAccountRequestBody}
import v1.models.response.addUkSavingsAccount.AddUkSavingsAccountResponse

import scala.concurrent.Future

class AddUkSavingsAccountServiceSpec extends ServiceSpec {

  private val nino                                                   = "AA112233A"
  val addUkSavingsAccountRequestBody: AddUkSavingsAccountRequestBody = AddUkSavingsAccountRequestBody(accountName = "Shares savings account")

  val addUkSavingsAccountRequest: AddUkSavingsAccountRequest = AddUkSavingsAccountRequest(
    nino = Nino(nino),
    body = addUkSavingsAccountRequestBody
  )

  val addUkSavingsAccountResponse: AddUkSavingsAccountResponse = AddUkSavingsAccountResponse(
    savingsAccountId = "SAVKB2UVwUTBQGJ"
  )

  trait Test extends MockAddUkSavingsAccountConnector {
    implicit val logContext: EndpointLogContext = EndpointLogContext("c", "ep")

    val service: AddUkSavingsAccountService = new AddUkSavingsAccountService(
      connector = mockAddUkSavingsAccountConnector
    )

  }

  "addSavings" should {
    "return a 200 status for a success scenario" when {
      "valid request is supplied" in new Test {
        private val outcome = Right(ResponseWrapper(correlationId, addUkSavingsAccountResponse))

        MockAddUkSavingsAccountConnector
          .addSavings(addUkSavingsAccountRequest)
          .returns(Future.successful(outcome))

        await(service.addSavings(addUkSavingsAccountRequest)) shouldBe outcome
      }
    }
    "map errors according to spec" when {

      def serviceError(desErrorCode: String, error: MtdError): Unit =
        s"a $desErrorCode error is returned from the service" in new Test {

          MockAddUkSavingsAccountConnector
            .addSavings(addUkSavingsAccountRequest)
            .returns(Future.successful(Left(ResponseWrapper(correlationId, DownstreamErrors.single(DownstreamErrorCode(desErrorCode))))))

          await(service.addSavings(addUkSavingsAccountRequest)) shouldBe Left(ErrorWrapper(correlationId, error))
        }

      val input = List(
        ("INVALID_IDVALUE", NinoFormatError),
        ("MAX_ACCOUNTS_REACHED", RuleMaximumSavingsAccountsLimitError),
        ("ALREADY_EXISTS", RuleDuplicateAccountNameError),
        ("INVALID_IDTYPE", InternalError),
        ("INVALID_PAYLOAD", InternalError),
        ("SERVER_ERROR", InternalError),
        ("SERVICE_UNAVAILABLE", InternalError)
      )

      input.foreach(args => (serviceError _).tupled(args))

    }
  }

}
