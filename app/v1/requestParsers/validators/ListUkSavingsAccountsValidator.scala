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

package v1.requestParsers.validators

import api.models.errors.MtdError
import api.requestParsers.validators.Validator
import v1.models.request.listUkSavingsAccounts.ListUkSavingsAccountsRawData
import v1.requestParsers.validators.validations.{NinoValidation, SavingsAccountIdValidation}

import javax.inject.{Inject, Singleton}

@Singleton
class ListUkSavingsAccountsValidator @Inject() extends Validator[ListUkSavingsAccountsRawData] {

  private val validationSet = List(parameterFormatValidation)

  override def validate(data: ListUkSavingsAccountsRawData): List[MtdError] =
    run(validationSet, data).distinct

  private def parameterFormatValidation: ListUkSavingsAccountsRawData => List[List[MtdError]] =
    (data: ListUkSavingsAccountsRawData) => {
      List(
        NinoValidation.validate(data.nino),
        data.savingsAccountId.map(SavingsAccountIdValidation.validate).getOrElse(Nil)
      )
    }

}
