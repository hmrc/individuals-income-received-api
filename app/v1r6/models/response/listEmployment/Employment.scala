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

package v1r6.models.response.listEmployment

import play.api.libs.functional.syntax._
import play.api.libs.json.{JsPath, Json, OWrites, Reads}
import v1r6.hateoas.HateoasLinks

case class Employment(employmentId: String,
                      employerName: String,
                      dateIgnored: Option[String] = None)

object Employment extends HateoasLinks {

  implicit val writes: OWrites[Employment] = Json.writes[Employment]

  implicit val reads: Reads[Employment] = (
      (JsPath \ "employmentId").read[String] and
        (JsPath \ "employerName").read[String] and
        (JsPath \ "dateIgnored").readNullable[String]
      )(Employment.apply _)
}
