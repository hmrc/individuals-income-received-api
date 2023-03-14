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

trait ValueFormatErrorMessages {

  val ZERO_MINIMUM_INCLUSIVE             = "The value must be between 0 and 99999999999.99"
  val ZERO_MINIMUM_INTEGER_INCLUSIVE     = "The value must be between 0 and 99"
  val ZERO_MINIMUM_BIG_INTEGER_INCLUSIVE = "The value must be 0 or more"
  val BIG_DECIMAL_MINIMUM_INCLUSIVE      = "The value must be between -99999999999.99 and 99999999999.99"
}
