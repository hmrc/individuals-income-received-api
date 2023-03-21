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

package api.hateoas

import api.models.hateoas.Link
import api.models.hateoas.Method._
import api.models.hateoas.RelType._
import config.AppConfig

//noinspection ScalaStyle
trait HateoasLinks {

  private def savingsUri(appConfig: AppConfig, nino: String, taxYear: String) =
    s"/${appConfig.apiGatewayContext}/savings/$nino/$taxYear"

  private def savingsUkUri(appConfig: AppConfig, nino: String, taxYear: String, accountId: String) =
    s"/${appConfig.apiGatewayContext}/savings/uk-accounts/$nino/$taxYear/$accountId"

  private def savingsUkUriWithoutTaxYearAndAccountId(appConfig: AppConfig, nino: String) =
    s"/${appConfig.apiGatewayContext}/savings/uk-accounts/$nino"

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

  private def dividendsUkUri(appConfig: AppConfig, nino: String, taxYear: String) =
    s"/${appConfig.apiGatewayContext}/uk-dividends/$nino/$taxYear"

  private def employmentUri(appConfig: AppConfig, nino: String, taxYear: String) =
    s"/${appConfig.apiGatewayContext}/employments/$nino/$taxYear"

  private def employmentUriWithId(appConfig: AppConfig, nino: String, taxYear: String, employmentId: String) =
    s"/${appConfig.apiGatewayContext}/employments/$nino/$taxYear/$employmentId"

  private def otherCgtAndDisposalsUri(appConfig: AppConfig, nino: String, taxYear: String) =
    s"/${appConfig.apiGatewayContext}/disposals/other-gains/$nino/$taxYear"

  private def cgtPpdOverridesUri(appConfig: AppConfig, nino: String, taxYear: String) =
    s"/${appConfig.apiGatewayContext}/disposals/residential-property/$nino/$taxYear/ppd"

  private def nonPayeEmploymentUri(appConfig: AppConfig, nino: String, taxYear: String) =
    s"/${appConfig.apiGatewayContext}/employments/non-paye/$nino/$taxYear"

  private def cgtResidentialPropertyDisposalsAndOverridesUri(appConfig: AppConfig, nino: String, taxYear: String) =
    s"/${appConfig.apiGatewayContext}/disposals/residential-property/$nino/$taxYear"

  private def ukAccountUri(appConfig: AppConfig, nino: String) =
    s"/${appConfig.apiGatewayContext}/savings/uk-accounts/$nino"

  // API resource links

  // Savings Income
  def createAmendSavings(appConfig: AppConfig, nino: String, taxYear: String): Link =
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

  // UK Savings Income
  def createAmendUkSavings(appConfig: AppConfig, nino: String, taxYear: String, accountId: String): Link =
    Link(
      href = savingsUkUri(appConfig, nino, taxYear, accountId),
      method = PUT,
      rel = CREATE_AND_AMEND_UK_SAVINGS_INCOME
    )

  def retrieveUkSavings(appConfig: AppConfig, nino: String, taxYear: String, accountId: String): Link =
    Link(
      href = savingsUkUri(appConfig, nino, taxYear, accountId),
      method = GET,
      rel = SELF
    )

  def addUkSavings(appConfig: AppConfig, nino: String): Link =
    Link(
      href = savingsUkUriWithoutTaxYearAndAccountId(appConfig, nino),
      method = POST,
      rel = ADD_UK_SAVINGS_INCOME
    )

  def listUkSavings(appConfig: AppConfig, nino: String, isSelf: Boolean = false): Link =
    Link(
      href = savingsUkUriWithoutTaxYearAndAccountId(appConfig, nino),
      method = GET,
      rel = if (isSelf) SELF else ADD_UK_SAVINGS_INCOME
    )

  // Insurance Policies Income
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

  // Foreign Income
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

  // Pensions Income
  def createAmendPensions(appConfig: AppConfig, nino: String, taxYear: String): Link =
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

  // Other Income
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

  // Other Employment Income
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

  // Dividends Income
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

  // UK Dividends Income
  def createAmendUkDividends(appConfig: AppConfig, nino: String, taxYear: String): Link =
    Link(
      href = dividendsUkUri(appConfig, nino, taxYear),
      method = PUT,
      rel = CREATE_AND_AMEND_UK_DIVIDENDS_INCOME
    )

  def retrieveUkDividends(appConfig: AppConfig, nino: String, taxYear: String): Link =
    Link(
      href = dividendsUkUri(appConfig, nino, taxYear),
      method = GET,
      rel = SELF
    )

