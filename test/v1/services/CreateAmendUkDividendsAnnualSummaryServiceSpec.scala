/*
 * Copyright 2022 HM Revenue & Customs
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

import api.controllers.EndpointLogContext
import api.models.domain.{Nino, TaxYear}
import api.models.errors._
import api.models.outcomes.ResponseWrapper
import api.services.ServiceSpec
import mocks.MockAppConfig
import play.api.Configuration
import v1.mocks.connectors.MockCreateAmendUkDividendsAnnualSummaryConnector
import v1.models.request.createAmendUkDividendsIncomeAnnualSummary.{
  CreateAmendUkDividendsIncomeAnnualSummaryBody,
  CreateAmendUkDividendsIncomeAnnualSummaryRequest
}

import scala.concurrent.Future

class CreateAmendUkDividendsAnnualSummaryServiceSpec extends ServiceSpec {

  private val request = CreateAmendUkDividendsIncomeAnnualSummaryRequest(
    nino = Nino("AA112233A"),
    taxYear = TaxYear.fromMtd("2023-24"),
    body = CreateAmendUkDividendsIncomeAnnualSummaryBody(None, None)
  )

  trait Test extends MockCreateAmendUkDividendsAnnualSummaryConnector with MockAppConfig {
    protected def featureSwitchConfig: Configuration

    implicit val logContext: EndpointLogContext = EndpointLogContext("c", "ep")

    MockedAppConfig.featureSwitches returns featureSwitchConfig

    val service: CreateAmendUkDividendsAnnualSummaryService =
      new CreateAmendUkDividendsAnnualSummaryService(mockAmendUkDividendsConnector, mockAppConfig)

  }

  trait DesTest extends Test {

    override protected def featureSwitchConfig: Configuration = Configuration.from(
      Map(
        "tys-api.enabled" -> false
      ))

  }

  trait TaxYearSpecificTest extends Test {

    override protected def featureSwitchConfig: Configuration = Configuration.from(
      Map(
        "tys-api.enabled" -> true
      ))

  }

  "CreateAmendAmendUkDividendsAnnualSummaryService calling a DES endpoint" when {
    "the downstream request is successful" must {
      "return a success result" in new DesTest {
        val outcome = Right(ResponseWrapper(correlationId, ()))

        MockCreateAmendUkDividendsAnnualSummaryConnector
          .createOrAmendAnnualSummary(request)
          .returns(Future.successful(outcome))

        await(service.createOrAmendAnnualSummary(request)) shouldBe outcome
      }

      "map errors according to spec" when {

        def serviceError(downstreamErrorCode: String, error: MtdError): Unit =
          s"downstream returns $downstreamErrorCode" in new DesTest {
            MockedAppConfig.featureSwitches returns Configuration.empty

            MockCreateAmendUkDividendsAnnualSummaryConnector
              .createOrAmendAnnualSummary(request)
              .returns(Future.successful(Left(ResponseWrapper(correlationId, DownstreamErrors.single(DownstreamErrorCode(downstreamErrorCode))))))

            val result: Either[ErrorWrapper, ResponseWrapper[Unit]] = await(service.createOrAmendAnnualSummary(request))
            result shouldBe Left(ErrorWrapper(correlationId, error))
          }

        val input = Seq(
          ("INVALID_NINO", NinoFormatError),
          ("INVALID_TAXYEAR", TaxYearFormatError),
          ("INVALID_TYPE", StandardDownstreamError),
          ("INVALID_PAYLOAD", StandardDownstreamError),
          ("NOT_FOUND_INCOME_SOURCE", NotFoundError),
          ("MISSING_CHARITIES_NAME_GIFT_AID", StandardDownstreamError),
          ("MISSING_GIFT_AID_AMOUNT", StandardDownstreamError),
          ("MISSING_CHARITIES_NAME_INVESTMENT", StandardDownstreamError),
          ("MISSING_INVESTMENT_AMOUNT", StandardDownstreamError),
          ("INVALID_ACCOUNTING_PERIOD", RuleTaxYearNotSupportedError),
          ("GONE", StandardDownstreamError),
          ("NOT_FOUND", NotFoundError),
          ("SERVER_ERROR", StandardDownstreamError),
          ("SERVICE_UNAVAILABLE", StandardDownstreamError)
        )

        input.foreach(args => (serviceError _).tupled(args))
      }
    }
  }

  "CreateAmendAmendUkDividendsAnnualSummaryService calling a Tax Year Specific IFS endpoint" when {

    "the downstream request is successful" must {
      "return a success result" in new TaxYearSpecificTest {
        val outcome = Right(ResponseWrapper(correlationId, ()))

        MockCreateAmendUkDividendsAnnualSummaryConnector
          .createOrAmendAnnualSummary(request)
          .returns(Future.successful(outcome))

        val result: Either[ErrorWrapper, ResponseWrapper[Unit]] = await(service.createOrAmendAnnualSummary(request))
        result shouldBe outcome
      }
    }

    "map errors according to spec" when {

      def serviceError(downstreamErrorCode: String, error: MtdError): Unit =
        s"downstream returns $downstreamErrorCode" in new TaxYearSpecificTest {
          MockedAppConfig.featureSwitches returns Configuration.empty

          MockCreateAmendUkDividendsAnnualSummaryConnector
            .createOrAmendAnnualSummary(request)
            .returns(Future.successful(Left(ResponseWrapper(correlationId, DownstreamErrors.single(DownstreamErrorCode(downstreamErrorCode))))))

          val result: Either[ErrorWrapper, ResponseWrapper[Unit]] = await(service.createOrAmendAnnualSummary(request))
          result shouldBe Left(ErrorWrapper(correlationId, error))
        }

      val input = Seq(
        ("INVALID_NINO", NinoFormatError),
        ("INVALID_TAXYEAR", TaxYearFormatError),
        ("INVALID_INCOMESOURCE_TYPE", StandardDownstreamError),
        ("INVALID_CORRELATIONID", BadRequestError),
        ("INVALID_PAYLOAD", BadRequestError),
        ("INCOME_SOURCE_NOT_FOUND", NotFoundError),
        ("MISSING_CHARITIES_NAME_GIFT_AID", StandardDownstreamError),
        ("MISSING_GIFT_AID_AMOUNT", StandardDownstreamError),
        ("MISSING_CHARITIES_NAME_INVESTMENT", StandardDownstreamError),
        ("MISSING_INVESTMENT_AMOUNT", StandardDownstreamError),
        ("INVALID_ACCOUNTING_PERIOD", RuleTaxYearNotSupportedError),
        ("INCOMPATIBLE_INCOME_SOURCE", StandardDownstreamError),
        ("TAX_YEAR_NOT_SUPPORTED", RuleTaxYearNotSupportedError),
        ("SERVER_ERROR", StandardDownstreamError),
        ("SERVICE_UNAVAILABLE", StandardDownstreamError)
      )

      input.foreach(args => (serviceError _).tupled(args))
    }
  }

}
