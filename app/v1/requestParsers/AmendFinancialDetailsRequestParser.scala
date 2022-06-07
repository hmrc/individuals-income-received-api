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

package v1.requestParsers

import api.models.domain.Nino
import api.requestParsers.RequestParser

import javax.inject.{Inject, Singleton}
import v1.models.request.amendFinancialDetails.{AmendFinancialDetailsRawData, AmendFinancialDetailsRequest, AmendFinancialDetailsRequestBody}
import v1.requestParsers.validators.AmendFinancialDetailsValidator

@Singleton
class AmendFinancialDetailsRequestParser @Inject() (val validator: AmendFinancialDetailsValidator)
    extends RequestParser[AmendFinancialDetailsRawData, AmendFinancialDetailsRequest] {

  override protected def requestFor(data: AmendFinancialDetailsRawData): AmendFinancialDetailsRequest =
    AmendFinancialDetailsRequest(Nino(data.nino), data.taxYear, data.employmentId, data.body.json.as[AmendFinancialDetailsRequestBody])

}