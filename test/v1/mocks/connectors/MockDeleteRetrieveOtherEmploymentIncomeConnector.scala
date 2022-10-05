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

package v1.mocks.connectors

import api.connectors.DownstreamOutcome
import org.scalamock.handlers.CallHandler
import org.scalamock.scalatest.MockFactory
import uk.gov.hmrc.http.HeaderCarrier
import v1.connectors.DeleteRetrieveOtherEmploymentIncomeConnector
import v1.models.request.retrieveOtherEmploymentIncome.RetrieveOtherEmploymentIncomeRequest
import v1.models.response.retrieveOtherEmployment.RetrieveOtherEmploymentResponse
import v1.models.request.deleteOtherEmploymentIncome.DeleteOtherEmploymentIncomeRequest

import scala.concurrent.{ExecutionContext, Future}

trait MockDeleteRetrieveOtherEmploymentIncomeConnector extends MockFactory {

  val mockDeleteRetrieveOtherEmploymentIncomeConnector: DeleteRetrieveOtherEmploymentIncomeConnector =
    mock[DeleteRetrieveOtherEmploymentIncomeConnector]

  object MockDeleteRetrieveOtherEmploymentIncomeConnector {

    def deleteOtherEmploymentIncome(request: DeleteOtherEmploymentIncomeRequest): CallHandler[Future[DownstreamOutcome[Unit]]] = {
      (
        mockDeleteRetrieveOtherEmploymentIncomeConnector
          .deleteOtherEmploymentIncome(_: DeleteOtherEmploymentIncomeRequest)(
            _: HeaderCarrier,
            _: ExecutionContext,
            _: String
          )
        )
        .expects(request, *, *, *)
    }

    def retrieveOtherEmploymentIncome(
        request: RetrieveOtherEmploymentIncomeRequest): CallHandler[Future[DownstreamOutcome[RetrieveOtherEmploymentResponse]]] = {
      (
        mockDeleteRetrieveOtherEmploymentIncomeConnector
          .retrieveOtherEmploymentIncome(_: RetrieveOtherEmploymentIncomeRequest)(
            _: HeaderCarrier,
            _: ExecutionContext,
            _: String
          )
        )
        .expects(request, *, *, *)
    }

  }

}
