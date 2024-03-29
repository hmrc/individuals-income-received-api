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

package v2.controllers.requestParsers

import api.controllers.requestParsers.RequestParser
import api.models.domain.Nino
import v2.controllers.requestParsers.validators.AmendCustomEmploymentValidator
import v2.models.request.amendCustomEmployment.{AmendCustomEmploymentRawData, AmendCustomEmploymentRequest, AmendCustomEmploymentRequestBody}

import javax.inject.{Inject, Singleton}

@Singleton
class AmendCustomEmploymentRequestParser @Inject()(val validator: AmendCustomEmploymentValidator)
    extends RequestParser[AmendCustomEmploymentRawData, AmendCustomEmploymentRequest] {

  override protected def requestFor(data: AmendCustomEmploymentRawData): AmendCustomEmploymentRequest =
    AmendCustomEmploymentRequest(Nino(data.nino), data.taxYear, data.employmentId, data.body.json.as[AmendCustomEmploymentRequestBody])

}
