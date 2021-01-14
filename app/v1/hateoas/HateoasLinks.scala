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

  private def otherUri(appConfig: AppConfig, nino: String, taxYear: String) =
    s"/${appConfig.apiGatewayContext}/other/$nino/$taxYear"

  private def otherEmploymentUri(appConfig: AppConfig, nino: String, taxYear: String) =
    s"/${appConfig.apiGatewayContext}/employments/other/$nino/$taxYear"

  private def dividendsUri(appConfig: AppConfig, nino: String, taxYear: String) =
    s"/${appConfig.apiGatewayContext}/dividends/$nino/$taxYear"

  private def employmentUri(appConfig: AppConfig, nino: String, taxYear: String) =
    s"/${appConfig.apiGatewayContext}/employments/$nino/$taxYear"

  private def employmentUriWithId(appConfig: AppConfig, nino: String, taxYear: String, employmentId: String) =
    s"/${appConfig.apiGatewayContext}/employments/$nino/$taxYear/$employmentId"

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

  //Other Income
  def amendOther(appConfig: AppConfig, nino: String, taxYear: String): Link =
    Link(
      href = otherUri(appConfig, nino, taxYear),
      method = PUT,
      rel = AMEND_OTHER_INCOME
    )

  def retrieveOther(appConfig: AppConfig, nino: String, taxYear: String): Link =
    Link(
      href = otherUri(appConfig, nino, taxYear),
      method = GET,
      rel = SELF
    )

  def deleteOther(appConfig: AppConfig, nino: String, taxYear: String): Link =
    Link(
      href = otherUri(appConfig, nino, taxYear),
      method = DELETE,
      rel = DELETE_OTHER_INCOME
    )

  //Other Employment Income
  def amendOtherEmployment(appConfig: AppConfig, nino: String, taxYear: String): Link =
    Link(
      href = otherEmploymentUri(appConfig, nino, taxYear),
      method = PUT,
      rel = AMEND_OTHER_EMPLOYMENT_INCOME
    )

  def retrieveOtherEmployment(appConfig: AppConfig, nino: String, taxYear: String): Link =
    Link(
      href = otherEmploymentUri(appConfig, nino, taxYear),
      method = GET,
      rel = SELF
    )

  def deleteOtherEmployment(appConfig: AppConfig, nino: String, taxYear: String): Link =
    Link(
      href = otherEmploymentUri(appConfig, nino, taxYear),
      method = DELETE,
      rel = DELETE_OTHER_EMPLOYMENT_INCOME
    )

  //Dividends Income
  def amendDividends(appConfig: AppConfig, nino: String, taxYear: String): Link =
    Link(
      href = dividendsUri(appConfig, nino, taxYear),
      method = PUT,
      rel = AMEND_DIVIDENDS_INCOME
    )

  def retrieveDividends(appConfig: AppConfig, nino: String, taxYear: String): Link =
    Link(
      href = dividendsUri(appConfig, nino, taxYear),
      method = GET,
      rel = SELF
    )

  def deleteDividends(appConfig: AppConfig, nino: String, taxYear: String): Link =
    Link(
      href = dividendsUri(appConfig, nino, taxYear),
      method = DELETE,
      rel = DELETE_DIVIDENDS_INCOME
    )

  //Employments
  def listEmployment(appConfig: AppConfig, nino: String, taxYear: String, isSelf: Boolean): Link =
    Link(
      href = employmentUri(appConfig, nino, taxYear),
      method = GET,
      rel = if (isSelf) SELF else LIST_EMPLOYMENTS
    )

  def retrieveEmployment(appConfig: AppConfig, nino: String, taxYear: String, employmentId: String): Link =
    Link(
      href = employmentUriWithId(appConfig, nino, taxYear, employmentId),
      method = GET,
      rel = SELF
    )

  def ignoreEmployment(appConfig: AppConfig, nino: String, taxYear: String, employmentId: String): Link =
    Link(
      href = s"${employmentUriWithId(appConfig, nino, taxYear, employmentId)}/ignore",
      method = PUT,
      rel = IGNORE_EMPLOYMENT
    )

  //Employment
  def addCustomEmployment(appConfig: AppConfig, nino: String, taxYear: String): Link =
    Link(
      href = employmentUri(appConfig, nino, taxYear),
      method = POST,
      rel = ADD_CUSTOM_EMPLOYMENT
    )

  def amendCustomEmployment(appConfig: AppConfig, nino: String, taxYear: String, employmentId: String): Link =
    Link(
      href = employmentUriWithId(appConfig, nino, taxYear, employmentId),
      method = PUT,
      rel = AMEND_CUSTOM_EMPLOYMENT
    )

  def deleteCustomEmployment(appConfig: AppConfig, nino: String, taxYear: String, employmentId: String): Link =
    Link(
      href = employmentUriWithId(appConfig, nino, taxYear, employmentId),
      method = DELETE,
      rel = DELETE_CUSTOM_EMPLOYMENT
    )

  //Financial Details
  def retrieveFinancialDetails(appConfig: AppConfig, nino: String, taxYear: String, employmentId: String): Link =
    Link(
      href = s"${employmentUriWithId(appConfig, nino, taxYear, employmentId)}/financial-details",
      method = GET,
      rel = SELF
    )

  def amendFinancialDetails(appConfig: AppConfig, nino: String, taxYear: String, employmentId: String): Link =
    Link(
      href = s"${employmentUriWithId(appConfig, nino, taxYear, employmentId)}/financial-details",
      method = PUT,
      rel = AMEND_EMPLOYMENT_FINANCIAL_DETAILS
    )

  def deleteFinancialDetails(appConfig: AppConfig, nino: String, taxYear: String, employmentId: String): Link =
    Link(
      href = s"${employmentUriWithId(appConfig, nino, taxYear, employmentId)}/financial-details",
      method = DELETE,
      rel = DELETE_EMPLOYMENT_FINANCIAL_DETAILS
    )
}