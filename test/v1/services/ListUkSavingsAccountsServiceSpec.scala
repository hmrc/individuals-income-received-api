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
import v1.mocks.connectors.MockListUkSavingsAccountsConnector
import v1.models.request.listUkSavingsAccounts.ListUkSavingsAccountsRequest
import v1.models.response.listUkSavingsAccounts.{ListUkSavingsAccountsResponse, UkSavingsAccount}

import scala.concurrent.Future

class ListUkSavingsAccountsServiceSpec extends ServiceSpec {

  private val nino             = "AA112233A"
  private val savingsAccountId = "SAVKB2UVwUTBQGJ"

  private val requestData = ListUkSavingsAccountsRequest(Nino(nino), Some(savingsAccountId))

  private val validResponse = ListUkSavingsAccountsResponse(
    savingsAccounts = Some(
      Seq(
        UkSavingsAccount(savingsAccountId = "000000000000001", accountName = "Bank Account 1"),
        UkSavingsAccount(savingsAccountId = "000000000000002", accountName = "Bank Account 2"),
        UkSavingsAccount(savingsAccountId = "000000000000003", accountName = "Bank Account 3")
      )
    )
  )

  trait Test extends MockListUkSavingsAccountsConnector {
    implicit val logContext: EndpointLogContext = EndpointLogContext("c", "ep")

    val service: ListUkSavingsAccountsService = new ListUkSavingsAccountsService(connector = mockListUkSavingsAccountsConnector)
  }

  "ListUkSavingsAccountsService" when {
    "listUkSavingsAccounts" must {
      "return correct result for a success" in new Test {
        val outcome = Right(ResponseWrapper(correlationId, validResponse))

        MockListUkSavingsAccountsConnector
          .listUkSavingsAccounts(requestData)
          .returns(Future.successful(outcome))

        await(service.listUkSavingsAccounts(requestData)) shouldBe outcome
      }

      "map errors according to spec" when {

        def serviceError(ifsErrorCode: String, error: MtdError): Unit =
          s"a $ifsErrorCode error is returned from the service" in new Test {

            MockListUkSavingsAccountsConnector
              .listUkSavingsAccounts(requestData)
              .returns(Future.successful(Left(ResponseWrapper(correlationId, DownstreamErrors.single(DownstreamErrorCode(ifsErrorCode))))))

            await(service.listUkSavingsAccounts(requestData)) shouldBe Left(ErrorWrapper(correlationId, error))
          }

        val input = List(
          ("INVALID_ID_TYPE", InternalError),
          ("INVALID_IDVALUE", NinoFormatError),
          ("INVALID_INCOMESOURCETYPE", InternalError),
          ("INVALID_TAXYEAR", InternalError),
          ("INVALID_INCOMESOURCEID", SavingsAccountIdFormatError),
          ("INVALID_ENDDATE", InternalError),
          ("NOT_FOUND", NotFoundError),
          ("SERVER_ERROR", InternalError),
          ("SERVICE_UNAVAILABLE", InternalError)
        )

        input.foreach(args => (serviceError _).tupled(args))
      }
    }
  }

}