  def deleteUkDividends(appConfig: AppConfig, nino: String, taxYear: String): Link =
    Link(
      href = dividendsUkUri(appConfig, nino, taxYear),
      method = DELETE,
      rel = DELETE_UK_DIVIDENDS_INCOME
    )

  // Employments
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
      method = POST,
      rel = IGNORE_EMPLOYMENT
    )

  def unignoreEmployment(appConfig: AppConfig, nino: String, taxYear: String, employmentId: String): Link =
    Link(
      href = s"${employmentUriWithId(appConfig, nino, taxYear, employmentId)}/unignore",
      method = POST,
      rel = UNIGNORE_EMPLOYMENT
    )

  // Employment
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

  // Financial Details
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

  // Other CGT and Disposals
  def retrieveOtherCgt(appConfig: AppConfig, nino: String, taxYear: String): Link =
    Link(
      href = otherCgtAndDisposalsUri(appConfig, nino, taxYear),
      method = GET,
      rel = SELF
    )

  def createAmendOtherCgt(appConfig: AppConfig, nino: String, taxYear: String): Link =
    Link(
      href = otherCgtAndDisposalsUri(appConfig, nino, taxYear),
      method = PUT,
      rel = CREATE_AND_AMEND_OTHER_CGT_AND_DISPOSALS
    )

  def deleteOtherCgt(appConfig: AppConfig, nino: String, taxYear: String): Link =
    Link(
      href = otherCgtAndDisposalsUri(appConfig, nino, taxYear),
      method = DELETE,
      rel = DELETE_OTHER_CGT_AND_DISPOSALS
    )

  // Retrieve All CGT Residential Property Disposals and Overrides
  def retrieveAllCgtPpdDisposalsOverrides(appConfig: AppConfig, nino: String, taxYear: String): Link =
    Link(
      href = cgtResidentialPropertyDisposalsAndOverridesUri(appConfig, nino, taxYear),
      method = GET,
      rel = SELF
    )

  def createAmendNonPpdCgt(appConfig: AppConfig, nino: String, taxYear: String): Link =
    Link(
      href = cgtResidentialPropertyDisposalsAndOverridesUri(appConfig, nino, taxYear),
      method = PUT,
      rel = CREATE_AND_AMEND_NON_PPD_CGT_AND_DISPOSALS
    )

  def deleteNonPpdCgt(appConfig: AppConfig, nino: String, taxYear: String): Link =
    Link(
      href = cgtResidentialPropertyDisposalsAndOverridesUri(appConfig, nino, taxYear),
      method = DELETE,
      rel = DELETE_NON_PPD_CGT_AND_DISPOSALS
    )

  // 'Report and Pay Capital Gains Tax on Property' Overrides
  def createAmendCgtPpdOverrides(appConfig: AppConfig, nino: String, taxYear: String): Link =
    Link(
      href = cgtPpdOverridesUri(appConfig, nino, taxYear),
      method = PUT,
      rel = CREATE_AND_AMEND_CGT_PPD_OVERRIDES
    )

  def deleteCgtPpdOverrides(appConfig: AppConfig, nino: String, taxYear: String): Link =
    Link(
      href = cgtPpdOverridesUri(appConfig, nino, taxYear),
      method = DELETE,
      rel = DELETE_CGT_PPD_OVERRIDES
    )

  // Non-PAYE Employment Income
  def retrieveNonPayeEmployment(appConfig: AppConfig, nino: String, taxYear: String): Link =
    Link(
      href = nonPayeEmploymentUri(appConfig, nino, taxYear),
      method = GET,
      rel = SELF
    )

  def createAmendNonPayeEmployment(appConfig: AppConfig, nino: String, taxYear: String): Link =
    Link(
      href = nonPayeEmploymentUri(appConfig, nino, taxYear),
      method = PUT,
      rel = CREATE_AND_AMEND_NON_PAYE_EMPLOYMENT
    )

  def deleteNonPayeEmployment(appConfig: AppConfig, nino: String, taxYear: String): Link =
    Link(
      href = nonPayeEmploymentUri(appConfig, nino, taxYear),
      method = DELETE,
      rel = DELETE_NON_PAYE_EMPLOYMENT
    )

  // UK Savings Account

  def listUkSavings(appConfig: AppConfig, nino: String): Link =
    Link(
      href = ukAccountUri(appConfig, nino),
      method = GET,
      rel = LIST_UK_SAVINGS
    )

}
