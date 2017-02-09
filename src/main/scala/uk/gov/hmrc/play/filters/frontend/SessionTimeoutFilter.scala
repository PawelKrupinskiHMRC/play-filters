package uk.gov.hmrc.play.filters.frontend

import org.joda.time.{DateTime, DateTimeZone}
import play.api.mvc._
import uk.gov.hmrc.play.filters.MicroserviceFilterSupport
import uk.gov.hmrc.play.http.SessionKeys
import play.api.http.HeaderNames.COOKIE

import scala.concurrent.Future

class SessionTimeoutFilter(clock: () => Long) extends Filter with MicroserviceFilterSupport {

  override def apply(f: (RequestHeader) => Future[Result])(rh: RequestHeader): Future[Result] = f(updateTimestamp(rh))

  val lastRequestTimestamp = "ts"

  def currentTime() = clock().toString

  private def updateTimestamp(requestHeader: RequestHeader): RequestHeader = {
    val ts = extractTimestamp(requestHeader.session)

    val newCookie = updateSessionCookie(requestHeader)

    updateHeadersWithNewCookie(requestHeader.headers, newCookie) match {
      case Some(newHeaders) => requestHeader.copy(headers = newHeaders)
      case None => requestHeader
    }
  }

  private def updateHeadersWithNewCookie(headers: Headers, sessionCookie: Cookie) = {
    headers.get(COOKIE).map(cookieHeaderValue => {
      val newCookieHeader = Cookies.mergeCookieHeader(cookieHeaderValue, Seq(sessionCookie))
      headers.remove(COOKIE).add((COOKIE, newCookieHeader))
    })
  }

  private def updateSessionCookie(requestHeader: RequestHeader) = {
    val session = requestHeader.session
    val newSession = session + (SessionKeys.lastRequestTimestamp -> currentTime())
    Session.encodeAsCookie(newSession)
  }

  private def extractTimestamp(session: Session): Option[Long] = {
    try {
      session.get(lastRequestTimestamp) map (_.toLong)
    } catch {
      case e: NumberFormatException => None
    }
  }

}
