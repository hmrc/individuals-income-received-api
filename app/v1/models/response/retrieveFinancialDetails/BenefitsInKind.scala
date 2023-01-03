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

package v1.models.response.retrieveFinancialDetails

import play.api.libs.functional.syntax._
import play.api.libs.json._

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
    None,
    None,
    None,
    None,
    None,
    None,
    None,
    None,
    None,
    None,
    None,
    None,
    None,
    None,
    None,
    None,
    None,
    None,
    None,
    None,
    None,
    None,
    None,
    None,
    None,
    None,
    None,
    None
  )

  private case class BenefitsInKindPart1(accommodation: Option[BigDecimal],
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
                                         vanFuel: Option[BigDecimal])

  private case class BenefitsInKindPart2(mileage: Option[BigDecimal],
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

  private val formatPt1: OFormat[BenefitsInKindPart1] = Json.format[BenefitsInKindPart1]
  private val formatPt2: OFormat[BenefitsInKindPart2] = Json.format[BenefitsInKindPart2]

  private def buildBenefitsObject(part1: BenefitsInKindPart1, part2: BenefitsInKindPart2): BenefitsInKind = {
    import part1._
    import part2._

    BenefitsInKind(
      accommodation = accommodation,
      assets = assets,
      assetTransfer = assetTransfer,
      beneficialLoan = beneficialLoan,
      car = car,
      carFuel = carFuel,
      educationalServices = educationalServices,
      entertaining = entertaining,
      expenses = expenses,
      medicalInsurance = medicalInsurance,
      telephone = telephone,
      service = service,
      taxableExpenses = taxableExpenses,
      van = van,
      vanFuel = vanFuel,
      mileage = mileage,
      nonQualifyingRelocationExpenses = nonQualifyingRelocationExpenses,
      nurseryPlaces = nurseryPlaces,
      otherItems = otherItems,
      paymentsOnEmployeesBehalf = paymentsOnEmployeesBehalf,
      personalIncidentalExpenses = personalIncidentalExpenses,
      qualifyingRelocationExpenses = qualifyingRelocationExpenses,
      employerProvidedProfessionalSubscriptions = employerProvidedProfessionalSubscriptions,
      employerProvidedServices = employerProvidedServices,
      incomeTaxPaidByDirector = incomeTaxPaidByDirector,
      travelAndSubsistence = travelAndSubsistence,
      vouchersAndCreditCards = vouchersAndCreditCards,
      nonCash = nonCash
    )
  }

  private def splitBenefitsObject(o: BenefitsInKind): (BenefitsInKindPart1, BenefitsInKindPart2) = {
    import o._

    (
      BenefitsInKindPart1(
        accommodation = accommodation,
        assets = assets,
        assetTransfer = assetTransfer,
        beneficialLoan = beneficialLoan,
        car = car,
        carFuel = carFuel,
        educationalServices = educationalServices,
        entertaining = entertaining,
        expenses = expenses,
        medicalInsurance = medicalInsurance,
        telephone = telephone,
        service = service,
        taxableExpenses = taxableExpenses,
        van = van,
        vanFuel = vanFuel
      ),
      BenefitsInKindPart2(
        mileage = mileage,
        nonQualifyingRelocationExpenses = nonQualifyingRelocationExpenses,
        nurseryPlaces = nurseryPlaces,
        otherItems = otherItems,
        paymentsOnEmployeesBehalf = paymentsOnEmployeesBehalf,
        personalIncidentalExpenses = personalIncidentalExpenses,
        qualifyingRelocationExpenses = qualifyingRelocationExpenses,
        employerProvidedProfessionalSubscriptions = employerProvidedProfessionalSubscriptions,
        employerProvidedServices = employerProvidedServices,
        incomeTaxPaidByDirector = incomeTaxPaidByDirector,
        travelAndSubsistence = travelAndSubsistence,
        vouchersAndCreditCards = vouchersAndCreditCards,
        nonCash = nonCash
      )
    )
  }

  implicit val writes: OWrites[BenefitsInKind] = (o: BenefitsInKind) => {
    val splitData = BenefitsInKind.splitBenefitsObject(o)
    Json.toJsObject(splitData._1)(formatPt1) ++ Json.toJsObject(splitData._2)(formatPt2)
  }

  implicit val reads: Reads[BenefitsInKind] = (
    JsPath.read[BenefitsInKindPart1](formatPt1) and
      JsPath.read[BenefitsInKindPart2](formatPt2)
  )(BenefitsInKind.buildBenefitsObject _)

}
