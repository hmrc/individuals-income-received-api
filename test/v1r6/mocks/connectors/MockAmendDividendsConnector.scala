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

package v1r6.mocks.connectors

import org.scalamock.handlers.CallHandler
import org.scalamock.scalatest.MockFactory
import uk.gov.hmrc.http.HeaderCarrier
import v1r6.connectors.{AmendDividendsConnector, DesOutcome}
import v1r6.models.request.amendDividends.AmendDividendsRequest

import scala.concurrent.{ExecutionContext, Future}

trait MockAmendDividendsConnector extends MockFactory {

  val mockAmendDividendsConnector: AmendDividendsConnector = mock[AmendDividendsConnector]

  object MockAmendDividendsConnector {

    def amendDividends(request: AmendDividendsRequest): CallHandler[Future[DesOutcome[Unit]]] = {
      (mockAmendDividendsConnector
        .amendDividends(_: AmendDividendsRequest)(_: HeaderCarrier, _: ExecutionContext, _: String))
        .expects(request, *, *, *)
    }
  }

}