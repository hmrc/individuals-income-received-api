/*
 * Copyright 2021 HM Revenue & Customs
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
import v1r6.models.request.ignoreEmployment.IgnoreEmploymentRequest
import v1r6.services.IgnoreEmploymentService

import scala.concurrent.{ExecutionContext, Future}

trait MockIgnoreEmploymentService extends MockFactory {

  val mockIgnoreEmploymentService: IgnoreEmploymentService = mock[IgnoreEmploymentService]

  object MockIgnoreEmploymentService {

    def ignoreEmployment(requestData: IgnoreEmploymentRequest): CallHandler[Future[Either[ErrorWrapper, ResponseWrapper[Unit]]]] = {
      (mockIgnoreEmploymentService
        .ignoreEmployment(_: IgnoreEmploymentRequest)(_: HeaderCarrier, _: ExecutionContext, _: EndpointLogContext, _: String))
        .expects(requestData, *, *, *, *)
    }
  }

}