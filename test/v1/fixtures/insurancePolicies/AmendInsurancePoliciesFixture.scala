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

package v1.fixtures.insurancePolicies

import play.api.libs.json.{JsValue, Json}
import v1.models.request.insurancePolicies.amend.{CapitalRedemption, Foreign, LifeAnnuity, LifeInsurance, VoidedIsa}

object AmendInsurancePoliciesFixture {

  val fullVoidedIsaModel = VoidedIsa(
    customerReference = "INPOLY123A",
    event = Some("Death of spouse"),
    gainAmount = Some(2000.99),
    taxPaid = Some(5000.99),
    yearsHeld = Some(15),
    yearsHeldSinceLastGain = Some(12)
  )

  val fullVoidedIsaJson: JsValue = Json.parse(
    """
      |{
      |    "customerReference": "INPOLY123A",
      |    "event": "Death of spouse",
      |    "gainAmount": 2000.99,
      |    "taxPaid": 5000.99,
      |    "yearsHeld": 15,
      |    "yearsHeldSinceLastGain": 12
      |}
    """.stripMargin
  )

  val minVoidedIsaModel = VoidedIsa(
    customerReference = "INPOLY123A",
    event = None,
    gainAmount = None,
    taxPaid = None,
    yearsHeld = None,
    yearsHeldSinceLastGain = None
  )

  val minVoidedIsaJson: JsValue = Json.parse(
    """
      |{
      |    "customerReference": "INPOLY123A"
      |}
    """.stripMargin
  )


  val fullLifeInsuranceModel = LifeInsurance(
    customerReference = "INPOLY123A",
    event = Some("Death of spouse"),
    gainAmount = Some(2000.99),
    taxPaid = Some(5000.99),
    yearsHeld = Some(15),
    yearsHeldSinceLastGain = Some(12),
    deficiencyRelief = Some(5000.99)
  )

  val fullLifeInsuranceJson: JsValue = Json.parse(
    """
      |{
      |    "customerReference": "INPOLY123A",
      |    "event": "Death of spouse",
      |    "gainAmount": 2000.99,
      |    "taxPaid": 5000.99,
      |    "yearsHeld": 15,
      |    "yearsHeldSinceLastGain": 12,
      |    "deficiencyRelief": 5000.99
      |}
    """.stripMargin
  )

  val minLifeInsuranceModel = LifeInsurance(
    customerReference = "INPOLY123A",
    event = None,
    gainAmount = None,
    taxPaid = None,
    yearsHeld = None,
    yearsHeldSinceLastGain = None,
    deficiencyRelief = None
  )

  val minLifeInsuranceJson: JsValue = Json.parse(
    """
      |{
      |    "customerReference": "INPOLY123A"
      |}
    """.stripMargin
  )

  val fullLifeAnnuityModel = LifeAnnuity(
    customerReference = "INPOLY123A",
    event = Some("Death of spouse"),
    gainAmount = Some(2000.99),
    taxPaid = Some(5000.99),
    yearsHeld = Some(15),
    yearsHeldSinceLastGain = Some(12),
    deficiencyRelief = Some(5000.99)
  )

  val fullLifeAnnuityJson: JsValue = Json.parse(
    """
      |{
      |    "customerReference": "INPOLY123A",
      |    "event": "Death of spouse",
      |    "gainAmount": 2000.99,
      |    "taxPaid": 5000.99,
      |    "yearsHeld": 15,
      |    "yearsHeldSinceLastGain": 12,
      |    "deficiencyRelief": 5000.99
      |}
    """.stripMargin
  )

  val minLifeAnnuityModel = LifeAnnuity(
    customerReference = "INPOLY123A",
    event = None,
    gainAmount = None,
    taxPaid = None,
    yearsHeld = None,
    yearsHeldSinceLastGain = None,
    deficiencyRelief = None
  )

  val minLifeAnnuityJson: JsValue = Json.parse(
    """
      |{
      |    "customerReference": "INPOLY123A"
      |}
    """.stripMargin
  )

  val fullForeignModel = Foreign(
    customerReference = "INPOLY123A",
    gainAmount = Some(2000.99),
    taxPaid = Some(5000.99),
    yearsHeld = Some(15)
  )

  val fullForeignJson: JsValue = Json.parse(
    """
      |{
      |    "customerReference": "INPOLY123A",
      |    "gainAmount": 2000.99,
      |    "taxPaid": 5000.99,
      |    "yearsHeld": 15
      |}
    """.stripMargin
  )

  val minForeignModel = Foreign(
    customerReference = "INPOLY123A",
    gainAmount = None,
    taxPaid = None,
    yearsHeld = None
  )

  val minForeignJson: JsValue = Json.parse(
    """
      |{
      |    "customerReference": "INPOLY123A"
      |}
    """.stripMargin
  )

  val fullCapitalRedemptionModel = CapitalRedemption(
    customerReference = "INPOLY123A",
    event = Some("Death of spouse"),
    gainAmount = Some(2000.99),
    taxPaid = Some(5000.99),
    yearsHeld = Some(15),
    yearsHeldSinceLastGain = Some(12),
    deficiencyRelief = Some(5000.99)
  )

  val fullCapitalRedemptionJson: JsValue = Json.parse(
    """
      |{
      |    "customerReference": "INPOLY123A",
      |    "event": "Death of spouse",
      |    "gainAmount": 2000.99,
      |    "taxPaid": 5000.99,
      |    "yearsHeld": 15,
      |    "yearsHeldSinceLastGain": 12,
      |    "deficiencyRelief": 5000.99
      |}
    """.stripMargin
  )

  val minCapitalRedemptionModel = CapitalRedemption(
    customerReference = "INPOLY123A",
    event = None,
    gainAmount = None,
    taxPaid = None,
    yearsHeld = None,
    yearsHeldSinceLastGain = None,
    deficiencyRelief = None
  )

  val minCapitalRedemptionJson: JsValue = Json.parse(
    """
      |{
      |    "customerReference": "INPOLY123A"
      |}
    """.stripMargin
  )
}
