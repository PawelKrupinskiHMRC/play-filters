package uk.gov.hmrc.play.filters.frontend

import org.joda.time.{DateTime, DateTimeZone}
import play.api.mvc.{Filter, RequestHeader, Result, Session}
import uk.gov.hmrc.play.filters.MicroserviceFilterSupport
import uk.gov.hmrc.play.http.SessionKeys

import scala.concurrent.Future

class SessionTimeoutFilter(clock: () => DateTime) extends Filter with MicroserviceFilterSupport {

  override def apply(f: (RequestHeader) => Future[Result])(rh: RequestHeader): Future[Result] = f(updateTimestamp(rh))

  val lastRequestTimestamp = "ts"

  private def updateTimestamp(requestHeader: RequestHeader): RequestHeader = {
    val ts = extractTimestamp(requestHeader.session)

    requestHeader.session


    val sessionData = session(result).getOrElse(request.session).data.toSeq
    val newSessionData = sessionData :+ (SessionKeys.lastRequestTimestamp -> now().getMillis.toString)
    result.withSession(newSessionData: _*)

  }

  private def extractTimestamp(session: Session): Option[DateTime] = {
    try {
      session.get(lastRequestTimestamp) map (timestamp => new DateTime(timestamp.toLong, DateTimeZone.UTC))
    } catch {
      case e: NumberFormatException => None
    }
  }

}
