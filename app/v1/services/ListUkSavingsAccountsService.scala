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
import api.models.errors.{ErrorWrapper, MtdError, NinoFormatError, NotFoundError, SavingsAccountIdFormatError, InternalError}
import api.models.outcomes.ResponseWrapper
import api.support.DownstreamResponseMappingSupport
import cats.data.EitherT
import cats.implicits._

import javax.inject.{Inject, Singleton}
import uk.gov.hmrc.http.HeaderCarrier
import utils.Logging
import v1.connectors.ListUkSavingsAccountsConnector
import v1.models.request.listUkSavingsAccounts.ListUkSavingsAccountsRequest
import v1.models.response.listUkSavingsAccounts.{ListUkSavingsAccountsResponse, UkSavingsAccount}

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class ListUkSavingsAccountsService @Inject() (connector: ListUkSavingsAccountsConnector) extends DownstreamResponseMappingSupport with Logging {

  def listUkSavingsAccounts(request: ListUkSavingsAccountsRequest)(implicit
      hc: HeaderCarrier,
      ec: ExecutionContext,
      logContext: EndpointLogContext,
      correlationId: String): Future[Either[ErrorWrapper, ResponseWrapper[ListUkSavingsAccountsResponse[UkSavingsAccount]]]] = {

    EitherT(connector.listUkSavingsAccounts(request))
      .leftMap(mapDownstreamErrors(mappingDesToMtdError))
      .value
  }

  private def mappingDesToMtdError: Map[String, MtdError] = Map(
    "INVALID_ID_TYPE"          -> InternalError,
    "INVALID_IDVALUE"          -> NinoFormatError,
    "INVALID_INCOMESOURCETYPE" -> InternalError,
    "INVALID_TAXYEAR"          -> InternalError, // Is tech spec correct here?
    "INVALID_INCOMESOURCEID"   -> SavingsAccountIdFormatError,
    "INVALID_ENDDATE"          -> InternalError,
    "NOT_FOUND"                -> NotFoundError,
    "SERVER_ERROR"             -> InternalError,
    "SERVICE_UNAVAILABLE"      -> InternalError
  )

}
