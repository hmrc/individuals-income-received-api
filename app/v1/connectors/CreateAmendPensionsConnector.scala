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

package v1.connectors

import api.connectors.BaseDownstreamConnector
import config.AppConfig
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient}
import api.connectors.DownstreamUri.{IfsUri, TaxYearSpecificIfsUri}
import v1.models.request.createAmendPensions.CreateAmendPensionsRequest

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}
import api.connectors.{BaseDownstreamConnector, DownstreamOutcome}

@Singleton
class CreateAmendPensionsConnector @Inject() (val http: HttpClient, val appConfig: AppConfig) extends BaseDownstreamConnector {

  def amendPensions(request: CreateAmendPensionsRequest)(implicit
      hc: HeaderCarrier,
      ec: ExecutionContext,
      correlationId: String): Future[DownstreamOutcome[Unit]] = {

    import api.connectors.httpparsers.StandardDownstreamHttpParser._

    import request._

    val downstreamUrl = if (taxYear.useTaxYearSpecificApi) {
      TaxYearSpecificIfsUri[Unit](s"income-tax/income/pensions/${taxYear.asTysDownstream}/$nino")
    } else IfsUri[Unit](s"income-tax/income/pensions/$nino/${taxYear.asMtd}")

    put(
      uri = downstreamUrl,
      body = request.body
    )
  }

}
