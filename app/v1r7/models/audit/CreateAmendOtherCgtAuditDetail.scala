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

package v1r7.models.audit

import api.models.audit.AuditResponse
import api.models.auth.UserDetails
import play.api.libs.json.{JsValue, Json, Writes}

case class CreateAmendOtherCgtAuditDetail(userType: String,
                                          agentReferenceNumber: Option[String],
                                          nino: String,
                                          taxYear: String,
                                          request: JsValue,
                                          `X-CorrelationId`: String,
                                          response: AuditResponse)


object CreateAmendOtherCgtAuditDetail {
  implicit val writes: Writes[CreateAmendOtherCgtAuditDetail] = Json.writes[CreateAmendOtherCgtAuditDetail]

  def apply(userDetails: UserDetails,
            nino: String,
            taxYear: String,
            request: JsValue,
            `X-CorrelationId`: String,
            auditResponse: AuditResponse): CreateAmendOtherCgtAuditDetail = {

    CreateAmendOtherCgtAuditDetail(
      userType = userDetails.userType,
      agentReferenceNumber = userDetails.agentReferenceNumber,
      nino = nino,
      taxYear = taxYear,
      request = request,
      `X-CorrelationId`,
      auditResponse
    )
  }
}
