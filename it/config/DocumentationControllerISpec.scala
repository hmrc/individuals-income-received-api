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

package config

import io.swagger.v3.parser.OpenAPIV3Parser
import play.api.http.Status
import play.api.libs.json.{JsValue, Json}
import play.api.libs.ws.WSResponse
import support.IntegrationBaseSpec
import uk.gov.hmrc.auth.core.ConfidenceLevel

import scala.util.Try

class DocumentationControllerISpec extends IntegrationBaseSpec {

  val config: AppConfig                = app.injector.instanceOf[AppConfig]
  val confidenceLevel: ConfidenceLevel = config.confidenceLevelConfig.confidenceLevel

  val apiDefinitionJson: JsValue = Json.parse(
    s"""
    |{
    |   "scopes":[
    |      {
    |         "key":"read:self-assessment",
    |         "name":"View your Self Assessment information",
    |         "description":"Allow read access to self assessment data",
    |         "confidenceLevel": $confidenceLevel
    |      },
    |      {
    |         "key":"write:self-assessment",
    |         "name":"Change your Self Assessment information",
    |         "description":"Allow write access to self assessment data",
    |         "confidenceLevel": $confidenceLevel
    |      }
    |   ],
    |   "api":{
    |      "name":"Individuals Income Received (MTD)",
    |      "description":"An API for providing individual income received data",
    |      "context":"individuals/income-received",
    |      "categories":[
    |         "INCOME_TAX_MTD"
    |      ],
    |      "versions":[
    |      {
    |            "version":"1.0",
    |            "status":"ALPHA",
    |            "endpointsEnabled":false
    |         },
    |         {
    |            "version":"2.0",
    |            "status":"ALPHA",
    |            "endpointsEnabled":false
    |         }
    |      ]
    |   }
    |}
    """.stripMargin
  )

  "GET /api/definition" should {
    "return a 200 with the correct response body" in {
      val response: WSResponse = await(buildRequest("/api/definition").get())
      response.status shouldBe Status.OK
      Json.parse(response.body) shouldBe apiDefinitionJson
    }
  }

  "an OAS documentation request" must {
    def version(version: String): Unit = {
      s"return the documentation that passes OAS V$version parser" in {
        val response: WSResponse = await(buildRequest(s"/api/conf/${version}.0/application.yaml").get())
        response.status shouldBe Status.OK

        val contents     = response.body[String]
        val parserResult = Try(new OpenAPIV3Parser().readContents(contents))
        parserResult.isSuccess shouldBe true

        val openAPI = Option(parserResult.get.getOpenAPI)
        openAPI.isEmpty shouldBe false
        openAPI.get.getOpenapi shouldBe "3.0.3"
        openAPI.get.getInfo.getTitle shouldBe "Individuals Income Received (MTD)"
        openAPI.get.getInfo.getVersion shouldBe s"${version}.0"
      }
    }
    val versions: Seq[String] = Seq("1", "2")
    versions.foreach(v => version(v))
  }

}
