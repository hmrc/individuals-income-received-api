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
import v1andv2.models.request.retrieveOtherCgt.RetrieveOtherCgtRequest
import v1andv2.models.response.retrieveOtherCgt.RetrieveOtherCgtResponse
import v1andv2.services.RetrieveOtherCgtService

import scala.concurrent.{ExecutionContext, Future}

trait MockRetrieveOtherCgtService extends MockFactory {

  val mockRetrieveOtherCgtService: RetrieveOtherCgtService =
    mock[RetrieveOtherCgtService]

  object MockRetrieveOtherCgtService {

    def retrieve(requestData: RetrieveOtherCgtRequest): CallHandler[Future[ServiceOutcome[RetrieveOtherCgtResponse]]] =
      (mockRetrieveOtherCgtService
        .retrieve(_: RetrieveOtherCgtRequest)(_: RequestContext, _: ExecutionContext))
        .expects(requestData, *, *)

  }

}
