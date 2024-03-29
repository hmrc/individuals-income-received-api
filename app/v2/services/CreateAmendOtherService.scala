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

package v2.services

import api.controllers.RequestContext
import api.models.errors._
import api.services.{BaseService, ServiceOutcome}
import cats.implicits._
import config.{AppConfig, FeatureSwitches}
import v2.connectors.CreateAmendOtherConnector
import v2.models.request.createAmendOther.CreateAmendOtherRequest

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class CreateAmendOtherService @Inject() (connector: CreateAmendOtherConnector, appConfig: AppConfig) extends BaseService {

  def createAmend(request: CreateAmendOtherRequest)(implicit ctx: RequestContext, ec: ExecutionContext): Future[ServiceOutcome[Unit]] = {

    val featureSwitchedRequest = if (FeatureSwitches(appConfig.featureSwitches).isPostCessationReceiptsEnabled) {
      request
    } else {
      request.copy(body = request.body.copy(postCessationReceipts = None))
    }

    connector.createAmend(featureSwitchedRequest).map(_.leftMap(mapDownstreamErrors(downstreamErrorMap)))
  }

  private val downstreamErrorMap: Map[String, MtdError] = {
    val errors = Map(
      "INVALID_TAXABLE_ENTITY_ID"    -> NinoFormatError,
      "INVALID_TAX_YEAR"             -> TaxYearFormatError,
      "INVALID_CORRELATIONID"        -> InternalError,
      "INVALID_PAYLOAD"              -> InternalError,
      "SERVER_ERROR"                 -> InternalError,
      "SERVICE_UNAVAILABLE"          -> InternalError,
      "UNALIGNED_CESSATION_TAX_YEAR" -> RuleUnalignedCessationTaxYear
    )

    val extraTysErrors = Map(
      "INVALID_CORRELATION_ID" -> InternalError,
      "TAX_YEAR_NOT_SUPPORTED" -> RuleTaxYearNotSupportedError
    )

    errors ++ extraTysErrors
  }

}
