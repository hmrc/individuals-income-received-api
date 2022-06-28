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

import api.connectors.DownstreamUri.DesUri
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient}

import javax.inject.{Inject, Singleton}
import config.AppConfig
import api.connectors.{BaseDownstreamConnector, DownstreamOutcome}
import api.connectors.DownstreamUri.DesUri

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class RetrieveUkSavingsAccountAnnualSummaryConnector {

  def retrieveUkSavingsAccountAnnualSummary(request: RetrieveUkSavingsAccountAnnualSummaryRequest)(implicit
                                                                                                   hc: HeaderCarrier,
                                                                                                   ec: ExecutionContext,
                                                                                                   correlationId: String): Future[DownstreamOutcome[RetrieveUkSavingsAccountAnnualSummaryResponse]] = {

    import api.connectors.httpparsers.StandardDownstreamHttpParser._

    val nino           = request.nino.nino
    val taxYear        = request.taxYear.toDownstream
    val incomeSourceId = request.savingsAccountId

    get(
      DesUri[RetrieveUkSavingsAccountAnnualSummaryResponse](s"income-tax/nino/$nino/income-source/savings/annual/$taxYear?incomeSourceId=$incomeSourceId")
    )
  }

}
