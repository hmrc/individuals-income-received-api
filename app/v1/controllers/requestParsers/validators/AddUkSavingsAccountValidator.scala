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

package v1.controllers.requestParsers.validators

import api.controllers.requestParsers.validators.Validator
import api.controllers.requestParsers.validators.validations._
import api.models.errors.MtdError
import v1.models.request.addUkSavingsAccount.{AddUkSavingsAccountRawData, AddUkSavingsAccountRequestBody}

import javax.inject.Singleton

@Singleton
class AddUkSavingsAccountValidator extends Validator[AddUkSavingsAccountRawData] {

  private val validationSet = List(parameterFormatValidation, bodyFormatValidator, bodyValueValidator)

  override def validate(data: AddUkSavingsAccountRawData): List[MtdError] = {
    run(validationSet, data).distinct
  }

  private def parameterFormatValidation: AddUkSavingsAccountRawData => List[List[MtdError]] = { data =>
    List(
      NinoValidation.validate(data.nino)
    )
  }

  private def bodyFormatValidator: AddUkSavingsAccountRawData => List[List[MtdError]] = { data =>
    List(
      JsonFormatValidation.validate[AddUkSavingsAccountRequestBody](data.body.json)
    )
  }

  private def bodyValueValidator: AddUkSavingsAccountRawData => List[List[MtdError]] = { data =>
    val requestBody = data.body.json.as[AddUkSavingsAccountRequestBody]

    List(AccountNameValidation.validate(accountName = requestBody.accountName))
  }

}
