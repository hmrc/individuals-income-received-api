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

import api.connectors.DownstreamUri
import api.controllers.EndpointLogContext
import api.models.errors.{ErrorWrapper, MtdError}
import api.models.outcomes.ResponseWrapper
import api.services.DeleteRetrieveService
import org.scalamock.handlers.CallHandler
import org.scalamock.scalatest.MockFactory
import play.api.libs.json.{Format, Reads}
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.{ExecutionContext, Future}

trait MockDeleteRetrieveService extends MockFactory {

  val mockDeleteRetrieveService: DeleteRetrieveService = mock[DeleteRetrieveService]

  object MockDeleteRetrieveService {

    def delete(downstreamErrorMap: Map[String, MtdError]): CallHandler[Future[Either[ErrorWrapper, ResponseWrapper[Unit]]]] = {
      (mockDeleteRetrieveService
        .delete(_: DownstreamUri[Unit], _: Map[String, MtdError])(_: HeaderCarrier, _: ExecutionContext, _: EndpointLogContext, _: String))
        .expects(*, downstreamErrorMap, *, *, *, *)
    }

    def retrieve[Resp: Reads](downstreamErrorMap: Map[String, MtdError]): CallHandler[Future[Either[ErrorWrapper, ResponseWrapper[Resp]]]] = {
      (
        mockDeleteRetrieveService
          .retrieve[Resp](_: DownstreamUri[Resp], _: Map[String, MtdError])(
            _: Format[Resp],
            _: HeaderCarrier,
            _: ExecutionContext,
            _: EndpointLogContext,
            _: String
          )
        )
        .expects(*, downstreamErrorMap, *, *, *, *, *)
    }

    def retrieve[Resp: Reads](downstreamUri: DownstreamUri[Resp],
                              downstreamErrorMap: Map[String, MtdError]): CallHandler[Future[Either[ErrorWrapper, ResponseWrapper[Resp]]]] = {
      (
        mockDeleteRetrieveService
          .retrieve[Resp](_: DownstreamUri[Resp], _: Map[String, MtdError])(
            _: Format[Resp],
            _: HeaderCarrier,
            _: ExecutionContext,
            _: EndpointLogContext,
            _: String
          )
        )
        .expects(downstreamUri, downstreamErrorMap, *, *, *, *, *)
    }

  }

}
