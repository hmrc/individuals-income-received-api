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

package v1.services

import uk.gov.hmrc.domain.Nino
import v1.controllers.EndpointLogContext
import v1.mocks.connectors.MockAmendOtherEmploymentConnector
import v1.models.errors._
import v1.models.outcomes.ResponseWrapper
import v1.models.request.amendOtherEmployment._

import scala.concurrent.Future

class AmendOtherEmploymentServiceSpec extends ServiceSpec {

  private val nino = "AA112233A"
  private val taxYear = "2019-20"

  private val shareOptionModel = Seq(
    AmendShareOptionItem(
      employerName = "Company Ltd",
      employerRef = Some("AB1321/123"),
      schemePlanType = "EMI",
      dateOfOptionGrant = "2019-11-20",
      dateOfEvent = "2019-12-22",
      optionNotExercisedButConsiderationReceived = true,
      amountOfConsiderationReceived = 23122.22,
      noOfSharesAcquired = 1,
      classOfSharesAcquired = "FIRST",
      exercisePrice = 12.22,
      amountPaidForOption = 123.22,
      marketValueOfSharesOnExcise = 1232.22,
      profitOnOptionExercised = 1232.33,
      employersNicPaid = 2312.22,
      taxableAmount = 2132.22
    ),
    AmendShareOptionItem(
      employerName = "Corp Ltd",
      employerRef = Some("AB1321/345"),
      schemePlanType = "CSOP",
      dateOfOptionGrant = "2019-09-10",
      dateOfEvent = "2019-07-29",
      optionNotExercisedButConsiderationReceived = false,
      amountOfConsiderationReceived = 30000.22,
      noOfSharesAcquired = 5,
      classOfSharesAcquired = "SECOND",
      exercisePrice = 10.50,
      amountPaidForOption = 150.50,
      marketValueOfSharesOnExcise = 2250.22,
      profitOnOptionExercised = 1350.40,
      employersNicPaid = 2450.25,
      taxableAmount = 2140.20
    )
  )

  private val sharesAwardedOrReceivedModel = Seq(
    AmendSharesAwardedOrReceivedItem(
      employerName = "Company Ltd",
      employerRef = Some("AB1321/123"),
      schemePlanType = "SIP",
      dateSharesCeasedToBeSubjectToPlan = "2019-11-10",
      noOfShareSecuritiesAwarded = 11,
      classOfShareAwarded = "FIRST",
      dateSharesAwarded = "2019-12-20",
      sharesSubjectToRestrictions = true,
      electionEnteredIgnoreRestrictions = false,
      actualMarketValueOfSharesOnAward = 2123.22,
      unrestrictedMarketValueOfSharesOnAward = 123.22,
      amountPaidForSharesOnAward = 123.22,
      marketValueAfterRestrictionsLifted = 1232.22,
      taxableAmount = 12321.22
    ),
    AmendSharesAwardedOrReceivedItem(
      employerName = "Corp Ltd",
      employerRef = Some("AB1326/789"),
      schemePlanType = "Other",
      dateSharesCeasedToBeSubjectToPlan = "2019-10-25",
      noOfShareSecuritiesAwarded = 15,
      classOfShareAwarded = "SECOND",
      dateSharesAwarded = "2019-08-23",
      sharesSubjectToRestrictions = true,
      electionEnteredIgnoreRestrictions = false,
      actualMarketValueOfSharesOnAward = 2140.23,
      unrestrictedMarketValueOfSharesOnAward = 125.30,
      amountPaidForSharesOnAward = 124.25,
      marketValueAfterRestrictionsLifted = 1259.40,
      taxableAmount = 12450.30
    )
  )

  private val disabilityModel = AmendCommonOtherEmployment(
    customerReference = Some("customer reference"),
    amountDeducted = 1223.22
  )

  private val foreignServiceModel = AmendCommonOtherEmployment(
    customerReference = Some("cust ref"),
    amountDeducted = 1234.50
  )

