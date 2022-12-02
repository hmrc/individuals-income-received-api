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

package api.services

import api.connectors.{DeleteRetrieveConnector, DownstreamUri}
import api.controllers.EndpointLogContext
import api.models.errors._
import api.models.outcomes.ResponseWrapper
import api.support.DownstreamResponseMappingSupport
import cats.data.EitherT
import cats.implicits._
import play.api.libs.json.Format
import uk.gov.hmrc.http.HeaderCarrier
import utils.Logging

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class DeleteRetrieveService @Inject() (connector: DeleteRetrieveConnector) extends DownstreamResponseMappingSupport with Logging {

  def delete(downstreamUri: DownstreamUri[Unit], downstreamErrorMap: Map[String, MtdError] = defaultDownstreamErrorMap)(implicit
      hc: HeaderCarrier,
      ec: ExecutionContext,
      logContext: EndpointLogContext,
      correlationId: String): Future[Either[ErrorWrapper, ResponseWrapper[Unit]]] = {

    EitherT(connector.delete(downstreamUri)).leftMap(mapDownstreamErrors(downstreamErrorMap)).value
  }

  def retrieve[Resp: Format](downstreamUri: DownstreamUri[Resp], downstreamErrorMap: Map[String, MtdError] = defaultDownstreamErrorMap)(implicit
      hc: HeaderCarrier,
      ec: ExecutionContext,
      logContext: EndpointLogContext,
      correlationId: String): Future[Either[ErrorWrapper, ResponseWrapper[Resp]]] = {

    val result = for {
      desResponseWrapper <- EitherT(connector.retrieve[Resp](downstreamUri)).leftMap(mapDownstreamErrors(downstreamErrorMap))
      mtdResponseWrapper <- EitherT.fromEither[Future](validateRetrieveResponse(desResponseWrapper))
    } yield mtdResponseWrapper

    result.value
  }

  private def defaultDownstreamErrorMap: Map[String, MtdError] =
    Map(
      "INVALID_TAXABLE_ENTITY_ID" -> NinoFormatError,
      "INVALID_TAX_YEAR"          -> TaxYearFormatError,
      "INVALID_CORRELATIONID"     -> StandardDownstreamError,
      "NO_DATA_FOUND"             -> NotFoundError,
      "SERVER_ERROR"              -> StandardDownstreamError,
      "SERVICE_UNAVAILABLE"       -> StandardDownstreamError
    )

}
