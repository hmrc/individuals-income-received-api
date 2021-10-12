/*
 * Copyright 2021 HM Revenue & Customs
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

package v1r7.connectors

import config.AppConfig

import javax.inject.{Inject, Singleton}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.http.HttpClient
import v1r7.connectors.DownstreamUri.IfsUri
import v1r7.models.request.amendFinancialDetails.AmendFinancialDetailsRequest

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class AmendFinancialDetailsConnector @Inject()(val http: HttpClient,
                                               val appConfig: AppConfig) extends BaseDownstreamConnector {

  def amendFinancialDetails(request: AmendFinancialDetailsRequest)(
    implicit hc: HeaderCarrier,
    ec: ExecutionContext,
    correlationId: String): Future[DesOutcome[Unit]] = {

    import v1r7.connectors.httpparsers.StandardDesHttpParser._

    val nino = request.nino.nino
    val taxYear = request.taxYear
    val employmentId = request.employmentId

    put(
      uri = IfsUri[Unit](s"income-tax/income/employments/$nino/$taxYear/$employmentId"), body = request.body
    )
  }
}