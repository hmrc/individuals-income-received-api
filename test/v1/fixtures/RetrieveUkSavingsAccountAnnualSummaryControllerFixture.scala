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

package v1.fixtures

import play.api.libs.json.{JsObject, JsValue, Json}

object RetrieveUkSavingsAccountAnnualSummaryControllerFixture {

  val mtdRetrieveResponse: JsValue = Json.parse("""
      |{
      |   "taxedUkInterest": 93556675358.99,
      |   "untaxedUkInterest": 34514974058.99
      |   }
      """.stripMargin)

  def mtdRetrieveResponseWithHateaos(nino: String, taxYear: String, savingsAccountId: String): JsValue = mtdRetrieveResponse.as[JsObject] ++ Json
    .parse(
      s"""
      |{
      |"links":[
      |{
      |   "href":"/individuals/income-received/savings/uk-accounts/$nino/$taxYear/$savingsAccountId",
      |   "rel":"create-and-amend-uk-savings-account-annual-summary",
      |   "method":"PUT"
      |},
      |{
      |   "href":"/individuals/income-received/savings/uk-accounts/$nino/$taxYear/$savingsAccountId",
      |   "rel":"self",
      |   "method":"GET"
      |}
      |]
      |}
  """.stripMargin
    )
    .as[JsObject]

}