  private val taxableLumpSumsAndCertainIncome = AmendTaxableLumpSumsAndCertainIncomeItem(
    amount = 5000.99,
    taxPaid = Some(3333.33),
    taxTakenOffInEmployment = true
  )

  private val benefitFromEmployerFinancedRetirementScheme = AmendBenefitFromEmployerFinancedRetirementSchemeItem(
    amount = 5000.99,
    exemptAmount = Some(2345.99),
    taxPaid = Some(3333.33),
    taxTakenOffInEmployment = true
  )

  private val redundancyCompensationPaymentsOverExemption = AmendRedundancyCompensationPaymentsOverExemptionItem(
    amount = 5000.99,
    taxPaid = Some(3333.33),
    taxTakenOffInEmployment = true
  )

  private val redundancyCompensationPaymentsUnderExemption = AmendRedundancyCompensationPaymentsUnderExemptionItem(
    amount = 5000.99
  )

  private val lumpSumsModel = Seq(
    AmendLumpSums(
      employerName = "BPDTS Ltd",
      employerRef = "123/AB456",
      taxableLumpSumsAndCertainIncome = Some(taxableLumpSumsAndCertainIncome),
      benefitFromEmployerFinancedRetirementScheme = Some(benefitFromEmployerFinancedRetirementScheme),
      redundancyCompensationPaymentsOverExemption = Some(redundancyCompensationPaymentsOverExemption),
      redundancyCompensationPaymentsUnderExemption = Some(redundancyCompensationPaymentsUnderExemption)
    )
  )

  val amendOtherEmploymentRequest: AmendOtherEmploymentRequest = AmendOtherEmploymentRequest(
      nino = Nino(nino),
      taxYear = taxYear,
      body = AmendOtherEmploymentRequestBody(
        shareOption = Some(shareOptionModel),
        sharesAwardedOrReceived = Some(sharesAwardedOrReceivedModel),
        disability = Some(disabilityModel),
        foreignService = Some(foreignServiceModel),
        lumpSums = Some(lumpSumsModel)
      )
  )

  trait Test extends MockAmendOtherEmploymentConnector{
    implicit val logContext: EndpointLogContext = EndpointLogContext("c", "ep")

    val service: AmendOtherEmploymentService = new AmendOtherEmploymentService(
      connector = mockAmendOtherEmploymentConnector
    )
  }

  "AmendOtherEmploymentService" when {
    "amendOtherEmployment" must {
      "return correct result for a success" in new Test {
        val outcome = Right(ResponseWrapper(correlationId, ()))

        MockAmendOtherEmploymentConnector.amendOtherEmployment(amendOtherEmploymentRequest)
          .returns(Future.successful(outcome))

        await(service.amendOtherEmployment(amendOtherEmploymentRequest)) shouldBe outcome
      }

      "map errors according to spec" when {

        def serviceError(desErrorCode: String, error: MtdError): Unit =
          s"a $desErrorCode error is returned from the service" in new Test {

            MockAmendOtherEmploymentConnector.amendOtherEmployment(amendOtherEmploymentRequest)
              .returns(Future.successful(Left(ResponseWrapper(correlationId, DesErrors.single(DesErrorCode(desErrorCode))))))

            await(service.amendOtherEmployment(amendOtherEmploymentRequest)) shouldBe Left(ErrorWrapper(correlationId, error))
          }

        val input = Seq(
          ("INVALID_TAXABLE_ENTITY_ID", NinoFormatError),
          ("INVALID_TAX_YEAR", TaxYearFormatError),
          ("INVALID_CORRELATIONID", DownstreamError),
          ("INVALID_PAYLOAD", DownstreamError),
          ("UNPROCESSABLE_ENTITY", DownstreamError),
          ("SERVER_ERROR", DownstreamError),
          ("SERVICE_UNAVAILABLE", DownstreamError)
        )

        input.foreach(args => (serviceError _).tupled(args))
      }
    }
  }
}