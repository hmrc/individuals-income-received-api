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

package v1.requestParsers.validators

import api.models.errors.MtdError
import api.requestParsers.validators.Validator
import javax.inject.Singleton
import v1.models.request.addUkSavings.{AddUkSavingsRawData, AddUkSavingsRequestBody}
import v1.requestParsers.validators.validations._

@Singleton
class AddUkSavingsAccountValidator extends Validator[AddUkSavingsRawData] {

  private val validationSet = List(parameterFormatValidation, bodyFormatValidator, bodyValueValidator)

  override def validate(data: AddUkSavingsRawData): List[MtdError] = {
    run(validationSet, data).distinct
  }

  private def parameterFormatValidation: AddUkSavingsRawData => List[List[MtdError]] = { data =>
    List(
      NinoValidation.validate(data.nino)
    )
  }

  private def bodyFormatValidator: AddUkSavingsRawData => List[List[MtdError]] = { data =>
    List(
      JsonFormatValidation.validate[AddUkSavingsRequestBody](data.body.json)
    )
  }

  private def bodyValueValidator: AddUkSavingsRawData => List[List[MtdError]] = { data =>
    val requestBody = data.body.json.as[AddUkSavingsRequestBody]

    List(AccountNameValidation.validate(accountName = requestBody.accountName))
  }

}
