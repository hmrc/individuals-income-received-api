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

package v1r6.connectors

import config.AppConfig
import mocks.MockAppConfig
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient, HttpReads}
import v1r6.connectors.DownstreamUri.{DesUri, IfsUri}
import v1r6.mocks.MockHttpClient
import v1r6.models.outcomes.ResponseWrapper

import scala.concurrent.Future

class BaseDownstreamConnectorSpec extends ConnectorSpec {
  // WLOG
  val body = "body"
  val outcome = Right(ResponseWrapper(correlationId, Result(2)))
  val url = "some/url?param=value"
  val absoluteUrl = s"$baseUrl/$url"

  // WLOG
  case class Result(value: Int)

  implicit val httpReads: HttpReads[DesOutcome[Result]] = mock[HttpReads[DesOutcome[Result]]]

  class DesTest extends MockHttpClient with MockAppConfig {
    val connector: BaseDownstreamConnector = new BaseDownstreamConnector {
      val http: HttpClient = mockHttpClient
      val appConfig: AppConfig = mockAppConfig
    }
    MockedAppConfig.desBaseUrl returns baseUrl
    MockedAppConfig.desToken returns "des-token"
    MockedAppConfig.desEnvironment returns "des-environment"
    MockedAppConfig.desEnvironmentHeaders returns Some(allowedDesHeaders)
  }

  class IfsTest extends MockHttpClient with MockAppConfig {
    val connector: BaseDownstreamConnector = new BaseDownstreamConnector {
      val http: HttpClient = mockHttpClient
      val appConfig: AppConfig = mockAppConfig
    }
    MockedAppConfig.ifsBaseUrl returns baseUrl
    MockedAppConfig.ifsToken returns "ifs-token"
    MockedAppConfig.ifsEnvironment returns "ifs-environment"
    MockedAppConfig.ifsEnvironmentHeaders returns Some(allowedIfsHeaders)
  }

  "for DES" when {
    "post" must {
      "posts with the required des headers and returns the result" in new DesTest {
        implicit val hc: HeaderCarrier = HeaderCarrier(otherHeaders = otherHeaders ++ Seq("Content-Type" -> "application/json"))
        val requiredDesHeadersPost: Seq[(String, String)] = requiredDesHeaders ++ Seq("Content-Type" -> "application/json")

        MockedHttpClient
          .post(absoluteUrl,
            config = dummyDesHeaderCarrierConfig,
            body,
            requiredDesHeadersPost,
            excludedHeaders = Seq("AnotherHeader" -> "HeaderValue")
          )
          .returns(Future.successful(outcome))

        await(connector.post(body, DesUri[Result](url))) shouldBe outcome
      }
    }

    "get" must {
      "get with the required des headers and return the result" in new DesTest {
        implicit val hc: HeaderCarrier = HeaderCarrier(otherHeaders = otherHeaders ++ Seq("Content-Type" -> "application/json"))

        MockedHttpClient
          .get(absoluteUrl,
            config = dummyDesHeaderCarrierConfig,
            requiredDesHeaders,
            excludedHeaders = Seq("AnotherHeader" -> "HeaderValue")
          )
          .returns(Future.successful(outcome))

        await(connector.get(DesUri[Result](url))) shouldBe outcome
      }
    }

    "delete" must {
      "delete with the required des headers and return the result" in new DesTest {
        implicit val hc: HeaderCarrier = HeaderCarrier(otherHeaders = otherHeaders ++ Seq("Content-Type" -> "application/json"))

        MockedHttpClient
          .delete(absoluteUrl,
            config =  dummyDesHeaderCarrierConfig,
            requiredDesHeaders,
            excludedHeaders = Seq("AnotherHeader" -> "HeaderValue")
          )
          .returns(Future.successful(outcome))

        await(connector.delete(DesUri[Result](url))) shouldBe outcome
      }
    }

    "put" must {
      "put with the required des headers and return result" in new DesTest {
        implicit val hc: HeaderCarrier = HeaderCarrier(otherHeaders = otherHeaders ++ Seq("Content-Type" -> "application/json"))
        val requiredDesHeadersPut: Seq[(String, String)] = requiredDesHeaders ++ Seq("Content-Type" -> "application/json")

        MockedHttpClient.put(absoluteUrl,
          config = dummyDesHeaderCarrierConfig,
          body,
          requiredDesHeadersPut,
          excludedHeaders = Seq("AnotherHeader" -> "HeaderValue")
        )
          .returns(Future.successful(outcome))

        await(connector.put(body, DesUri[Result](url))) shouldBe outcome
      }
    }
  }

  "for IFS" when {
    "post" must {
      "posts with the required ifs headers and returns the result" in new IfsTest {
        implicit val hc: HeaderCarrier = HeaderCarrier(otherHeaders = otherHeaders ++ Seq("Content-Type" -> "application/json"))
        val requiredIfsHeadersPost: Seq[(String, String)] = requiredIfsHeaders ++ Seq("Content-Type" -> "application/json")

        MockedHttpClient
          .post(absoluteUrl,
            config = dummyIfsHeaderCarrierConfig,
            body,
            requiredIfsHeadersPost,
            excludedHeaders = Seq("AnotherHeader" -> "HeaderValue")
          )
          .returns(Future.successful(outcome))

        await(connector.post(body, IfsUri[Result](url))) shouldBe outcome
      }
    }

    "get" must {
      "get with the required des headers and return the result" in new IfsTest {
        implicit val hc: HeaderCarrier = HeaderCarrier(otherHeaders = otherHeaders ++ Seq("Content-Type" -> "application/json"))

        MockedHttpClient
          .get(absoluteUrl,
            config = dummyIfsHeaderCarrierConfig,
            requiredIfsHeaders,
            excludedHeaders = Seq("AnotherHeader" -> "HeaderValue")
          )
          .returns(Future.successful(outcome))

        await(connector.get(IfsUri[Result](url))) shouldBe outcome
      }
    }

    "delete" must {
      "delete with the required des headers and return the result" in new IfsTest {
        implicit val hc: HeaderCarrier = HeaderCarrier(otherHeaders = otherHeaders ++ Seq("Content-Type" -> "application/json"))

        MockedHttpClient
          .delete(absoluteUrl,
            config = dummyIfsHeaderCarrierConfig,
            requiredIfsHeaders,
            excludedHeaders = Seq("AnotherHeader" -> "HeaderValue")
          )
          .returns(Future.successful(outcome))

        await(connector.delete(IfsUri[Result](url))) shouldBe outcome
      }
    }

    "put" must {
      "put with the required des headers and return result" in new IfsTest {
        implicit val hc: HeaderCarrier = HeaderCarrier(otherHeaders = otherHeaders ++ Seq("Content-Type" -> "application/json"))
        val requiredIfsHeadersPut: Seq[(String, String)] = requiredIfsHeaders ++ Seq("Content-Type" -> "application/json")

        MockedHttpClient.put(absoluteUrl,
          config =  dummyIfsHeaderCarrierConfig,
          body,
          requiredIfsHeadersPut,
          excludedHeaders = Seq("AnotherHeader" -> "HeaderValue"))
          .returns(Future.successful(outcome))

        await(connector.put(body, IfsUri[Result](url))) shouldBe outcome
      }
    }
  }
}