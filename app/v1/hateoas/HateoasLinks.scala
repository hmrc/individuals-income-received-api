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

package v1.hateoas

import config.AppConfig
import v1.models.hateoas.Link
import v1.models.hateoas.Method._
import v1.models.hateoas.RelType._

trait HateoasLinks {

  private def savingsUri(appConfig: AppConfig, nino: String, taxYear: String) =
    s"/${appConfig.apiGatewayContext}/savings/$nino/$taxYear"

  private def insurancePoliciesUri(appConfig: AppConfig, nino: String, taxYear: String) =
    s"/${appConfig.apiGatewayContext}/insurance-policies/$nino/$taxYear"

  private def foreignUri(appConfig: AppConfig, nino: String, taxYear: String) =
    s"/${appConfig.apiGatewayContext}/foreign/$nino/$taxYear"

  private def pensionsUri(appConfig: AppConfig, nino: String, taxYear: String) =
    s"/${appConfig.apiGatewayContext}/pensions/$nino/$taxYear"

  //API resource links

  //Savings Income
  def amendSavings(appConfig: AppConfig, nino: String, taxYear: String): Link =
    Link(
      href = savingsUri(appConfig, nino, taxYear),
      method = PUT,
      rel = AMEND_SAVINGS_INCOME
    )

  def retrieveSavings(appConfig: AppConfig, nino: String, taxYear: String): Link =
    Link(
      href = savingsUri(appConfig, nino, taxYear),
      method = GET,
      rel = SELF
    )

  def deleteSavings(appConfig: AppConfig, nino: String, taxYear: String): Link =
    Link(
      href = savingsUri(appConfig, nino, taxYear),
      method = DELETE,
      rel = DELETE_SAVINGS_INCOME
    )

  //Insurance Policies Income
  def amendInsurancePolicies(appConfig: AppConfig, nino: String, taxYear: String): Link =
    Link(
      href = insurancePoliciesUri(appConfig, nino, taxYear),
      method = PUT,
      rel = AMEND_INSURANCE_POLICIES_INCOME
    )

  def retrieveInsurancePolicies(appConfig: AppConfig, nino: String, taxYear: String): Link =
    Link(
      href = insurancePoliciesUri(appConfig, nino, taxYear),
      method = GET,
      rel = SELF
    )

  def deleteInsurancePolicies(appConfig: AppConfig, nino: String, taxYear: String): Link =
    Link(
      href = insurancePoliciesUri(appConfig, nino, taxYear),
      method = DELETE,
      rel = DELETE_INSURANCE_POLICIES_INCOME
    )

  //Foreign Income
  def amendForeign(appConfig: AppConfig, nino: String, taxYear: String): Link =
    Link(
      href = foreignUri(appConfig, nino, taxYear),
      method = PUT,
      rel = AMEND_FOREIGN_INCOME
    )

  def retrieveForeign(appConfig: AppConfig, nino: String, taxYear: String): Link =
    Link(
      href = foreignUri(appConfig, nino, taxYear),
      method = GET,
      rel = SELF
    )

  def deleteForeign(appConfig: AppConfig, nino: String, taxYear: String): Link =
    Link(
      href = foreignUri(appConfig, nino, taxYear),
      method = DELETE,
      rel = DELETE_FOREIGN_INCOME
    )

  //Pensions Income
  def amendPensions(appConfig: AppConfig, nino: String, taxYear: String): Link =
    Link(
      href = pensionsUri(appConfig, nino, taxYear),
      method = PUT,
      rel = AMEND_PENSIONS_INCOME
    )

  def retrievePensions(appConfig: AppConfig, nino: String, taxYear: String): Link =
    Link(
      href = pensionsUri(appConfig, nino, taxYear),
      method = GET,
      rel = SELF
    )

  def deletePensions(appConfig: AppConfig, nino: String, taxYear: String): Link =
    Link(
      href = pensionsUri(appConfig, nino, taxYear),
      method = DELETE,
      rel = DELETE_PENSIONS_INCOME
    )
}
