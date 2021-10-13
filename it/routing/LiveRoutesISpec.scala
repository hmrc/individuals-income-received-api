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

package routing

import com.github.tomakehurst.wiremock.stubbing.StubMapping
import play.api.http.HeaderNames.ACCEPT
import play.api.http.Status._
import play.api.libs.json.{JsValue, Json}
import play.api.libs.ws.{WSRequest, WSResponse}
import support.V1IntegrationSpec
import v1.stubs.{AuditStub, AuthStub, DownstreamStub, MtdIdLookupStub}

class LiveRoutesISpec extends V1IntegrationSpec {

  override def servicesConfig: Map[String, Any] = Map(
    "microservice.services.des.host" -> mockHost,
    "microservice.services.des.port" -> mockPort,
    "microservice.services.ifs.host" -> mockHost,
    "microservice.services.ifs.port" -> mockPort,
    "microservice.services.mtd-id-lookup.host" -> mockHost,
    "microservice.services.mtd-id-lookup.port" -> mockPort,
    "microservice.services.auth.host" -> mockHost,
    "microservice.services.auth.port" -> mockPort,
    "auditing.consumer.baseUri.port" -> mockPort,
    "minimumPermittedTaxYear" -> 2020,
    "feature-switch.foreign-endpoints.enabled" -> false,
    "feature-switch.release-6.enabled" -> false,
    "feature-switch.release-7.enabled" -> false
  )

  private trait Test {
    val nino: String = "AA123456A"
    val taxYear: String = "2019-20"

    val requestBodyJson: JsValue = Json.parse(
      """
        |{
        |  "securities":{
        |        "taxTakenOff": 100.11,
        |        "grossAmount": 100.22,
        |        "netAmount": 100.33
        |  },
        |  "foreignInterest":[
        |     {
        |        "amountBeforeTax": 101.11,
        |        "countryCode": "FRA",
        |        "taxTakenOff": 102.22,
        |        "specialWithholdingTax": 103.33,
        |        "taxableAmount": 104.44,
        |        "foreignTaxCreditRelief": true
        |      },
        |      {
        |        "amountBeforeTax": 201.11,
        |        "countryCode": "DEU",
        |        "taxTakenOff": 202.22,
        |        "specialWithholdingTax": 203.33,
        |        "taxableAmount": 204.44,
        |        "foreignTaxCreditRelief": true
        |      }
        |   ]
        |}
      """.stripMargin
    )

    def uri: String = s"/savings/$nino/$taxYear"

    def setupStubs(): StubMapping

    def request(uri: String): WSRequest = {
      setupStubs()
      buildRequest(uri)
        .withHttpHeaders((ACCEPT, "application/vnd.hmrc.1.0+json"))
    }
  }

  "Calling the 'amend savings' endpoint (switched on in production)" should {
    "return a 200 status code" when {
      "the feature switch is turned off to point to live routes only" in new Test {

        def ifsUri: String = s"/income-tax/income/savings/$nino/$taxYear"

        override def setupStubs(): StubMapping = {
          AuditStub.audit()
          AuthStub.authorised()
          MtdIdLookupStub.ninoFound(nino)
          DownstreamStub.onSuccess(DownstreamStub.PUT, ifsUri, NO_CONTENT)
        }

        val response: WSResponse = await(request(uri).put(requestBodyJson))
        response.status shouldBe OK
      }
    }
  }

  "Calling the 'amend foreign' endpoint (switched off in production)" should {
    "return a 404 status code" when {
      "the feature switch is turned off to point to live routes only" in new Test {

        override val requestBodyJson: JsValue = Json.parse(
          """
            |{
            |   "foreignEarnings": {
            |      "customerReference": "FOREIGNINCME123A",
            |      "earningsNotTaxableUK": 1999.99
            |   },
            |   "unremittableForeignIncome": [
            |       {
            |          "countryCode": "FRA",
            |          "amountInForeignCurrency": 1999.99,
            |          "amountTaxPaid": 1999.99
            |       },
            |       {
            |          "countryCode": "IND",
            |          "amountInForeignCurrency": 2999.99,
            |          "amountTaxPaid": 2999.99
            |       }
            |    ]
            |}
           """.stripMargin
        )

        override def uri: String = s"/foreign/$nino/$taxYear"

        override def setupStubs(): StubMapping = {
          AuditStub.audit()
          AuthStub.authorised()
          MtdIdLookupStub.ninoFound(nino)
        }

        val response: WSResponse = await(request(uri).put(requestBodyJson))
        response.status shouldBe NOT_FOUND
      }
    }
  }

  "Calling the 'delete cgt ppd overrides' endpoint (switched off in production)" should {
    "return a 404 status code" when {
      "the feature switch is turned off to point to the live routes only" in new Test {

        override def uri: String = s"/disposals/residential-property/$nino/$taxYear/ppd"

        override def setupStubs(): StubMapping = {
          AuthStub.authorised()
          MtdIdLookupStub.ninoFound(nino)
        }

        val response: WSResponse = await(request(uri).delete)
        response.status shouldBe NOT_FOUND
      }
    }
  }
}