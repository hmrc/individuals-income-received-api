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

package v1.mocks.connectors

import org.scalamock.handlers.CallHandler
import org.scalamock.scalatest.MockFactory
import uk.gov.hmrc.http.HeaderCarrier
import v1.connectors.{AmendForeignConnector, DesOutcome}
import v1.models.request.amendForeign.AmendForeignRequest

import scala.concurrent.{ExecutionContext, Future}

trait MockAmendForeignConnector extends MockFactory {

  val mockAmendForeignConnector: AmendForeignConnector = mock[AmendForeignConnector]

  object MockAmendForeignConnector {

    def amendForeign(request: AmendForeignRequest): CallHandler[Future[DesOutcome[Unit]]] = {
      (mockAmendForeignConnector
        .amendForeign(_: AmendForeignRequest)(_: HeaderCarrier, _: ExecutionContext, _: String))
        .expects(request, *, *, *)
    }
  }

}