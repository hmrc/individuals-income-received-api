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
import api.models.errors.{ErrorWrapper, InternalError, MtdError, NinoFormatError, RuleTaxYearNotSupportedError, TaxYearFormatError}
import api.models.outcomes.ResponseWrapper
import api.services.BaseService
import cats.implicits._
import v1.connectors.AmendOtherEmploymentConnector
import v1.models.request.amendOtherEmployment.AmendOtherEmploymentRequest

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class AmendOtherEmploymentService @Inject() (connector: AmendOtherEmploymentConnector) extends BaseService {

  def amendOtherEmployment(request: AmendOtherEmploymentRequest)(implicit
      ctx: RequestContext,
      ec: ExecutionContext): Future[Either[ErrorWrapper, ResponseWrapper[Unit]]] = {

    connector.amendOtherEmployment(request).map(_.leftMap(mapDownstreamErrors(downstreamErrorMap)))

  }

  private def downstreamErrorMap: Map[String, MtdError] = {
    def errors: Map[String, MtdError] = Map(
      "INVALID_TAXABLE_ENTITY_ID" -> NinoFormatError,
      "INVALID_TAX_YEAR"          -> TaxYearFormatError,
      "INVALID_CORRELATIONID"     -> InternalError,
      "INVALID_PAYLOAD"           -> InternalError,
      "UNPROCESSABLE_ENTITY"      -> InternalError,
      "SERVER_ERROR"              -> InternalError,
      "SERVICE_UNAVAILABLE"       -> InternalError
    )

    def extraTysErrors: Map[String, MtdError] = Map(
      "INVALID_CORRELATION_ID" -> InternalError,
      "TAX_YEAR_NOT_SUPPORTED" -> RuleTaxYearNotSupportedError
    )

    errors ++ extraTysErrors
  }

}
