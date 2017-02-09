/*
 * Copyright 2017 HM Revenue & Customs
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

package uk.gov.hmrc.play.filters.frontend

import org.joda.time.{DateTime, DateTimeZone}
import org.scalatest.mock.MockitoSugar
import org.scalatest.{Matchers, WordSpecLike}
import play.api.mvc.{AnyContentAsEmpty, Results}
import play.api.test.{FakeHeaders, FakeRequest, WithApplication}

import scala.concurrent.Future

class SessionTimeoutFilterSpec extends WordSpecLike with Matchers with MockitoSugar {

  "SessionTimeoutFilter" should {

    "update the timestamp if the session is not expired" in new WithApplication {
      val now = new DateTime(2017, 1, 12, 14, 56)

      val clock: () => Long = () => now.getMillis

      val filter = new SessionTimeoutFilter(clock)

      val timestamp = now.minusMinutes(1).getMillis.toString

      val rh = FakeRequest("POST", "/something", FakeHeaders(), AnyContentAsEmpty).withSession("ts" -> timestamp)

      filter.apply(requestHeader => {
        requestHeader.session("ts") shouldBe now.getMillis.toString
        Future.successful(Results.Ok)
      })(rh)
    }
  }

}
