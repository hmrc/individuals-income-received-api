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

import com.typesafe.config.ConfigFactory
import definition.Versions
import mocks.MockAppConfig
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.Configuration
import play.api.routing.Router
import support.UnitSpec

class VersionRoutingMapSpec extends UnitSpec with MockAppConfig with GuiceOneAppPerSuite {

  val defaultRouter: Router = mock[Router]
  val v1Routes: v1.Routes = app.injector.instanceOf[v1.Routes]
  val v1WithAllRoutes: v1WithAll.Routes = app.injector.instanceOf[v1WithAll.Routes]
  val v1WithCgtRoutes: v1WithCgt.Routes = app.injector.instanceOf[v1WithCgt.Routes]
  val v1WithForeignRoutes: v1withForeign.Routes = app.injector.instanceOf[v1withForeign.Routes]

  "map" when {
    "routing to v1" when {
      def test(isForeignEnabled: Boolean, isCgtEnabled: Boolean, routes: Any): Unit = {

        s"foreign feature switch is set to - $isForeignEnabled, and, cgt feature switch is set to - $isCgtEnabled" should {
          s"route to ${routes.toString}" in {

            MockedAppConfig.featureSwitch.returns(Some(Configuration(ConfigFactory.parseString(s"""
              |foreign-endpoints.enabled = $isForeignEnabled,
              |cgt-endpoints.enabled = $isCgtEnabled
              |""".stripMargin))))

            val versionRoutingMap: VersionRoutingMapImpl = VersionRoutingMapImpl(
              appConfig = mockAppConfig,
              defaultRouter = defaultRouter,
              v1Router = v1Routes,
              v1RouterWithForeign = v1WithForeignRoutes,
              v1RouterWithCgt = v1WithCgtRoutes,
              v1RouterWithAll = v1WithAllRoutes
            )

            versionRoutingMap.map(Versions.VERSION_1) shouldBe routes
          }
        }
      }

      Seq(
        (true, true,  v1WithAllRoutes),
        (true, false, v1WithForeignRoutes),
        (false, true, v1WithCgtRoutes),
        (false, false, v1Routes),
      ).foreach(args => (test _).tupled(args))
    }
  }
}
