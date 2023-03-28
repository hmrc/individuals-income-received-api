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

import api.models.errors.{MtdError, SchemePlanTypeFormatError}

object SchemePlanTypeValidation {

  val schemeEnumsShareOptions: List[String] = List("EMI", "CSOP", "SAYE", "Other")
  val schemeEnumsShareAwarded: List[String] = List("SIP", "Other")

  def validate(schemePlanType: String, awarded: Boolean): List[MtdError] = {
    if (!awarded && schemeEnumsShareOptions.contains(schemePlanType)) { NoValidationErrors }
    else if (awarded && schemeEnumsShareAwarded.contains(schemePlanType)) { NoValidationErrors }
    else { List(SchemePlanTypeFormatError) }
  }

}
