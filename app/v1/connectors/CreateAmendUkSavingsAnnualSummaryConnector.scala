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
import api.connectors.{BaseDownstreamConnector, DownstreamOutcome}
import api.models.domain.{Nino, TaxYear}
import config.AppConfig
import play.api.http.Status
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient}
import v1.models.request.createAmendUkSavingsAnnualSummary.DownstreamCreateAmendUkSavingsAnnualSummaryBody

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class CreateAmendUkSavingsAnnualSummaryConnector @Inject() (val http: HttpClient, val appConfig: AppConfig) extends BaseDownstreamConnector {

  def createOrAmendUKSavingsAccountSummary(nino: Nino, taxYear: TaxYear, body: DownstreamCreateAmendUkSavingsAnnualSummaryBody)(implicit
      hc: HeaderCarrier,
      cc: ExecutionContext,
      correlationId: String): Future[DownstreamOutcome[Unit]] = {

    import api.connectors.httpparsers.StandardDownstreamHttpParser._

    implicit val successCode: SuccessCode = SuccessCode(Status.OK)

    post(
      uri = DesUri[Unit](s"income-tax/nino/${nino.nino}/income-source/savings/annual/${taxYear.toDownstream}"),
      body = body
    )
  }

}
