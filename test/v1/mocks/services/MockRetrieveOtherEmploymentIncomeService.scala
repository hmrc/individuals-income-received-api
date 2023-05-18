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

package v1.mocks.services

import api.controllers.RequestContext
import api.services.ServiceOutcome
import org.scalamock.handlers.CallHandler
import org.scalamock.scalatest.MockFactory
import v1.models.request.otherEmploymentIncome.OtherEmploymentIncomeRequest
import v1.models.response.retrieveOtherEmployment.RetrieveOtherEmploymentResponse
import v1.services.RetrieveOtherEmploymentIncomeService

import scala.concurrent.{ExecutionContext, Future}

trait MockRetrieveOtherEmploymentIncomeService extends MockFactory {

  val mockRetrieveOtherEmploymentIncomeService: RetrieveOtherEmploymentIncomeService =
    mock[RetrieveOtherEmploymentIncomeService]

  object MockRetrieveOtherEmploymentIncomeService {

    def retrieve(requestData: OtherEmploymentIncomeRequest): CallHandler[Future[ServiceOutcome[RetrieveOtherEmploymentResponse]]] = {
      (
        mockRetrieveOtherEmploymentIncomeService
          .retrieve(_: OtherEmploymentIncomeRequest)(
            _: RequestContext,
            _: ExecutionContext
          )
        )
        .expects(requestData, *, *)
    }

  }

}
