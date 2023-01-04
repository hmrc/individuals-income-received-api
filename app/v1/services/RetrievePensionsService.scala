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

package v1.services

import api.controllers.EndpointLogContext
import api.models.errors.{
  ErrorWrapper,
  MtdError,
  NinoFormatError,
  NotFoundError,
  RuleTaxYearNotSupportedError,
  StandardDownstreamError,
  TaxYearFormatError
}
import api.models.outcomes.ResponseWrapper
import api.support.DownstreamResponseMappingSupport
import cats.data.EitherT
import uk.gov.hmrc.http.HeaderCarrier
import utils.Logging
import v1.connectors.RetrievePensionsConnector
import v1.models.request.retirevePensions.RetrievePensionsRequest
import v1.models.response.retrievePensions.RetrievePensionsResponse

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class RetrievePensionsService @Inject() (connector: RetrievePensionsConnector) extends DownstreamResponseMappingSupport with Logging {

  def retrievePensions(request: RetrievePensionsRequest)(implicit
      hc: HeaderCarrier,
      ec: ExecutionContext,
      logContext: EndpointLogContext,
      correlationId: String): Future[Either[ErrorWrapper, ResponseWrapper[RetrievePensionsResponse]]] = {

    val result = for {
      downstreamResponseWrapper <- EitherT(connector.retrieve(request)).leftMap(mapDownstreamErrors(errorMap))
      mtdResponseWrapper        <- EitherT.fromEither[Future](validateRetrieveResponse(downstreamResponseWrapper))
    } yield mtdResponseWrapper

    result.value
  }

  private def errorMap: Map[String, MtdError] = {
    val errors = Map(
      "INVALID_TAXABLE_ENTITY_ID" -> NinoFormatError,
      "INVALID_TAX_YEAR"          -> TaxYearFormatError,
      "INVALID_CORRELATIONID"     -> StandardDownstreamError,
      "NO_DATA_FOUND"             -> NotFoundError,
      "SERVER_ERROR"              -> StandardDownstreamError,
      "SERVICE_UNAVAILABLE"       -> StandardDownstreamError
    )
    val extraTysErrors = Map(
      "INVALID_CORRELATION_ID" -> StandardDownstreamError,
      "TAX_YEAR_NOT_SUPPORTED" -> RuleTaxYearNotSupportedError
    )

    errors ++ extraTysErrors
  }

}
