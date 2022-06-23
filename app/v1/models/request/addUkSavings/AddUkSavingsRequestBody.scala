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

package v1.models.request.addUkSavings

import play.api.libs.json.{Json, OWrites, Reads}

case class AddUkSavingsRequestBody(accountName: String)

object AddUkSavingsRequestBody {

  implicit val reads: Reads[AddUkSavingsRequestBody] = Json.reads[AddUkSavingsRequestBody]

  implicit val writes: OWrites[AddUkSavingsRequestBody] = (addUkSavingsRequestBody:AddUkSavingsRequestBody) =>
    Json.obj(
      "incomeSourceType" -> "interest-from-uk-banks",
      "incomeSourceName" -> addUkSavingsRequestBody.accountName
  )

}
