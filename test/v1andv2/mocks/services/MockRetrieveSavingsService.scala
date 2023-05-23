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

package v1andv2.mocks.services

import api.controllers.RequestContext
import api.services.ServiceOutcome
import org.scalamock.handlers.CallHandler
import org.scalamock.scalatest.MockFactory
import v1andv2.models.request.retrieveSavings.RetrieveSavingsRequest
import v1andv2.models.response.retrieveSavings.RetrieveSavingsResponse
import v1andv2.services.RetrieveSavingsService

import scala.concurrent.{ExecutionContext, Future}

trait MockRetrieveSavingsService extends MockFactory {

  val mockRetrieveSavingsService: RetrieveSavingsService =
    mock[RetrieveSavingsService]

  object MockRetrieveSavingsService {

    def retrieve(requestData: RetrieveSavingsRequest): CallHandler[Future[ServiceOutcome[RetrieveSavingsResponse]]] = (
      mockRetrieveSavingsService
        .retrieveSavings(_: RetrieveSavingsRequest)(
          _: RequestContext,
          _: ExecutionContext
        )
      )
      .expects(requestData, *, *)

  }

}
