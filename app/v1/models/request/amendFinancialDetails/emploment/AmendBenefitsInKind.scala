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

package v1.models.request.amendFinancialDetails.emploment

import play.api.libs.functional.syntax._
import play.api.libs.json.{Format, JsPath, OFormat}

case class AmendBenefitsInKind(accommodation: Option[BigDecimal],
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

object AmendBenefitsInKind {
  val empty: AmendBenefitsInKind = AmendBenefitsInKind(
    None, None, None, None, None, None, None, None, None, None, None, None, None, None,
    None, None, None, None, None, None, None, None, None, None, None, None, None, None
  )

  val firstSegment: OFormat[(
    Option[BigDecimal], Option[BigDecimal], Option[BigDecimal], Option[BigDecimal], Option[BigDecimal], Option[BigDecimal], Option[BigDecimal],
      Option[BigDecimal], Option[BigDecimal], Option[BigDecimal], Option[BigDecimal], Option[BigDecimal], Option[BigDecimal], Option[BigDecimal]
    )] =
    ((JsPath \ "accommodation").formatNullable[BigDecimal] and
      (JsPath \ "assets").formatNullable[BigDecimal] and
      (JsPath \ "assetTransfer").formatNullable[BigDecimal] and
      (JsPath \ "beneficialLoan").formatNullable[BigDecimal] and
      (JsPath \ "car").formatNullable[BigDecimal] and
      (JsPath \ "carFuel").formatNullable[BigDecimal] and
      (JsPath \ "educationalServices").formatNullable[BigDecimal] and
      (JsPath \ "entertaining").formatNullable[BigDecimal] and
      (JsPath \ "expenses").formatNullable[BigDecimal] and
      (JsPath \ "medicalInsurance").formatNullable[BigDecimal] and
      (JsPath \ "telephone").formatNullable[BigDecimal] and
      (JsPath \ "service").formatNullable[BigDecimal] and
      (JsPath \ "taxableExpenses").formatNullable[BigDecimal] and
      (JsPath \ "van").formatNullable[BigDecimal]).tupled

  val secondSegment: OFormat[(
    Option[BigDecimal], Option[BigDecimal], Option[BigDecimal], Option[BigDecimal], Option[BigDecimal], Option[BigDecimal], Option[BigDecimal],
      Option[BigDecimal], Option[BigDecimal], Option[BigDecimal], Option[BigDecimal], Option[BigDecimal], Option[BigDecimal], Option[BigDecimal]
    )] =
    ((JsPath \ "vanFuel").formatNullable[BigDecimal] and
      (JsPath \ "mileage").formatNullable[BigDecimal] and
      (JsPath \ "nonQualifyingRelocationExpenses").formatNullable[BigDecimal] and
      (JsPath \ "nurseryPlaces").formatNullable[BigDecimal] and
      (JsPath \ "otherItems").formatNullable[BigDecimal] and
      (JsPath \ "paymentsOnEmployeesBehalf").formatNullable[BigDecimal] and
      (JsPath \ "personalIncidentalExpenses").formatNullable[BigDecimal] and
      (JsPath \ "qualifyingRelocationExpenses").formatNullable[BigDecimal] and
      (JsPath \ "employerProvidedProfessionalSubscriptions").formatNullable[BigDecimal]  and
      (JsPath \ "employerProvidedServices").formatNullable[BigDecimal] and
      (JsPath \ "incomeTaxPaidByDirector").formatNullable[BigDecimal] and
      (JsPath \ "travelAndSubsistence").formatNullable[BigDecimal] and
      (JsPath \ "vouchersAndCreditCards").formatNullable[BigDecimal] and
      (JsPath \ "nonCash").formatNullable[BigDecimal]).tupled

  implicit val format: Format[AmendBenefitsInKind] = (firstSegment and secondSegment)({
    case ((accommodation, assets, assetTransfer, beneficialLoan, car, carFuel, educationalServices,
    entertaining, expenses, medicalInsurance, telephone, service, taxableExpenses, van),
    (vanFuel, mileage, nonQualifyingRelocationExpenses, nurseryPlaces, otherItems,
    paymentsOnEmployeesBehalf, personalIncidentalExpenses, qualifyingRelocationExpenses, employerProvidedProfessionalSubscriptions,
    employerProvidedServices, incomeTaxPaidByDirector, travelAndSubsistence, vouchersAndCreditCards, nonCash)) =>

      AmendBenefitsInKind(accommodation, assets, assetTransfer, beneficialLoan, car, carFuel, educationalServices, entertaining, expenses,
        medicalInsurance, telephone, service, taxableExpenses, van, vanFuel, mileage, nonQualifyingRelocationExpenses, nurseryPlaces, otherItems,
        paymentsOnEmployeesBehalf, personalIncidentalExpenses, qualifyingRelocationExpenses, employerProvidedProfessionalSubscriptions,
        employerProvidedServices, incomeTaxPaidByDirector, travelAndSubsistence, vouchersAndCreditCards, nonCash)},

    (amendBenefitsInKind: AmendBenefitsInKind) => (
      (amendBenefitsInKind.accommodation, amendBenefitsInKind.assets, amendBenefitsInKind.assetTransfer,
        amendBenefitsInKind.beneficialLoan, amendBenefitsInKind.car, amendBenefitsInKind.carFuel, amendBenefitsInKind.educationalServices,
        amendBenefitsInKind.entertaining, amendBenefitsInKind.expenses, amendBenefitsInKind.medicalInsurance, amendBenefitsInKind.telephone,
        amendBenefitsInKind.service, amendBenefitsInKind.taxableExpenses, amendBenefitsInKind.van
      ),
        (amendBenefitsInKind.vanFuel, amendBenefitsInKind.mileage, amendBenefitsInKind.nonQualifyingRelocationExpenses, amendBenefitsInKind.nurseryPlaces,
          amendBenefitsInKind.otherItems, amendBenefitsInKind.paymentsOnEmployeesBehalf, amendBenefitsInKind.personalIncidentalExpenses,
          amendBenefitsInKind.qualifyingRelocationExpenses, amendBenefitsInKind.employerProvidedProfessionalSubscriptions,
          amendBenefitsInKind.employerProvidedServices, amendBenefitsInKind.incomeTaxPaidByDirector, amendBenefitsInKind.travelAndSubsistence,
          amendBenefitsInKind.vouchersAndCreditCards, amendBenefitsInKind.nonCash
        )
    )
  )
}