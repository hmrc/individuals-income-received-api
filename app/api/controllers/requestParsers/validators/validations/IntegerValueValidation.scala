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

package api.controllers.requestParsers.validators.validations

import api.models.errors.{MtdError, ValueFormatError}

object IntegerValueValidation extends ValueFormatErrorMessages {

  def validateOptional(field: Option[Int],
                       minValue: Int = 0,
                       maxValue: Int = 99,
                       path: String,
                       message: String = ZERO_MINIMUM_INTEGER_INCLUSIVE): List[MtdError] = field match {
    case None => NoValidationErrors
    case Some(value) =>
      validate(
        field = value,
        minValue = minValue,
        maxValue = maxValue,
        path = path,
        message = message
      )
  }

  def validate(field: Int, minValue: Int = 0, maxValue: Int = 99, path: String, message: String = ZERO_MINIMUM_INTEGER_INCLUSIVE): List[MtdError] = {

    if (field >= minValue && field <= maxValue) NoValidationErrors
    else
      List(
        ValueFormatError.copy(
          message = message,
          paths = Some(Seq(path))
        )
      )
  }

}
