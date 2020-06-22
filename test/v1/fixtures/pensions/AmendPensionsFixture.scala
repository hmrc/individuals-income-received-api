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

package v1.fixtures.pensions

import play.api.libs.json.{JsValue, Json}
import v1.models.request.amendPensions.{AmendForeignPensionsItem, AmendOverseasPensionContributionsItem}

object AmendPensionsFixture {

  val foreignPensionsModel = AmendForeignPensionsItem(
      countryCode = "DEU",
      amountBeforeTax = Some(100.23),
      taxTakenOff = Some(1.23),
      specialWithholdingTax = Some(2.23),
      foreignTaxCreditRelief = false,
      taxableAmount = 3.23
  )

  val foreignPensionsModelJson: JsValue = Json.parse(
    """
      |{
      |   "countryCode": "DEU",
      |   "amountBeforeTax": 100.23,
      |   "taxTakenOff": 1.23,
      |   "specialWithholdingTax": 2.23,
      |   "foreignTaxCreditRelief": false,
      |   "taxableAmount": 3.23
      |}
      |""".stripMargin
  )

  val minForeignPensionsModel = AmendForeignPensionsItem(
    countryCode = "DEU",
    amountBeforeTax = None,
    taxTakenOff = None,
    specialWithholdingTax = None,
    foreignTaxCreditRelief = false,
    taxableAmount = 3.23
  )

  val minForeignPensionsModelJson: JsValue = Json.parse(
    """
      |{
      |   "countryCode": "DEU",
      |   "foreignTaxCreditRelief": false,
      |   "taxableAmount": 3.23
      |}
      |""".stripMargin
  )

  val overseasPensionContributionsModel = AmendOverseasPensionContributionsItem(
      customerReference = Some("PENSIONINCOME245"),
      exemptEmployersPensionContribs = 200.23,
      migrantMemReliefQopsRefNo = Some("QOPS000000"),
      dblTaxationRelief = Some(4.23),
      dblTaxationCountryCode = Some("FRA"),
      dblTaxationArticle = Some("AB3211-1"),
      dblTaxationTreaty = Some("Treaty"),
      sf74reference = Some("SF74-123456")
  )

  val overseasPensionContributionsModelJson: JsValue = Json.parse(
    """
      |{
      |   "customerReference": "PENSIONINCOME245",
      |   "exemptEmployersPensionContribs": 200.23,
      |   "migrantMemReliefQopsRefNo": "QOPS000000",
      |   "dblTaxationRelief": 4.23,
      |   "dblTaxationCountryCode": "FRA",
      |   "dblTaxationArticle": "AB3211-1",
      |   "dblTaxationTreaty": "Treaty",
      |   "sf74reference": "SF74-123456"
      |}
      |""".stripMargin
  )

  val minOverseasPensionContributionsModel = AmendOverseasPensionContributionsItem(
    customerReference = None,
    exemptEmployersPensionContribs = 200.23,
    migrantMemReliefQopsRefNo = None,
    dblTaxationRelief = None,
    dblTaxationCountryCode = None,
    dblTaxationArticle = None,
    dblTaxationTreaty = None,
    sf74reference = None
  )

  val minOverseasPensionContributionsModelJson: JsValue = Json.parse(
    """
      |{
      |   "exemptEmployersPensionContribs": 200.23
      |}
      |""".stripMargin
  )
}
