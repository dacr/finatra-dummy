package dummy

import com.twitter.finagle.http.Request
import com.twitter.finatra.http.Controller
import com.twitter.finatra.http.Controller
import com.twitter.finatra.request.{ QueryParam, RouteParam }
import com.twitter.finatra.response.Mustache
import com.twitter.finatra.http.request.RequestUtils

import java.net._

case class DummyContext(
    base: String
    )

@Mustache("index")
case class IndexView(
  ctx: DummyContext,
  info:RequestInfo,
  version: String,
  buildate: String
)


case class RequestInfo(
    path:String,
    uri:String,
    location:Option[String],
    referer:Option[String],
    userAgent:Option[String],
    host:Option[String],
    pathUrl:String,
    xForwardedFor:Option[String],
    remoteHost:String,
    remoteAddress:InetAddress,
    remotePort:Int,
    expires:Option[String]
    )
object RequestInfo{
  def apply(rq:Request):RequestInfo = {
    RequestInfo(
        path = rq.path,
        uri = rq.uri,
        location = rq.location,
        userAgent = rq.userAgent.map(_.take(20)+"..."),
        referer = rq.referer,
        host = rq.host,
        pathUrl = RequestUtils.pathUrl(rq),
        xForwardedFor = rq.xForwardedFor,
        remoteHost = rq.remoteHost,
        remoteAddress = rq.remoteAddress,
        remotePort = rq.remotePort,
        expires = rq.expires
        )
  }
}
    


class DummyController extends Controller {

  def base2use:String = {
    import scala.util.Properties._
    val key="DUMMY_BASE"
    val default=""
    propOrNone(key)
      .orElse(envOrNone(key))
      .getOrElse(default)
      .replaceAll("/+$", "")
      .replaceAll("^/+", "")
      .replaceAll("/{2,}", "/")
      .trim match {
      case "" => ""
      case s => s"/$s"
    }
  }
  
  val ctx = DummyContext(base2use)
  import ctx.base
  logger.info(s"Using context base $base")

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
    IndexView(
      ctx = ctx,
      info = RequestInfo(request),
      version = MetaInfo.version,
      buildate = MetaInfo.buildate)
  }

  get(s"$base/") { request: Request => home(request) }
  get(s"$base") { request: Request => home(request) }

  // -------------------------------------------------------------------------------------------------
  
  post(s"$base/file-upload") { request: Request =>
    info(request)
    request.multipart match {
      case Some(m) =>
        info(m.files)
        info(m.attributes)
      case None =>
    }
  }

  // -------------------------------------------------------------------------------------------------
  //case class Cell(time:Long, value:Double)
  case class Series(name:String, data:List[Tuple2[Long,Double]])
  
  get(s"$base/myseries") { request:Request =>
    val now = System.currentTimeMillis()

    val sampleData=1.to(1000).map(i=> now+i*1000 -> scala.math.random*10d).toList
    Series("random-data", sampleData )
  }
  
  // -------------------------------------------------------------------------------------------------  
  for { res <- List("js", "css", "images") } {
    get(s"$base/$res/:*") { request: Request =>
      response.ok.file(s"/static/$res/" + request.params("*"))
    }
  }
  
}
