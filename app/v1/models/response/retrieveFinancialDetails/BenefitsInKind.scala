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

package v1.models.response.retrieveFinancialDetails

import play.api.libs.json.{Json, OFormat}

case class BenefitsInKind(accommodation: Option[BigDecimal],
                          assets: Option[BigDecimal],
                          assetTransfer: Option[BigDecimal],
                          beneficialLoan: Option[BigDecimal],
                          car: Option[BigDecimal],
                          carFuel: Option[BigDecimal],
                          educationalServices: Option[BigDecimal],
                          entertaining: Option[BigDecimal],
                          expenses: Option[BigDecimal],
                          medicalInsurance: Option[BigDecimal],
                          telephone: Option[BigDecimal],
                          service: Option[BigDecimal],
                          taxableExpenses: Option[BigDecimal],
                          van: Option[BigDecimal],
                          vanFuel: Option[BigDecimal],
                          mileage: Option[BigDecimal],
                          nonQualifyingRelocationExpenses: Option[BigDecimal],
                          nurseryPlaces: Option[BigDecimal],
                          otherItems: Option[BigDecimal],
                          paymentsOnEmployeesBehalf: Option[BigDecimal],
                          personalIncidentalExpenses: Option[BigDecimal],
                          qualifyingRelocationExpenses: Option[BigDecimal],
                          employerProvidedProfessionalSubscriptions: Option[BigDecimal],
                          employerProvidedServices: Option[BigDecimal],
                          incomeTaxPaidByDirector: Option[BigDecimal],
                          travelAndSubsistence: Option[BigDecimal],
                          vouchersAndCreditCards: Option[BigDecimal],
                          nonCash: Option[BigDecimal])

object BenefitsInKind {
  val empty: BenefitsInKind = BenefitsInKind(
    None, None, None, None, None, None, None, None, None, None, None, None, None, None,
    None, None, None, None, None, None, None, None, None, None, None, None, None, None
  )

  implicit val format: OFormat[BenefitsInKind] = Json.format[BenefitsInKind]
}
