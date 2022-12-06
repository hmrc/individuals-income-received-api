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

package api.mocks.services

import api.controllers.EndpointLogContext
import api.models.errors.ErrorWrapper
import api.models.outcomes.ResponseWrapper
import org.scalamock.handlers.CallHandler
import org.scalamock.scalatest.MockFactory
import uk.gov.hmrc.http.HeaderCarrier
import v1.models.request.deleteNonPayeEmployment.DeleteNonPayeEmploymentRequest
import v1.services.DeleteNonPayeEmploymentService

import scala.concurrent.{ExecutionContext, Future}

trait MockDeleteNonPayeEmploymentService extends MockFactory {

  val mockDeleteNonPayeEmploymentService: DeleteNonPayeEmploymentService = mock[DeleteNonPayeEmploymentService]

  object MockDeleteNonPayeEmploymentService {

    def deleteNonPayeEmployment(requestData: DeleteNonPayeEmploymentRequest)
    : CallHandler[Future[Either[ErrorWrapper, ResponseWrapper[Unit]]]] = {
      (
        mockDeleteNonPayeEmploymentService
          .deleteNonPayeEmployment(_: DeleteNonPayeEmploymentRequest)(
            _: HeaderCarrier,
            _: ExecutionContext,
            _: EndpointLogContext,
            _: String
          )
        )
        .expects(requestData, *, *, *, *)
    }

  }

}
