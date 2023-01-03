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

package routing

import com.typesafe.config.ConfigFactory
import definition.Versions
import mocks.MockAppConfig
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.Configuration
import play.api.routing.Router
import support.UnitSpec

class VersionRoutingMapSpec extends UnitSpec with MockAppConfig with GuiceOneAppPerSuite {

  val defaultRouter: Router     = mock[Router]
  val v1Routes: v1.Routes       = app.injector.instanceOf[v1.Routes]
  val v1r7cRoutes: v1r7c.Routes = app.injector.instanceOf[v1r7c.Routes]

  private def newVersionRoutingMap(v1r7cEnabled: Boolean) = {
    MockedAppConfig.featureSwitches.returns(Configuration(ConfigFactory.parseString(s"v1r7c-endpoints.enabled = $v1r7cEnabled")))

    VersionRoutingMapImpl(
      appConfig = mockAppConfig,
      defaultRouter = defaultRouter,
      v1Router = v1Routes,
      v1r7cRouter = v1r7cRoutes
    )
  }

  "map" when {
    "routing to v1" should {
      "route to v1.routes" in {
        val versionRoutingMap = newVersionRoutingMap(v1r7cEnabled = false)
        versionRoutingMap.map(Versions.VERSION_1) shouldBe v1Routes
      }
    }

    "routing to v1WithUkDividends" should {
      "route to v1r7c.routes" in {
        val versionRoutingMap = newVersionRoutingMap(v1r7cEnabled = true)
        versionRoutingMap.map(Versions.VERSION_1) shouldBe v1r7cRoutes
      }
    }
  }

}
