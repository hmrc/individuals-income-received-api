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

import api.controllers.{EndpointLogContext, RequestContext}
import api.models.errors._
import api.models.outcomes.ResponseWrapper
import api.services.{BaseService, ServiceOutcome}
import cats.data.EitherT
import v1.connectors.RetrieveUkSavingsAccountAnnualSummaryConnector
import v1.models.request.retrieveUkSavingsAnnualSummary.RetrieveUkSavingsAnnualSummaryRequest
import v1.models.response.retrieveUkSavingsAnnualSummary.{DownstreamUkSavingsAnnualIncomeResponse, RetrieveUkSavingsAnnualSummaryResponse}

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class RetrieveUkSavingsAccountAnnualSummaryService @Inject() (connector: RetrieveUkSavingsAccountAnnualSummaryConnector) extends BaseService {

  def retrieveUkSavingsAccountAnnualSummary(request: RetrieveUkSavingsAnnualSummaryRequest)(implicit
      ctx: RequestContext,
      ec: ExecutionContext): Future[ServiceOutcome[RetrieveUkSavingsAnnualSummaryResponse]] = {

    val result = for {
      downstreamResponseWrapper <- EitherT(connector.retrieveUkSavingsAccountAnnualSummary(request)).leftMap(mapDownstreamErrors(downstreamErrorMap))
      mtdResponseWrapper        <- EitherT.fromEither[Future](convertToMtd(downstreamResponseWrapper))
    } yield mtdResponseWrapper

    result.value
  }

  private def convertToMtd(downstreamResponseWrapper: ResponseWrapper[DownstreamUkSavingsAnnualIncomeResponse])(implicit
      logContext: EndpointLogContext): ServiceOutcome[RetrieveUkSavingsAnnualSummaryResponse] = {

    downstreamResponseWrapper.responseData.savingsInterestAnnualIncome match {
      case item +: Nil => Right(ResponseWrapper(downstreamResponseWrapper.correlationId, item.toMtd))
      case Nil         => Left(ErrorWrapper(downstreamResponseWrapper.correlationId, NotFoundError, None))

      case _ =>
        logger.info(s"[${logContext.controllerName}] [${logContext.endpointName}] - More than one matching account found")
        Left(ErrorWrapper(downstreamResponseWrapper.correlationId, InternalError, None))
    }
  }

  private val downstreamErrorMap: Map[String, MtdError] = {
    val errors = Map(
      "INVALID_NINO"            -> NinoFormatError,
      "INVALID_TYPE"            -> InternalError,
      "INVALID_TAXYEAR"         -> TaxYearFormatError, //remove once DES to IFS migration complete
      "INVALID_INCOME_SOURCE"   -> SavingsAccountIdFormatError,
      "NOT_FOUND_PERIOD"        -> NotFoundError,
      "NOT_FOUND_INCOME_SOURCE" -> NotFoundError,
      "SERVER_ERROR"            -> InternalError,
      "SERVICE_UNAVAILABLE"     -> InternalError
    )

    val extraTysErrors = Map(
      "INVALID_TAX_YEAR"             -> TaxYearFormatError,
      "INVALID_INCOMESOURCE_ID"      -> SavingsAccountIdFormatError,
      "INVALID_INCOMESOURCE_TYPE"    -> InternalError,
      "SUBMISSION_PERIOD_NOT_FOUND"  -> NotFoundError,
      "INCOME_DATA_SOURCE_NOT_FOUND" -> NotFoundError,
      "TAX_YEAR_NOT_SUPPORTED"       -> RuleTaxYearNotSupportedError
    )

    errors ++ extraTysErrors
  }

}
