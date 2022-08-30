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
import cats.implicits._
import javax.inject.{Inject, Singleton}
import uk.gov.hmrc.http.HeaderCarrier
import utils.Logging
import v1.connectors.RetrieveUKDividendsIncomeAnnualSummaryConnector
import v1.models.request.retrieveUkDividendsAnnualIncomeSummary.RetrieveUkDividendsAnnualIncomeSummaryRequest
import v1.models.response.retrieveUkDividendsAnnualIncomeSummary.RetrieveUkDividendsAnnualIncomeSummaryResponse

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class RetrieveUkDividendsIncomeAnnualSummaryService @Inject() (connector: RetrieveUKDividendsIncomeAnnualSummaryConnector)
    extends DownstreamResponseMappingSupport
    with Logging {

  def retrieveUKDividendsIncomeAnnualSummary(request: RetrieveUkDividendsAnnualIncomeSummaryRequest)(implicit
      hc: HeaderCarrier,
      ec: ExecutionContext,
      logContext: EndpointLogContext,
      correlationId: String): Future[Either[ErrorWrapper, ResponseWrapper[RetrieveUkDividendsAnnualIncomeSummaryResponse]]] = {

    val result = for {
      desResponseWrapper <- EitherT(connector.retrieveUKDividendsIncomeAnnualSummary(request)).leftMap(mapDownstreamErrors(mappingDesToMtdError))
    } yield desResponseWrapper

    result.value
  }

  private def mappingDesToMtdError: Map[String, MtdError] = Map(
    "INVALID_NINO"            -> NinoFormatError,
    "INVALID_TYPE"            -> StandardDownstreamError,
    "INVALID_TAXYEAR"         -> TaxYearFormatError,
    "INVALID_INCOME_SOURCE"   -> StandardDownstreamError,
    "NOT_FOUND_PERIOD"        -> NotFoundError,
    "NOT_FOUND_INCOME_SOURCE" -> NotFoundError,
    "SERVER_ERROR"            -> StandardDownstreamError,
    "SERVICE_UNAVAILABLE"     -> StandardDownstreamError
  )

}
