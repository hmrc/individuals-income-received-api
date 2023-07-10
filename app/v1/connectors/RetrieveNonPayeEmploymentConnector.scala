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

import api.connectors.DownstreamUri.{Api1661Uri, TaxYearSpecificIfsUri}
import api.connectors.httpparsers.StandardDownstreamHttpParser._
import api.connectors.{BaseDownstreamConnector, DownstreamOutcome}
import config.{AppConfig, FeatureSwitches}
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient}
import v1.models.request.retrieveNonPayeEmploymentIncome.RetrieveNonPayeEmploymentIncomeRequest
import v1.models.response.retrieveNonPayeEmploymentIncome.RetrieveNonPayeEmploymentIncomeResponse

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class RetrieveNonPayeEmploymentConnector @Inject() (val http: HttpClient, val appConfig: AppConfig)(implicit featureSwitches: FeatureSwitches)
    extends BaseDownstreamConnector {

  def retrieveNonPayeEmployment(request: RetrieveNonPayeEmploymentIncomeRequest)(implicit
      hc: HeaderCarrier,
      ec: ExecutionContext,
      correlationId: String): Future[DownstreamOutcome[RetrieveNonPayeEmploymentIncomeResponse]] = {

    import request._

    val downstreamUri = if (taxYear.useTaxYearSpecificApi) {
      TaxYearSpecificIfsUri[RetrieveNonPayeEmploymentIncomeResponse](
        s"income-tax/income/employments/non-paye/${taxYear.asTysDownstream}/${nino.value}?view=${source.toDesViewString}"
      )
    } else {
      Api1661Uri[RetrieveNonPayeEmploymentIncomeResponse](
        s"income-tax/income/employments/non-paye/${nino.value}/${taxYear.asMtd}?view=${source.toDesViewString}"
      )
    }

    get(downstreamUri)

  }

}
