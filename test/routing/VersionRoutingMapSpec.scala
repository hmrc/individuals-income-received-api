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
  val v1WithForeignRoutes: v1WithForeign.Routes = app.injector.instanceOf[v1WithForeign.Routes]
  val v1WithRelease6Routes: v1WithRelease6.Routes = app.injector.instanceOf[v1WithRelease6.Routes]
  val v1WithRelease6AndForeignRoutes: v1WithRelease6AndForeign.Routes = app.injector.instanceOf[v1WithRelease6AndForeign.Routes]
  val v1WithRelease7Routes: v1WithRelease7.Routes = app.injector.instanceOf[v1WithRelease7.Routes]
  val v1WithRelease7AndForeignRoutes: v1WithRelease7AndForeign.Routes = app.injector.instanceOf[v1WithRelease7AndForeign.Routes]

  "map" when {
    "routing to v1" when {
      def test(isForeignEnabled: Boolean, isRelease6Enabled: Boolean, isRelease7Enabled: Boolean, routes: Any): Unit = {

        s"foreign feature switch is set to - $isForeignEnabled, " +
          s"release 6 feature switch is set to - $isRelease6Enabled, " +
          s"and release 7 feature switch is set to - $isRelease7Enabled" should {
          s"route to ${routes.toString}" in {

            MockedAppConfig.featureSwitch.returns(Some(Configuration(ConfigFactory.parseString(s"""
              |foreign-endpoints.enabled = $isForeignEnabled,
              |release-6.enabled = $isRelease6Enabled,
              |release-7.enabled = $isRelease7Enabled
              |""".stripMargin))))

            val versionRoutingMap: VersionRoutingMapImpl = VersionRoutingMapImpl(
              appConfig = mockAppConfig,
              defaultRouter = defaultRouter,
              v1Router = v1Routes,
              v1RouterWithForeign = v1WithForeignRoutes,
              v1RouterWithRelease6 = v1WithRelease6Routes,
              v1RouterWithRelease6AndForeign = v1WithRelease6AndForeignRoutes,
              v1RouterWithRelease7 = v1WithRelease7Routes,
              v1RouterWithRelease7AndForeign = v1WithRelease7AndForeignRoutes
            )

            versionRoutingMap.map(Versions.VERSION_1) shouldBe routes
          }
        }
      }

      Seq(
        (true, true, true, v1WithRelease7AndForeignRoutes),
        (true, false, true, v1WithRelease7AndForeignRoutes),
        (false, false, true, v1WithRelease7Routes),
        (false, true, true, v1WithRelease7Routes),
        (true, true, false, v1WithRelease6AndForeignRoutes),
        (false, true, false, v1WithRelease6Routes),
        (true, false, false, v1WithForeignRoutes),
        (false, false, false, v1Routes)
      ).foreach(args => (test _).tupled(args))
    }
  }
}
