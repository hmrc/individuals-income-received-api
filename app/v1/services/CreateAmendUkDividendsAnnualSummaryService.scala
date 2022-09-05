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
import config.AppConfig
import uk.gov.hmrc.http.HeaderCarrier
import utils.Logging
import v1.connectors.CreateAmendUkDividendsAnnualSummaryConnector
import v1.models.request.createAmendUkDividendsIncomeAnnualSummary.CreateAmendUkDividendsIncomeAnnualSummaryRequest

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class CreateAmendUkDividendsAnnualSummaryService @Inject() (connector: CreateAmendUkDividendsAnnualSummaryConnector, appConfig: AppConfig)
    extends DownstreamResponseMappingSupport
    with Logging {

  def createOrAmendAnnualSummary(request: CreateAmendUkDividendsIncomeAnnualSummaryRequest)(implicit
      hc: HeaderCarrier,
      ec: ExecutionContext,
      logContext: EndpointLogContext,
      correlationId: String): Future[Either[ErrorWrapper, ResponseWrapper[Unit]]] = {

    val result = for {
      desResponseWrapper <- EitherT(connector.createOrAmendAnnualSummary(request)).leftMap(mapDownstreamErrors(errorMap))
    } yield desResponseWrapper

    result.value
  }

  private val errorMap: Map[String, MtdError] = {
    val errors = Map(
      "INVALID_NINO"                      -> NinoFormatError,
      "INVALID_TAXYEAR"                   -> TaxYearFormatError,
      "INVALID_TYPE"                      -> StandardDownstreamError,
      "INVALID_PAYLOAD"                   -> StandardDownstreamError,
      "NOT_FOUND_INCOME_SOURCE"           -> NotFoundError,
      "MISSING_CHARITIES_NAME_GIFT_AID"   -> StandardDownstreamError,
      "MISSING_GIFT_AID_AMOUNT"           -> StandardDownstreamError,
      "MISSING_CHARITIES_NAME_INVESTMENT" -> StandardDownstreamError,
      "MISSING_INVESTMENT_AMOUNT"         -> StandardDownstreamError,
      "INVALID_ACCOUNTING_PERIOD"         -> RuleTaxYearNotSupportedError,
      "GONE"                              -> StandardDownstreamError,
      "NOT_FOUND"                         -> NotFoundError,
      "SERVICE_UNAVAILABLE"               -> StandardDownstreamError,
      "SERVER_ERROR"                      -> StandardDownstreamError
    )
    val extraTysErrors = Map(
      "INVALID_TAX_YEAR"           -> TaxYearFormatError,
      "INVALID_INCOMESOURCE_TYPE"  -> StandardDownstreamError,
      "INVALID_CORRELATIONID"      -> StandardDownstreamError,
      "TAX_YEAR_NOT_SUPPORTED"     -> RuleTaxYearNotSupportedError,
      "INCOME_SOURCE_NOT_FOUND"    -> StandardDownstreamError,
      "INCOMPATIBLE_INCOME_SOURCE" -> StandardDownstreamError
    )

    errors ++ extraTysErrors
  }

}
