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
import api.models.errors._
import api.models.outcomes.ResponseWrapper
import api.support.DownstreamResponseMappingSupport
import cats.data.EitherT
import javax.inject.{Inject, Singleton}
import uk.gov.hmrc.http.HeaderCarrier
import utils.Logging
import v1.connectors.DeleteUkDividendsIncomeAnnualSummaryConnector
import v1.models.request.deleteUkDividendsIncomeAnnualSummary.DeleteUkDividendsIncomeAnnualSummaryRequest

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class DeleteUkDividendsIncomeAnnualSummaryService @Inject() (connector: DeleteUkDividendsIncomeAnnualSummaryConnector)
    extends DownstreamResponseMappingSupport
    with Logging {

  def delete(request: DeleteUkDividendsIncomeAnnualSummaryRequest)(implicit
      hc: HeaderCarrier,
      ec: ExecutionContext,
      logContext: EndpointLogContext,
      correlationId: String): Future[Either[ErrorWrapper, ResponseWrapper[Unit]]] = {

    val result = EitherT(connector.delete(request)).leftMap(mapDownstreamErrors(downstreamErrorMap))

    result.value
  }

  private def downstreamErrorMap: Map[String, MtdError] = {
    val errors = Map(
      "INVALID_NINO"                      -> NinoFormatError,
      "INVALID_TYPE"                      -> StandardDownstreamError,
      "INVALID_TAXYEAR"                   -> TaxYearFormatError,
      "INVALID_PAYLOAD"                   -> StandardDownstreamError,
      "NOT_FOUND_INCOME_SOURCE"           -> NotFoundError,
      "MISSING_CHARITIES_NAME_GIFT_AID"   -> StandardDownstreamError,
      "MISSING_GIFT_AID_AMOUNT"           -> StandardDownstreamError,
      "MISSING_CHARITIES_NAME_INVESTMENT" -> StandardDownstreamError,
      "MISSING_INVESTMENT_AMOUNT"         -> StandardDownstreamError,
      "INVALID_ACCOUNTING_PERIOD"         -> RuleTaxYearNotSupportedError,
      "SERVER_ERROR"                      -> StandardDownstreamError,
      "SERVICE_UNAVAILABLE"               -> StandardDownstreamError,
      "GONE"                              -> NotFoundError,
      "NOT_FOUND"                         -> NotFoundError
    )

    val extraTysErrors = Map(
      "INVALID_INCOMESOURCE_TYPE"    -> StandardDownstreamError,
      "INVALID_TAX_YEAR"             -> TaxYearFormatError,
      "INVALID_CORRELATION_ID"       -> StandardDownstreamError,
      "INVALID_INCOMESOURCE_ID"      -> StandardDownstreamError,
      "INCOME_SOURCE_DATA_NOT_FOUND" -> NotFoundError,
      "PERIOD_NOT_FOUND"             -> NotFoundError,
      "PERIOD_ALREADY_DELETED"       -> NotFoundError
    )

    errors ++ extraTysErrors
  }

}
