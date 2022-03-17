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

package v1r7.mocks.services

import api.controllers.EndpointLogContext
import api.models.errors.ErrorWrapper
import api.models.outcomes.ResponseWrapper
import org.scalamock.handlers.CallHandler
import org.scalamock.scalatest.MockFactory
import uk.gov.hmrc.http.HeaderCarrier
import v1r7.models.request.createAmendNonPayeEmployment.CreateAmendNonPayeEmploymentRequest
import v1r7.services.CreateAmendNonPayeEmploymentService

import scala.concurrent.{ExecutionContext, Future}

trait MockCreateAmendNonPayeEmploymentService extends MockFactory{

  val mockService: CreateAmendNonPayeEmploymentService = mock[CreateAmendNonPayeEmploymentService]

  object MockCreateAmendNonPayeEmploymentService {
    def createAndAmend(requestData: CreateAmendNonPayeEmploymentRequest): CallHandler[Future[Either[ErrorWrapper, ResponseWrapper[Unit]]]] = {
      (mockService
        .createAndAmend(_: CreateAmendNonPayeEmploymentRequest)(_: HeaderCarrier, _: ExecutionContext, _: EndpointLogContext, _: String))
        .expects(requestData, *, *, *, *)
    }
  }
}
