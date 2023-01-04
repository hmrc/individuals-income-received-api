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

package v1.connectors

import api.models.domain.{Nino, TaxYear}
import api.models.outcomes.ResponseWrapper
import v1.models.request.retirevePensions.RetrievePensionsRequest
import v1.models.response.retrievePensions.{ForeignPensionsItem, OverseasPensionContributions, RetrievePensionsResponse}

import scala.concurrent.Future

class RetrievePensionsConnectorSpec extends ConnectorSpec {

  val nino: String = "AA111111A"

  private val foreignPensionsItemModel = Seq(
    ForeignPensionsItem(
      countryCode = "DEU",
      amountBeforeTax = Some(100.23),
      taxTakenOff = Some(1.23),
      specialWithholdingTax = Some(2.23),
      foreignTaxCreditRelief = false,
      taxableAmount = 3.23
    ),
    ForeignPensionsItem(
      countryCode = "FRA",
      amountBeforeTax = Some(200.25),
      taxTakenOff = Some(1.27),
      specialWithholdingTax = Some(2.50),
      foreignTaxCreditRelief = true,
      taxableAmount = 3.50
    )
  )

  private val overseasPensionContributionsItemModel = Seq(
    OverseasPensionContributions(
      customerReference = Some("PENSIONINCOME245"),
      exemptEmployersPensionContribs = 200.23,
      migrantMemReliefQopsRefNo = Some("QOPS000000"),
      dblTaxationRelief = Some(4.23),
      dblTaxationCountryCode = Some("FRA"),
      dblTaxationArticle = Some("AB3211-1"),
      dblTaxationTreaty = Some("Treaty"),
      sf74reference = Some("SF74-123456")
    ),
    OverseasPensionContributions(
      customerReference = Some("PENSIONINCOME275"),
      exemptEmployersPensionContribs = 270.50,
      migrantMemReliefQopsRefNo = Some("QOPS000245"),
      dblTaxationRelief = Some(5.50),
      dblTaxationCountryCode = Some("NGA"),
      dblTaxationArticle = Some("AB3477-5"),
      dblTaxationTreaty = Some("Treaty"),
      sf74reference = Some("SF74-1235")
    )
  )

  val retrievePensionsResponse: RetrievePensionsResponse = RetrievePensionsResponse(
    submittedOn = "2020-07-06T09:37:17Z",
    foreignPensions = Some(foreignPensionsItemModel),
    overseasPensionContributions = Some(overseasPensionContributionsItemModel)
  )

  trait Test {
    _: ConnectorTest =>

    def taxYear: String

    val connector: RetrievePensionsConnector = new RetrievePensionsConnector(
      http = mockHttpClient,
      appConfig = mockAppConfig
    )

    lazy val request: RetrievePensionsRequest = RetrievePensionsRequest(Nino(nino), TaxYear.fromMtd(taxYear))
  }

  "RetrievePensionsConnector" when {
    "retrieve" must {
      "return a 200 status for a success scenario" in new IfsTest with Test {

        def taxYear: String = "2021-22"

        val outcome = Right(ResponseWrapper(correlationId, retrievePensionsResponse))

        willGet(url = s"$baseUrl/income-tax/income/pensions/$nino/2021-22")
          .returns(Future.successful(outcome))

        await(connector.retrieve(request)) shouldBe outcome
      }

      "return a 200 status for a success scenario for a TYS tax year" in new TysIfsTest with Test {

        def taxYear: String = "2023-24"

        val outcome = Right(ResponseWrapper(correlationId, retrievePensionsResponse))

        willGet(url = s"$baseUrl/income-tax/income/pensions/23-24/$nino")
          .returns(Future.successful(outcome))

        await(connector.retrieve(request)) shouldBe outcome
      }

    }

  }

}
