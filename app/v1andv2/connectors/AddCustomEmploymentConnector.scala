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

package v1andv2.connectors

import api.connectors.DownstreamUri.Api1661Uri
import api.connectors.{BaseDownstreamConnector, DownstreamOutcome}
import config.AppConfig
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient}
import v1andv2.models.request.addCustomEmployment.AddCustomEmploymentRequest
import v1andv2.models.response.addCustomEmployment.AddCustomEmploymentResponse

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class AddCustomEmploymentConnector @Inject() (val http: HttpClient, val appConfig: AppConfig) extends BaseDownstreamConnector {

  def addEmployment(request: AddCustomEmploymentRequest)(implicit
      hc: HeaderCarrier,
      ec: ExecutionContext,
      correlationId: String): Future[DownstreamOutcome[AddCustomEmploymentResponse]] = {

    import api.connectors.httpparsers.StandardDownstreamHttpParser._

    val nino    = request.nino.nino
    val taxYear = request.taxYear

    post(request.body, Api1661Uri[AddCustomEmploymentResponse](s"income-tax/income/employments/$nino/$taxYear/custom"))
  }

}
