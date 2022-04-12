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
import api.connectors.DownstreamUri.Api1661Uri
import v1.models.request.createAmendNonPayeEmployment.CreateAmendNonPayeEmploymentRequest

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}
import api.connectors.{BaseDownstreamConnector, DownstreamOutcome}

class CreateAmendNonPayeEmploymentConnector @Inject() (val http: HttpClient, val appConfig: AppConfig) extends BaseDownstreamConnector {

  def createAndAmend(request: CreateAmendNonPayeEmploymentRequest)(implicit
      hc: HeaderCarrier,
      ec: ExecutionContext,
      correlationId: String): Future[DownstreamOutcome[Unit]] = {

    import api.connectors.httpparsers.StandardDownstreamHttpParser._

    val nino    = request.nino.nino
    val taxYear = request.taxYear

    put(
      uri = Api1661Uri[Unit](s"income-tax/income/employments/non-paye/$nino/$taxYear"),
      body = request.body
    )
  }

}
