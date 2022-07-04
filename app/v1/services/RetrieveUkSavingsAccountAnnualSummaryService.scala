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
import v1.connectors.RetrieveUkSavingsAccountAnnualSummaryConnector
import v1.models.request.retrieveUkSavingsAnnualSummary.RetrieveUkSavingsAnnualSummaryRequest
import v1.models.response.retrieveUkSavingsAnnualSummary.{DownstreamUkSavingsAnnualIncomeResponse, RetrieveUkSavingsAnnualSummaryResponse}

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class RetrieveUkSavingsAccountAnnualSummaryService @Inject() (connector: RetrieveUkSavingsAccountAnnualSummaryConnector)
    extends DownstreamResponseMappingSupport
    with Logging {

  def retrieveUkSavingsAccountAnnualSummary(request: RetrieveUkSavingsAnnualSummaryRequest)(implicit
      hc: HeaderCarrier,
      ec: ExecutionContext,
      logContext: EndpointLogContext,
      correlationId: String): Future[ServiceOutcome[RetrieveUkSavingsAnnualSummaryResponse]] = {

    val result = for {
      downstreamResponseWrapper <- EitherT(connector.retrieveUkSavingsAccountAnnualSummary(request)).leftMap(mapDesErrors(desErrorMap))
      mtdResponseWrapper        <- EitherT.fromEither[Future](convertToMtd(downstreamResponseWrapper))
    } yield mtdResponseWrapper

    result.value
  }

  private def convertToMtd(downstreamResponseWrapper: ResponseWrapper[DownstreamUkSavingsAnnualIncomeResponse])(implicit
      logContext: EndpointLogContext): ServiceOutcome[RetrieveUkSavingsAnnualSummaryResponse] = {
    downstreamResponseWrapper.responseData.savingsInterestAnnualIncome match {
      case item :: Nil => Right(ResponseWrapper(downstreamResponseWrapper.correlationId, item.toMtd))
      case Nil         => Left(ErrorWrapper(downstreamResponseWrapper.correlationId, NotFoundError, None))

      case _ =>
        logger.info(s"[${logContext.controllerName}] [${logContext.endpointName}] - More than one matching account found")
        Left(ErrorWrapper(downstreamResponseWrapper.correlationId, StandardDownstreamError, None))
    }
  }

  private def desErrorMap: Map[String, MtdError] = Map(
    "INVALID_NINO"            -> NinoFormatError,
    "INVALID_TYPE"            -> StandardDownstreamError,
    "INVALID_TAXYEAR"         -> TaxYearFormatError,
    "INVALID_INCOME_SOURCE"   -> SavingsAccountIdFormatError,
    "NOT_FOUND_PERIOD"        -> NotFoundError,
    "NOT_FOUND_INCOME_SOURCE" -> NotFoundError,
    "SERVER_ERROR"            -> StandardDownstreamError,
    "SERVICE_UNAVAILABLE"     -> StandardDownstreamError
  )

}
