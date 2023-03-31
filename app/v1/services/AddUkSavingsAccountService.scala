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

import api.controllers.RequestContext
import api.models.errors._
import api.services.BaseService
import cats.implicits._
import v1.connectors.AddUkSavingsAccountConnector
import v1.models.request.addUkSavingsAccount.AddUkSavingsAccountRequest

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class AddUkSavingsAccountService @Inject() (connector: AddUkSavingsAccountConnector) extends BaseService {

  def addSavings(
      request: AddUkSavingsAccountRequest)(implicit ctx: RequestContext, ec: ExecutionContext): Future[AddUkSavingsAccountServiceOutcome] = {

    connector.addSavings(request).map(_.leftMap(mapDownstreamErrors(downstreamErrorMap)))
  }

  private val downstreamErrorMap: Map[String, MtdError] =
    Map(
      "INVALID_IDVALUE"                  -> NinoFormatError,
      "MAX_ACCOUNTS_REACHED"             -> RuleMaximumSavingsAccountsLimitError,
      "ALREADY_EXISTS"                   -> RuleDuplicateAccountNameError,
      "INVALID_IDTYPE"                   -> InternalError,
      "INVALID_PAYLOAD"                  -> InternalError,
      "SERVER_ERROR"                     -> InternalError,
      "SERVICE_UNAVAILABLE"              -> InternalError
    )

}
