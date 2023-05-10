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

package v1.models.request.amendFinancialDetails.emploment

import play.api.libs.functional.syntax._
import play.api.libs.json.{Format, Json, OFormat}

/*
Due to limitations with the Play JSON library, it is not possible to create a single formatter for a case class which contains
more than 22 fields. To work around this, along with the main AmendBenefitsInKind case class, we create two smaller case classes,
which each contain a subset of the fields, and have their own formatters. We can then create a formatter for AmendBenefitsInKind
which converts to the intermediate classes (see AmendBenefitsInKind.format).
 */

private case class AmendBenefitsInKindSection1(
    accommodation: Option[BigDecimal],
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
    van: Option[BigDecimal]
)

private case class AmendBenefitsInKindSection2(
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
    nonCash: Option[BigDecimal]
)

case class AmendBenefitsInKind(
    accommodation: Option[BigDecimal],
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
    nonCash: Option[BigDecimal]
) {

  def isEmpty: Boolean =
    accommodation.isEmpty && assets.isEmpty &&
      assetTransfer.isEmpty && beneficialLoan.isEmpty &&
      car.isEmpty && carFuel.isEmpty &&
      educationalServices.isEmpty && entertaining.isEmpty &&
      expenses.isEmpty && medicalInsurance.isEmpty &&
      telephone.isEmpty && service.isEmpty &&
      taxableExpenses.isEmpty && van.isEmpty &&
      vanFuel.isEmpty && mileage.isEmpty &&
      nonQualifyingRelocationExpenses.isEmpty && nurseryPlaces.isEmpty &&
      otherItems.isEmpty && paymentsOnEmployeesBehalf.isEmpty &&
      personalIncidentalExpenses.isEmpty && qualifyingRelocationExpenses.isEmpty &&
      employerProvidedProfessionalSubscriptions.isEmpty && employerProvidedServices.isEmpty &&
      incomeTaxPaidByDirector.isEmpty && travelAndSubsistence.isEmpty &&
      vouchersAndCreditCards.isEmpty && nonCash.isEmpty

}

object AmendBenefitsInKind {

  private val firstSegment: OFormat[AmendBenefitsInKindSection1] = Json.format[AmendBenefitsInKindSection1]

  private val secondSegment: OFormat[AmendBenefitsInKindSection2] = Json.format[AmendBenefitsInKindSection2]

  implicit val format: Format[AmendBenefitsInKind] = (firstSegment and secondSegment)(
    (segment1, segment2) =>
      AmendBenefitsInKind(
        accommodation = segment1.accommodation,
        assets = segment1.assets,
        assetTransfer = segment1.assetTransfer,
        beneficialLoan = segment1.beneficialLoan,
        car = segment1.car,
        carFuel = segment1.carFuel,
        educationalServices = segment1.educationalServices,
        entertaining = segment1.entertaining,
        expenses = segment1.expenses,
        medicalInsurance = segment1.medicalInsurance,
        telephone = segment1.telephone,
        service = segment1.service,
        taxableExpenses = segment1.taxableExpenses,
        van = segment1.van,
        vanFuel = segment2.vanFuel,
        mileage = segment2.mileage,
        nonQualifyingRelocationExpenses = segment2.nonQualifyingRelocationExpenses,
        nurseryPlaces = segment2.nurseryPlaces,
        otherItems = segment2.otherItems,
        paymentsOnEmployeesBehalf = segment2.paymentsOnEmployeesBehalf,
        personalIncidentalExpenses = segment2.personalIncidentalExpenses,
        qualifyingRelocationExpenses = segment2.qualifyingRelocationExpenses,
        employerProvidedProfessionalSubscriptions = segment2.employerProvidedProfessionalSubscriptions,
        employerProvidedServices = segment2.employerProvidedServices,
        incomeTaxPaidByDirector = segment2.incomeTaxPaidByDirector,
        travelAndSubsistence = segment2.travelAndSubsistence,
        vouchersAndCreditCards = segment2.vouchersAndCreditCards,
        nonCash = segment2.nonCash
      ),
    (amendBenefitsInKind: AmendBenefitsInKind) =>
      (
        AmendBenefitsInKindSection1(
          amendBenefitsInKind.accommodation,
          amendBenefitsInKind.assets,
          amendBenefitsInKind.assetTransfer,
          amendBenefitsInKind.beneficialLoan,
          amendBenefitsInKind.car,
          amendBenefitsInKind.carFuel,
          amendBenefitsInKind.educationalServices,
          amendBenefitsInKind.entertaining,
          amendBenefitsInKind.expenses,
          amendBenefitsInKind.medicalInsurance,
          amendBenefitsInKind.telephone,
          amendBenefitsInKind.service,
          amendBenefitsInKind.taxableExpenses,
          amendBenefitsInKind.van
        ),
        AmendBenefitsInKindSection2(
          amendBenefitsInKind.vanFuel,
          amendBenefitsInKind.mileage,
          amendBenefitsInKind.nonQualifyingRelocationExpenses,
          amendBenefitsInKind.nurseryPlaces,
          amendBenefitsInKind.otherItems,
          amendBenefitsInKind.paymentsOnEmployeesBehalf,
          amendBenefitsInKind.personalIncidentalExpenses,
          amendBenefitsInKind.qualifyingRelocationExpenses,
          amendBenefitsInKind.employerProvidedProfessionalSubscriptions,
          amendBenefitsInKind.employerProvidedServices,
          amendBenefitsInKind.incomeTaxPaidByDirector,
          amendBenefitsInKind.travelAndSubsistence,
          amendBenefitsInKind.vouchersAndCreditCards,
          amendBenefitsInKind.nonCash
        )
      )
  )

}
