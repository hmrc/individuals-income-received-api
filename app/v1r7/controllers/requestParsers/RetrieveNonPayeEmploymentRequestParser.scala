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

package v1r7.controllers.requestParsers

import v1r7.controllers.requestParsers.validators.RetrieveNonPayeEmploymentValidator
import v1r7.models.domain.{MtdSourceEnum, Nino}
import v1r7.models.request.retrieveNonPayeEmploymentIncome.{RetrieveNonPayeEmploymentIncomeRawData, RetrieveNonPayeEmploymentIncomeRequest}

import javax.inject.Inject

class RetrieveNonPayeEmploymentRequestParser @Inject()(val validator: RetrieveNonPayeEmploymentValidator)
  extends RequestParser[RetrieveNonPayeEmploymentIncomeRawData, RetrieveNonPayeEmploymentIncomeRequest] {
  override protected def requestFor(data: RetrieveNonPayeEmploymentIncomeRawData): RetrieveNonPayeEmploymentIncomeRequest =
    RetrieveNonPayeEmploymentIncomeRequest(Nino(data.nino),
    data.taxYear,
    data.source.flatMap(MtdSourceEnum.parser.lift).getOrElse(MtdSourceEnum.latest))
}