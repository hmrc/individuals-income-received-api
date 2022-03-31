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

package v1r7.mocks.connectors

import api.connectors.DownstreamOutcome
import org.scalamock.handlers.CallHandler
import org.scalamock.scalatest.MockFactory
import uk.gov.hmrc.http.HeaderCarrier
import v1r7.connectors.CreateAmendCgtResidentialPropertyDisposalsConnector
import v1r7.models.request.createAmendCgtResidentialPropertyDisposals.CreateAmendCgtResidentialPropertyDisposalsRequest

import scala.concurrent.{ExecutionContext, Future}

trait MockCreateAmendCgtResidentialPropertyDisposalsConnector extends MockFactory {

  val mockCreateAmendCgtResidentialPropertyDisposalsConnector: CreateAmendCgtResidentialPropertyDisposalsConnector =
    mock[CreateAmendCgtResidentialPropertyDisposalsConnector]

  object MockCreateAmendCgtResidentialPropertyDisposalsConnector {

    def createAndAmend(request: CreateAmendCgtResidentialPropertyDisposalsRequest): CallHandler[Future[DownstreamOutcome[Unit]]] = {
      (
        mockCreateAmendCgtResidentialPropertyDisposalsConnector
          .createAndAmend(_: CreateAmendCgtResidentialPropertyDisposalsRequest)(
            _: HeaderCarrier,
            _: ExecutionContext,
            _: String
          )
        )
        .expects(request, *, *, *)
    }

  }

}
