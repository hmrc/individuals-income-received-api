/*
 * Copyright 2020 HM Revenue & Customs
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

package v1.models.request.amendCustomEmployment

import play.api.libs.json.{JsPath, Json, OWrites, Reads}
import play.api.libs.functional.syntax._

case class AmendCustomEmploymentRequestBody(employerRef: Option[String],
                                            employerName: String,
                                            startDate: String,
                                            cessationDate: Option[String],
                                            payrollId: Option[String])

object AmendCustomEmploymentRequestBody {

  implicit val reads: Reads[AmendCustomEmploymentRequestBody] = Json.reads[AmendCustomEmploymentRequestBody]

  implicit val writes: OWrites[AmendCustomEmploymentRequestBody] = (
    (JsPath \ "employerRef").writeNullable[String] and
      (JsPath \ "employerName").write[String] and
      (JsPath \ "startDate").write[String] and
      (JsPath \ "cessationDate").writeNullable[String] and
      (JsPath \ "payrollId").writeNullable[String]
    ) (unlift(AmendCustomEmploymentRequestBody.unapply))
}
