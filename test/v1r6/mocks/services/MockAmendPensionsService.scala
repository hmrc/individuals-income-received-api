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

package v1r6.mocks.services

import org.scalamock.handlers.CallHandler
import org.scalamock.scalatest.MockFactory
import uk.gov.hmrc.http.HeaderCarrier
import v1r6.controllers.EndpointLogContext
import v1r6.models.errors.ErrorWrapper
import v1r6.models.outcomes.ResponseWrapper
import v1r6.models.request.amendPensions.AmendPensionsRequest
import v1r6.services.AmendPensionsService

import scala.concurrent.{ExecutionContext, Future}

trait MockAmendPensionsService extends MockFactory {

  val mockAmendPensionsService: AmendPensionsService = mock[AmendPensionsService]

  object MockAmendPensionsService {

    def amendPensions(requestData: AmendPensionsRequest): CallHandler[Future[Either[ErrorWrapper, ResponseWrapper[Unit]]]] = {
      (mockAmendPensionsService
        .amendPensions(_: AmendPensionsRequest)(_: HeaderCarrier, _: ExecutionContext, _: EndpointLogContext, _: String))
        .expects(requestData, *, *, *, *)
    }
  }

}