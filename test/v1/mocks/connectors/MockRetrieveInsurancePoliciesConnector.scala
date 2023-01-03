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

package v1.mocks.connectors

import api.connectors.DownstreamOutcome
import org.scalamock.handlers.CallHandler
import org.scalamock.scalatest.MockFactory
import uk.gov.hmrc.http.HeaderCarrier
import v1.connectors.RetrieveInsurancePoliciesConnector
import v1.models.request.retrieveInsurancePolicies.RetrieveInsurancePoliciesRequest
import v1.models.response.retrieveInsurancePolicies.RetrieveInsurancePoliciesResponse

import scala.concurrent.{ExecutionContext, Future}

trait MockRetrieveInsurancePoliciesConnector extends MockFactory {

  val mockRetrieveInsurancePoliciesConnector: RetrieveInsurancePoliciesConnector = mock[RetrieveInsurancePoliciesConnector]

  object MockRetrieveInsurancePoliciesConnector {

    def retrieve(requestData: RetrieveInsurancePoliciesRequest): CallHandler[Future[DownstreamOutcome[RetrieveInsurancePoliciesResponse]]] = {
      (mockRetrieveInsurancePoliciesConnector
        .retrieveInsurancePolicies(_: RetrieveInsurancePoliciesRequest)(_: HeaderCarrier, _: ExecutionContext, _: String))
        .expects(requestData, *, *, *)
    }

  }

}
