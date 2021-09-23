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

package v1.hateoas

import config.AppConfig
import play.api.libs.json.{JsValue, Json}

trait AmendHateoasBody extends HateoasLinks {

  def amendSavingsHateoasBody(appConfig: AppConfig, nino: String, taxYear: String): JsValue = {

    val links = Seq(
      amendSavings(appConfig, nino, taxYear),
      retrieveSavings(appConfig, nino, taxYear),
      deleteSavings(appConfig, nino, taxYear)
    )

    Json.obj("links" -> links)
  }

  def amendInsurancePoliciesHateoasBody(appConfig: AppConfig, nino: String, taxYear: String): JsValue = {

    val links = Seq(
      amendInsurancePolicies(appConfig, nino, taxYear),
      retrieveInsurancePolicies(appConfig, nino, taxYear),
      deleteInsurancePolicies(appConfig, nino, taxYear)
    )

    Json.obj("links" -> links)
  }

  def amendForeignHateoasBody(appConfig: AppConfig, nino: String, taxYear: String): JsValue = {

    val links = Seq(
      amendForeign(appConfig, nino, taxYear),
      retrieveForeign(appConfig, nino, taxYear),
      deleteForeign(appConfig, nino, taxYear)
    )

    Json.obj("links" -> links)
  }

  def amendPensionsHateoasBody(appConfig: AppConfig, nino: String, taxYear: String): JsValue = {

    val links = Seq(
      amendPensions(appConfig, nino, taxYear),
      retrievePensions(appConfig, nino, taxYear),
      deletePensions(appConfig, nino, taxYear)
    )

    Json.obj("links" -> links)
  }

  def amendOtherHateoasBody(appConfig: AppConfig, nino: String, taxYear: String): JsValue = {

    val links = Seq(
      amendOther(appConfig, nino, taxYear),
      retrieveOther(appConfig, nino, taxYear),
      deleteOther(appConfig, nino, taxYear)
    )

    Json.obj("links" -> links)
  }

  def amendOtherEmploymentHateoasBody(appConfig: AppConfig, nino: String, taxYear: String): JsValue = {

    val links = Seq(
      amendOtherEmployment(appConfig, nino, taxYear),
      retrieveOtherEmployment(appConfig, nino, taxYear),
      deleteOtherEmployment(appConfig, nino, taxYear)
    )

    Json.obj("links" -> links)
  }

  def amendDividendsHateoasBody(appConfig: AppConfig, nino: String, taxYear: String): JsValue = {

    val links = Seq(
      amendDividends(appConfig, nino, taxYear),
      retrieveDividends(appConfig, nino, taxYear),
      deleteDividends(appConfig, nino, taxYear)
    )

    Json.obj("links" -> links)
  }

  def amendCustomEmploymentHateoasBody(appConfig: AppConfig, nino: String, taxYear: String, employmentId: String): JsValue = {

    val links = Seq(
      listEmployment(appConfig, nino, taxYear, isSelf = false),
      retrieveEmployment(appConfig, nino, taxYear, employmentId),
      amendCustomEmployment(appConfig, nino, taxYear, employmentId),
      deleteCustomEmployment(appConfig, nino, taxYear, employmentId)
    )

    Json.obj("links" -> links)
  }

  def amendFinancialDetailsHateoasBody(appConfig: AppConfig, nino: String, taxYear: String, employmentId: String): JsValue = {

    val links = Seq(
      retrieveFinancialDetails(appConfig, nino, taxYear, employmentId),
      amendFinancialDetails(appConfig, nino, taxYear, employmentId),
      deleteFinancialDetails(appConfig, nino, taxYear, employmentId)
    )

    Json.obj("links" -> links)
  }
}