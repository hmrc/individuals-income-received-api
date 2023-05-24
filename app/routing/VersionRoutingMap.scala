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

import com.google.inject.ImplementedBy
import config.{AppConfig, FeatureSwitches}
import definition.Versions.{VERSION_1, VERSION_2}
import play.api.Logger
import play.api.routing.Router

import javax.inject.Inject

// So that we can have API-independent implementations of
// VersionRoutingRequestHandler and VersionRoutingRequestHandlerSpec
// implement this for the specific API...
@ImplementedBy(classOf[VersionRoutingMapImpl])
trait VersionRoutingMap {
  val defaultRouter: Router

  val map: Map[String, Router]

  final def versionRouter(version: String): Option[Router] = map.get(version)
}

// Add routes corresponding to available versions...

case class VersionRoutingMapImpl @Inject() (appConfig: AppConfig,
                                            defaultRouter: Router,
                                            v1Router: v1.Routes,
                                            v1r7cRouter: v1r7c.Routes,
                                            v2Router: v2.Routes,
                                            v2r7cRouter: v2r7c.Routes)
  extends VersionRoutingMap {

  val featureSwitches: FeatureSwitches = FeatureSwitches(appConfig.featureSwitches)
  protected val logger: Logger         = Logger(this.getClass)

  val map: Map[String, Router] = Map(
    VERSION_1 -> getRouter(VERSION_1, v1r7cRouter, v1Router),
    VERSION_2 -> getRouter(VERSION_2, v2r7cRouter, v2Router)
  )

  private def getRouter(version: String, r7cRoutes: Router, nonR7cRoutes: Router) = {
    if (featureSwitches.isV1R7cRoutingEnabled) {
      logger.info("[VersionRoutingMap][map] using ukDividendsRouter to include UK Dividends routes")
      r7cRoutes
    } else {
      logger.info(s"[VersionRoutingMap][map] using $version router without UK Dividends routes")
      nonR7cRoutes
    }
  }

}
