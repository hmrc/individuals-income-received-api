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

package v1r7.controllers.requestParsers

import javax.inject.{Inject, Singleton}
import v1r7.controllers.requestParsers.validators.CreateAmendCgtPpdOverridesValidator
import v1r7.models.domain.Nino
import v1r7.models.request.createAmendCgtPpdOverrides._

@Singleton
class CreateAmendCgtPpdOverridesRequestParser @Inject()(val validator: CreateAmendCgtPpdOverridesValidator)
  extends RequestParser[CreateAmendCgtPpdOverridesRawData, CreateAmendCgtPpdOverridesRequest] {

  override protected def requestFor(data: CreateAmendCgtPpdOverridesRawData): CreateAmendCgtPpdOverridesRequest =
    CreateAmendCgtPpdOverridesRequest(Nino(data.nino), data.taxYear, data.body.json.as[CreateAmendCgtPpdOverridesRequestBody])
}
