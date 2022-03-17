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

package v1r7.requestParsers

import api.models.domain.Nino
import api.requestParsers.RequestParser

import javax.inject.{Inject, Singleton}
import v1r7.models.request.amendSavings.{AmendSavingsRawData, AmendSavingsRequest, AmendSavingsRequestBody}
import v1r7.requestParsers.validators.AmendSavingsValidator

@Singleton
class AmendSavingsRequestParser @Inject()(val validator: AmendSavingsValidator)
  extends RequestParser[AmendSavingsRawData, AmendSavingsRequest] {

  override protected def requestFor(data: AmendSavingsRawData): AmendSavingsRequest =
    AmendSavingsRequest(Nino(data.nino), data.taxYear, data.body.json.as[AmendSavingsRequestBody])
}