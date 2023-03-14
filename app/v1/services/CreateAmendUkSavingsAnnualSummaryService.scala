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
import uk.gov.hmrc.http.HeaderCarrier
import utils.Logging
import v1.connectors.CreateAmendUkSavingsAnnualSummaryConnector
import v1.models.request.createAmendUkSavingsAnnualSummary.{CreateAmendUkSavingsAnnualSummaryRequest, DownstreamCreateAmendUkSavingsAnnualSummaryBody}

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class CreateAmendUkSavingsAnnualSummaryService @Inject() (connector: CreateAmendUkSavingsAnnualSummaryConnector)
    extends DownstreamResponseMappingSupport
    with Logging {

  def createAmend(request: CreateAmendUkSavingsAnnualSummaryRequest)(implicit
      hc: HeaderCarrier,
      ec: ExecutionContext,
      logContext: EndpointLogContext,
      correlationId: String): Future[Either[ErrorWrapper, ResponseWrapper[Unit]]] = {
    val result = for {
      desResponseWrapper <- EitherT(
        connector.createOrAmendUKSavingsAccountSummary(request.nino, request.taxYear, DownstreamCreateAmendUkSavingsAnnualSummaryBody(request)))
        .leftMap(mapDownstreamErrors(downstreamErrorMap))
    } yield desResponseWrapper
    result.value
  }

  private def downstreamErrorMap: Map[String, MtdError] = {
    val errors = Map(
      "INVALID_NINO"                      -> NinoFormatError,
      "INVALID_TAXYEAR"                   -> TaxYearFormatError,
      "INVALID_TYPE"                      -> InternalError,
      "INVALID_PAYLOAD"                   -> InternalError,
      "NOT_FOUND_INCOME_SOURCE"           -> NotFoundError,
      "MISSING_CHARITIES_NAME_GIFT_AID"   -> InternalError,
      "MISSING_GIFT_AID_AMOUNT"           -> InternalError,
      "MISSING_CHARITIES_NAME_INVESTMENT" -> InternalError,
      "MISSING_INVESTMENT_AMOUNT"         -> InternalError,
      "INVALID_ACCOUNTING_PERIOD"         -> RuleTaxYearNotSupportedError,
      "GONE"                              -> InternalError,
      "NOT_FOUND"                         -> NotFoundError,
      "SERVER_ERROR"                      -> InternalError,
      "SERVICE_UNAVAILABLE"               -> InternalError
    )

    val extraTysErrors = Map(
      "INVALID_TAX_YEAR"           -> TaxYearFormatError,
      "INCOME_SOURCE_NOT_FOUND"    -> NotFoundError,
      "INVALID_INCOMESOURCE_TYPE"  -> InternalError,
      "INVALID_CORRELATIONID"      -> InternalError,
      "INCOMPATIBLE_INCOME_SOURCE" -> InternalError,
      "TAX_YEAR_NOT_SUPPORTED"     -> RuleTaxYearNotSupportedError
    )
    errors ++ extraTysErrors
  }

}
