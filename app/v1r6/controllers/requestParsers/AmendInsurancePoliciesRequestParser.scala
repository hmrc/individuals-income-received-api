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

package v1r6.controllers.requestParsers

import javax.inject.{Inject, Singleton}
import v1r6.models.domain.Nino
import v1r6.controllers.requestParsers.validators.AmendInsurancePoliciesValidator
import v1r6.models.request.amendInsurancePolicies.{AmendInsurancePoliciesRawData, AmendInsurancePoliciesRequest, AmendInsurancePoliciesRequestBody}

@Singleton
class AmendInsurancePoliciesRequestParser @Inject()(val validator: AmendInsurancePoliciesValidator)
  extends RequestParser[AmendInsurancePoliciesRawData, AmendInsurancePoliciesRequest] {

  override protected def requestFor(data: AmendInsurancePoliciesRawData): AmendInsurancePoliciesRequest =
    AmendInsurancePoliciesRequest(Nino(data.nino), data.taxYear, data.body.json.as[AmendInsurancePoliciesRequestBody])
}