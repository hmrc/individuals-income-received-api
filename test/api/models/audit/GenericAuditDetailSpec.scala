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

package api.models.audit

import api.models.errors.TaxYearFormatError
import play.api.http.Status.{BAD_REQUEST, OK}
import play.api.libs.json.{JsValue, Json}
import support.UnitSpec

class GenericAuditDetailSpec extends UnitSpec {

  val nino: String                         = "XX751130C"
  val taxYear: String                      = "2021-22"
  val employmentId: String                 = "4557ecb5-fd32-48cc-81f5-e6acd1099f3c"
  val userType: String                     = "Agent"
  val agentReferenceNumber: Option[String] = Some("012345678")
  val correlationId: String                = "a1e8057e-fbbc-47a8-a8b4-78d9f015c253"

  val auditDetailJsonSuccess: JsValue = Json.parse(
    s"""
      |{
      |    "userType": "$userType",
      |    "agentReferenceNumber": "${agentReferenceNumber.get}",
      |    "nino": "$nino",
      |    "taxYear": "$taxYear",
      |    "request": {
      |       "employerRef": "123/AB56797",
      |       "employerName": "AMD infotech Ltd",
      |       "startDate": "2019-01-01",
      |       "cessationDate": "2020-06-01",
      |       "payrollId": "124214112412"
      |    },
      |    "X-CorrelationId": "$correlationId",
      |    "response": {
      |      "httpStatus": $OK,
      |      "body": {
      |         "employmentId": "$employmentId",
      |         "links":[
      |           {
      |               "href": "/individuals/income-received/employments/$nino/$taxYear",
      |               "rel": "list-employments",
      |               "method": "GET"
      |           },
      |           {
      |               "href": "/individuals/income-received/employments/$nino/$taxYear/$employmentId",
      |               "rel": "self",
      |               "method": "GET"
      |           },
      |           {
      |               "href": "/individuals/income-received/employments/$nino/$taxYear/$employmentId",
      |               "rel": "amend-custom-employment",
      |               "method": "PUT"
      |           },
      |           {
      |               "href": "/individuals/income-received/employments/$nino/$taxYear/$employmentId",
      |               "rel": "delete-custom-employment",
      |               "method": "DELETE"
      |           }
      |         ]
      |       }
      |    }
      |}
    """.stripMargin
  )

  val auditDetailModelSuccess: GenericAuditDetail = GenericAuditDetail(
    userType = userType,
    agentReferenceNumber = agentReferenceNumber,
    params = Map("nino" -> nino, "taxYear" -> taxYear),
    request = Some(
      Json.parse(
        """
        |{
        |   "employerRef": "123/AB56797",
        |   "employerName": "AMD infotech Ltd",
        |   "startDate": "2019-01-01",
        |   "cessationDate": "2020-06-01",
        |   "payrollId": "124214112412"
        |}
        """.stripMargin
      )),
    `X-CorrelationId` = correlationId,
    response = AuditResponse(
      OK,
      Right(Some(Json.parse(s"""
          |{
          |   "employmentId": "$employmentId",
          |   "links":[
          |      {
          |         "href": "/individuals/income-received/employments/$nino/$taxYear",
          |         "rel": "list-employments",
          |         "method": "GET"
          |      },
          |      {
          |         "href": "/individuals/income-received/employments/$nino/$taxYear/$employmentId",
          |         "rel": "self",
          |         "method": "GET"
          |      },
          |      {
          |         "href": "/individuals/income-received/employments/$nino/$taxYear/$employmentId",
          |         "rel": "amend-custom-employment",
          |         "method": "PUT"
          |      },
          |      {
          |         "href": "/individuals/income-received/employments/$nino/$taxYear/$employmentId",
          |         "rel": "delete-custom-employment",
          |         "method": "DELETE"
          |      }
          |   ]
          |}
        """.stripMargin)))
    )
  )

  val invalidTaxYearAuditDetailJson: JsValue = Json.parse(
    s"""
      |{
      |    "userType": "$userType",
      |    "agentReferenceNumber": "${agentReferenceNumber.get}",
      |    "nino": "$nino",
      |    "taxYear" : "2021-2022",
      |    "request": {
      |       "employerRef": "123/AB56797",
      |       "employerName": "AMD infotech Ltd",
      |       "startDate": "2019-01-01",
      |       "cessationDate": "2020-06-01",
      |       "payrollId": "124214112412"
      |    },
      |    "X-CorrelationId": "$correlationId",
      |    "response": {
      |      "httpStatus": $BAD_REQUEST,
      |      "errors": [
      |        {
      |          "errorCode": "FORMAT_TAX_YEAR"
      |        }
      |      ]
      |    }
      |}
    """.stripMargin
  )

  val invalidTaxYearAuditDetailModel: GenericAuditDetail = GenericAuditDetail(
    userType = userType,
    agentReferenceNumber = agentReferenceNumber,
    params = Map("nino" -> nino, "taxYear" -> "2021-2022"),
    request = Some(
      Json.parse(
        """
        |{
        |   "employerRef": "123/AB56797",
        |   "employerName": "AMD infotech Ltd",
        |   "startDate": "2019-01-01",
        |   "cessationDate": "2020-06-01",
        |   "payrollId": "124214112412"
        |}
      """.stripMargin
      )),
    `X-CorrelationId` = correlationId,
    response = AuditResponse(BAD_REQUEST, Left(Seq(AuditError(TaxYearFormatError.code))))
  )

  "GenericAuditDetail" when {
    "written to JSON (success)" should {
      "produce the expected JsObject" in {
        Json.toJson(auditDetailModelSuccess) shouldBe auditDetailJsonSuccess
      }
    }

    "written to JSON (error)" should {
      "produce the expected JsObject" in {
        Json.toJson(invalidTaxYearAuditDetailModel) shouldBe invalidTaxYearAuditDetailJson
      }
    }
  }

}
