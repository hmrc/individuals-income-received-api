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

package v1.mocks.connectors

import api.connectors.DownstreamOutcome
import api.models.domain.{Nino, TaxYear}
import org.scalamock.handlers.CallHandler
import org.scalamock.scalatest.MockFactory
import uk.gov.hmrc.http.HeaderCarrier
import v1.connectors.CreateAmendUkSavingsAnnualSummaryConnector
import v1.models.request.createAmendUkSavingsAnnualSummary.DownstreamCreateAmendUkSavingsAnnualSummaryBody

import scala.concurrent.{ExecutionContext, Future}

trait MockCreateAmendUkSavingsAnnualSummaryConnector extends MockFactory {

  val mockAmendUkSavingsConnector: CreateAmendUkSavingsAnnualSummaryConnector = mock[CreateAmendUkSavingsAnnualSummaryConnector]

  object MockCreateAmendUkSavingsAnnualSummaryConnector {

    def createOrAmendAnnualSummary(nino: Nino,
                                   taxYear: TaxYear,
                                   body: DownstreamCreateAmendUkSavingsAnnualSummaryBody): CallHandler[Future[DownstreamOutcome[Unit]]] = {
      (
        mockAmendUkSavingsConnector
          .createOrAmendUKSavingsAccountSummary(_: Nino, _: TaxYear, _: DownstreamCreateAmendUkSavingsAnnualSummaryBody)(
            _: HeaderCarrier,
            _: ExecutionContext,
            _: String
          )
        )
        .expects(nino, taxYear, body, *, *, *)
    }

  }

}
