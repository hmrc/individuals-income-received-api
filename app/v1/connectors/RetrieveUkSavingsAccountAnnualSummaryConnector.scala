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

package v1.connectors

import api.connectors.DownstreamUri.{DesUri, TaxYearSpecificIfsUri}
import api.connectors.{BaseDownstreamConnector, DownstreamOutcome, DownstreamUri}
import config.AppConfig
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient}
import v1.models.request.retrieveUkSavingsAnnualSummary.RetrieveUkSavingsAnnualSummaryRequest
import v1.models.response.retrieveUkSavingsAnnualSummary.DownstreamUkSavingsAnnualIncomeResponse

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class RetrieveUkSavingsAccountAnnualSummaryConnector @Inject() (val http: HttpClient, val appConfig: AppConfig) extends BaseDownstreamConnector {

  def retrieveUkSavingsAccountAnnualSummary(request: RetrieveUkSavingsAnnualSummaryRequest)(implicit
      hc: HeaderCarrier,
      ec: ExecutionContext,
      correlationId: String): Future[DownstreamOutcome[DownstreamUkSavingsAnnualIncomeResponse]] = {

    import api.connectors.httpparsers.StandardDownstreamHttpParser._

    val nino           = request.nino.nino
    val incomeSourceId = request.savingsAccountId

    val downstreamUri: DownstreamUri[DownstreamUkSavingsAnnualIncomeResponse] =
      if (request.taxYear.useTaxYearSpecificApi) {
        TaxYearSpecificIfsUri(s"income-tax/${request.taxYear.asTysDownstream}/$nino/income-source/savings/annual?incomeSourceId=$incomeSourceId")
      } else {
        DesUri(s"income-tax/nino/$nino/income-source/savings/annual/${request.taxYear.asDownstream}?incomeSourceId=$incomeSourceId")
      }

    get(downstreamUri)
  }

}
