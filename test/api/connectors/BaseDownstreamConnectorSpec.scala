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

package api.connectors

import api.connectors.DownstreamUri.{DesUri, IfsUri}
import api.mocks.MockHttpClient
import api.models.outcomes.ResponseWrapper
import config.AppConfig
import mocks.MockAppConfig
import uk.gov.hmrc.http.{HttpClient, HttpReads}

import scala.concurrent.Future

class BaseDownstreamConnectorSpec extends ConnectorSpec {
  // WLOG
  val body        = "body"
  val outcome     = Right(ResponseWrapper(correlationId, Result(2)))
  val url         = "some/url?param=value"
  val absoluteUrl = s"$baseUrl/$url"

  // WLOG
  case class Result(value: Int)

  implicit val httpReads: HttpReads[DownstreamOutcome[Result]] = mock[HttpReads[DownstreamOutcome[Result]]]

  "for DES" when {
    "post" must {
      "posts with the required des headers and returns the result" in new Test with DesTest {
        MockedHttpClient
          .post(
            absoluteUrl,
            config = dummyDesHeaderCarrierConfig,
            body,
            requiredHeaders = requiredHeaders,
            excludedHeaders = Seq("AnotherHeader" -> "HeaderValue"))
          .returns(Future.successful(outcome))

        await(connector.post(body, DesUri[Result](url))) shouldBe outcome
      }
    }

    "get" must {
      "get with the required des headers and return the result" in new Test with DesTest {
        MockedHttpClient
          .get(
            absoluteUrl,
            config = dummyDesHeaderCarrierConfig,
            parameters = qps,
            requiredHeaders = requiredDesHeaders,
            excludedHeaders = Seq("AnotherHeader" -> "HeaderValue"))
          .returns(Future.successful(outcome))

        await(connector.get(DesUri[Result](url), queryParams = qps)) shouldBe outcome
      }
    }

    "delete" must {
      "delete with the required des headers and return the result" in new Test with DesTest {
        MockedHttpClient
          .delete(
            absoluteUrl,
            config = dummyDesHeaderCarrierConfig,
            requiredHeaders = requiredDesHeaders,
            excludedHeaders = Seq("AnotherHeader" -> "HeaderValue"))
          .returns(Future.successful(outcome))

        await(connector.delete(DesUri[Result](url))) shouldBe outcome
      }
    }

    "put" must {
      "send the request with the required downstream headers and return result" in new Test with DesTest {

        MockedHttpClient
          .put(
            absoluteUrl,
            config = dummyDesHeaderCarrierConfig,
            body,
            requiredHeaders = requiredHeaders,
            excludedHeaders = Seq("AnotherHeader" -> "HeaderValue"))
          .returns(Future.successful(outcome))

        private val result: DownstreamOutcome[Result] = await(connector.put(DesUri[Result](url), body))
        result shouldBe outcome
      }
    }

    "content-type header already present and set to be passed through" must {
      "override (not duplicate) the value" when {
        testNoDuplicatedContentType("Content-Type" -> "application/user-type")
        testNoDuplicatedContentType("content-type" -> "application/user-type")

        def testNoDuplicatedContentType(userContentType: (String, String)): Unit =
          s"for user content type header $userContentType" in new Test with DesTest {
            MockedHttpClient
              .put(
                absoluteUrl,
                config = dummyDesHeaderCarrierConfig,
                body,
                requiredHeaders = requiredDesHeaders ++ Seq("Content-Type" -> "application/json"),
                excludedHeaders = Seq(userContentType)
              )
              .returns(Future.successful(outcome))

            await(connector.put(DesUri[Result](url), body)) shouldBe outcome
          }
      }
    }
  }

  "for IFS" when {
    "post" must {
      "posts with the required ifs headers and returns the result" in new Test with IfsTest {
        MockedHttpClient
          .post(
            absoluteUrl,
            config = dummyIfsHeaderCarrierConfig,
            body,
            requiredHeaders = requiredHeaders,
            excludedHeaders = Seq("AnotherHeader" -> "HeaderValue"))
          .returns(Future.successful(outcome))

        await(connector.post(body, IfsUri[Result](url))) shouldBe outcome
      }
    }

    "get" must {
      "get with the required des headers and return the result" in new Test with IfsTest {
        MockedHttpClient
          .get(
            absoluteUrl,
            config = dummyIfsHeaderCarrierConfig,
            parameters = qps,
            requiredHeaders = requiredIfsHeaders,
            excludedHeaders = Seq("AnotherHeader" -> "HeaderValue"))
          .returns(Future.successful(outcome))

        await(connector.get(IfsUri[Result](url), queryParams = qps)) shouldBe outcome
      }
    }

    "delete" must {
      "delete with the required des headers and return the result" in new Test with IfsTest {
        MockedHttpClient
          .delete(
            absoluteUrl,
            config = dummyIfsHeaderCarrierConfig,
            requiredHeaders = requiredIfsHeaders,
            excludedHeaders = Seq("AnotherHeader" -> "HeaderValue"))
          .returns(Future.successful(outcome))

        await(connector.delete(IfsUri[Result](url))) shouldBe outcome
      }
    }

    "put" must {
      "send the request with the required des headers and return result" in new Test with IfsTest {
        MockedHttpClient
          .put(
            absoluteUrl,
            config = dummyIfsHeaderCarrierConfig,
            body,
            requiredHeaders = requiredHeaders,
            excludedHeaders = Seq("AnotherHeader" -> "HeaderValue"))
          .returns(Future.successful(outcome))

        private val result = await(connector.put(IfsUri[Result](url), body))
        result shouldBe outcome
      }

      "send the request with no body" in new Test with IfsTest {

        MockedHttpClient
          .put(
            absoluteUrl,
            config = dummyIfsHeaderCarrierConfig,
            body = "",
            requiredHeaders = requiredHeaders,
            excludedHeaders = Seq("AnotherHeader" -> "HeaderValue"))
          .returns(Future.successful(outcome))

        private val result = await(connector.put(IfsUri[Result](url)))
        result shouldBe outcome
      }
    }

    "content-type header already present and set to be passed through" must {
      "override (not duplicate) the value" when {
        testNoDuplicatedContentType("Content-Type" -> "application/user-type")
        testNoDuplicatedContentType("content-type" -> "application/user-type")

        def testNoDuplicatedContentType(userContentType: (String, String)): Unit =
          s"for user content type header $userContentType" in new Test with IfsTest {
            MockedHttpClient
              .put(
                absoluteUrl,
                config = dummyIfsHeaderCarrierConfig,
                body,
                requiredHeaders = requiredIfsHeaders ++ Seq("Content-Type" -> "application/json"),
                excludedHeaders = Seq(userContentType)
              )
              .returns(Future.successful(outcome))

            await(connector.put(IfsUri[Result](url), body)) shouldBe outcome
          }
      }
    }
  }

  class Test extends MockHttpClient with MockAppConfig {

    val connector: BaseDownstreamConnector = new BaseDownstreamConnector {
      val http: HttpClient     = mockHttpClient
      val appConfig: AppConfig = mockAppConfig
    }

    val qps = Seq("param1" -> "value1")
  }

}
