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
import api.services.{BaseService, ServiceOutcome}
import cats.data.EitherT
import config.{AppConfig, FeatureSwitches}
import v1.connectors.RetrieveEmploymentAndFinancialDetailsConnector
import v1.models.request.retrieveFinancialDetails.RetrieveEmploymentAndFinancialDetailsRequest
import v1.models.response.retrieveFinancialDetails.RetrieveEmploymentAndFinancialDetailsResponse

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class RetrieveEmploymentAndFinancialDetailsService @Inject() (connector: RetrieveEmploymentAndFinancialDetailsConnector, appConfig: AppConfig)
    extends BaseService {

  def retrieve(request: RetrieveEmploymentAndFinancialDetailsRequest)(implicit
      ctx: RequestContext,
      ec: ExecutionContext): Future[ServiceOutcome[RetrieveEmploymentAndFinancialDetailsResponse]] = {

    EitherT(connector.retrieve(request))
      .map(_.map(opwResponseMap))
      .leftMap(mapDownstreamErrors(downstreamErrorMap))
      .value
  }

  def opwResponseMap(response: RetrieveEmploymentAndFinancialDetailsResponse): RetrieveEmploymentAndFinancialDetailsResponse = {
    if (!FeatureSwitches(appConfig.featureSwitches).isOpwEnabled) {
      response.copy(employment = response.employment.copy(offPayrollWorker = None))
    } else {
      response
    }
  }

  private val downstreamErrorMap: Map[String, MtdError] = Map(
    "INVALID_TAXABLE_ENTITY_ID" -> NinoFormatError,
    "INVALID_TAX_YEAR"          -> TaxYearFormatError,
    "INVALID_EMPLOYMENT_ID"     -> EmploymentIdFormatError,
    "INVALID_VIEW"              -> SourceFormatError,
    "TAX_YEAR_NOT_SUPPORTED"    -> RuleTaxYearNotSupportedError,
    "INVALID_CORRELATIONID"     -> InternalError,
    "NO_DATA_FOUND"             -> NotFoundError,
    "SERVER_ERROR"              -> InternalError,
    "SERVICE_UNAVAILABLE"       -> InternalError
  )

}
