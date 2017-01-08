package dummy

import com.twitter.finagle.http.{Request, Response}
import com.twitter.finatra.http.HttpServer
import com.twitter.finatra.http.routing.HttpRouter
import com.twitter.finatra.http.filters.{CommonFilters, LoggingMDCFilter, TraceIdMDCFilter}
import com.twitter.finatra.http.filters._

class DummyServer extends HttpServer {
  override def configureHttp(router: HttpRouter) {
    router
      .filter[LoggingMDCFilter[Request, Response]]
      .filter[TraceIdMDCFilter[Request, Response]]
      .filter[CommonFilters]
      .filter[HttpResponseFilter[Request]]
      .filter[ExceptionMappingFilter[Request]]
      .add[DummyController]
  }
}

object DummyServerMain extends DummyServer {
}
