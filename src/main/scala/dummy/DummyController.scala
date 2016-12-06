package dummy

import com.twitter.finagle.http.Request
import com.twitter.finatra.http.Controller
import com.twitter.finatra.http.Controller
import com.twitter.finatra.request.{ QueryParam, RouteParam }
import com.twitter.finatra.response.Mustache
import com.twitter.finatra.http.request.RequestUtils


case class DummyContext(base: String)

@Mustache("index")
case class IndexView(
  ctx: DummyContext,
  version: String,
  buildate: String
)


class DummyController extends Controller {

  val ctx = DummyContext("/")
  import ctx.base

  def dumpInfo(rq: Request) {
    info("path=" + rq.path)
    info("uri=" + rq.uri)
    info("loc=" + rq.location)
    info("ref=" + rq.referer)
    info("agent=" + rq.userAgent)
    info("host=" + rq.host)
    info("pathUrl=" + RequestUtils.pathUrl(rq))
  }

  // -------------------------------------------------------------------------------------------------
  def home(request: Request) = {
    //dumpInfo(request)
    IndexView(
      ctx = ctx,
      version = MetaInfo.version,
      buildate = MetaInfo.buildate)
  }

  get(ctx.base) { request: Request => home(request) }
  

  // -------------------------------------------------------------------------------------------------  
  for { res <- List("js", "css", "images") } {
    get(s"$base$res/:*") { request: Request =>
      response.ok.file(s"/static/$res/" + request.params("*"))
    }
  }
  
}
