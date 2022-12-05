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

package v1.services

import api.controllers.EndpointLogContext
import api.models.errors._
import api.models.outcomes.ResponseWrapper
import api.support.DownstreamResponseMappingSupport
import cats.data.EitherT
import uk.gov.hmrc.http.HeaderCarrier
import utils.Logging
import v1.connectors.RetrieveAllResidentialPropertyCgtConnector
import v1.models.request.retrieveAllResidentialPropertyCgt.RetrieveAllResidentialPropertyCgtRequest
import v1.models.response.retrieveAllResidentialPropertyCgt.RetrieveAllResidentialPropertyCgtResponse

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class RetrieveAllResidentialPropertyCgtService @Inject() (connector: RetrieveAllResidentialPropertyCgtConnector)
    extends DownstreamResponseMappingSupport
    with Logging {

  def retrieve(request: RetrieveAllResidentialPropertyCgtRequest)(implicit
      hc: HeaderCarrier,
      ec: ExecutionContext,
      logContext: EndpointLogContext,
      correlationId: String): Future[Either[ErrorWrapper, ResponseWrapper[RetrieveAllResidentialPropertyCgtResponse]]] = {

    val result = for {
      desResponseWrapper <- EitherT(connector.retrieve(request)).leftMap(mapDownstreamErrors(defaultDesErrorMap))
      mtdResponseWrapper <- EitherT.fromEither[Future](validateRetrieveResponse(desResponseWrapper))
    } yield mtdResponseWrapper

    result.value
  }

  private def defaultDesErrorMap: Map[String, MtdError] =
    Map(
      "INVALID_TAXABLE_ENTITY_ID" -> NinoFormatError,
      "INVALID_TAX_YEAR"          -> TaxYearFormatError,
      "INVALID_CORRELATIONID"     -> StandardDownstreamError,
      "NO_DATA_FOUND"             -> NotFoundError,
      "SERVER_ERROR"              -> StandardDownstreamError,
      "SERVICE_UNAVAILABLE"       -> StandardDownstreamError
    )

}
