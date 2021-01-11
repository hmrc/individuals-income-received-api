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

package v1.controllers.requestParsers.validators.validations

import com.neovisionaries.i18n.CountryCode
import v1.models.errors.{CountryCodeFormatError, CountryCodeRuleError, MtdError}

object CountryCodeValidation {

  def validateOptional(data: Option[String]): List[MtdError] = data match {
    case None => NoValidationErrors
    case Some(value) => validate(value)
  }

  def validate(data: String): List[MtdError] = (CountryCode.getByAlpha3Code(data),data) match {
    case (_: CountryCode,_) => NoValidationErrors
    case (_, code) if code.length == 3 => List(CountryCodeRuleError)
    case _ => List(CountryCodeFormatError)
  }
}
