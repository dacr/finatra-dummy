package dummy

import com.twitter.finagle.http.Request
import com.twitter.finatra.http.Controller
import com.twitter.finatra.http.Controller
import com.twitter.finatra.request.{ QueryParam, RouteParam }
import com.twitter.finatra.response.Mustache
import com.twitter.finatra.http.request.RequestUtils

import com.twitter.bijection.Conversion._
import com.twitter.bijection.twitter_util.UtilBijections.twitter2ScalaFuture
import com.twitter.util.{Future => TwitterFuture}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{Future => ScalaFuture}

import java.net._
import better.files._
import better.files.Cmds._
import java.io.{File => JavaFile}




case class DummyContext(
    base: String,
    workspace:File
    )

@Mustache("index")
case class IndexView(
  ctx: DummyContext,
  info:RequestInfo,
  version: String,
  buildate: String
)


@Mustache("elkpart")
case class ElkPartView(
  ctx: DummyContext,
  message: String
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
  import scala.concurrent.ExecutionContext.Implicits.global
    
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
  
  val ctx = {
    val workspace = file"/tmp/dummy"
    mkdirs(workspace)
    DummyContext(base2use, workspace)
  }
  import ctx.base
  logger.info(s"Using context base $base")
  val pers = new Persistency

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
        info(m)
        for {
          files <- m.files.get("chosenfiles")
          file <- files
        } {
          info("Processing uploaded file named : "+file.fileName)
          file match {
            case ondsk: com.twitter.finagle.http.exp.Multipart.OnDiskFileUpload =>
              cp(ondsk.content.toScala, ctx.workspace / file.fileName)
            case inmem: com.twitter.finagle.http.exp.Multipart.InMemoryFileUpload =>
              val bytes = com.twitter.io.Buf.ByteArray.Owned.extract(inmem.content)
              (ctx.workspace / file.fileName).writeBytes( bytes.toIterator)
          }
        }
      case None =>
    }
  }

  // -------------------------------------------------------------------------------------------------
  //case class Cell(time:Long, value:Double)
  case class Series(name:String, data:List[Tuple2[Long,Double]])
  
  get(s"$base/api/myseries") { request:Request =>
    val now = System.currentTimeMillis()

    val sampleData=1.to(1000).map(i=> now+i*1000 -> scala.math.random*10d).toList
    Series("random-data", sampleData )
  }
  
  // -------------------------------------------------------------------------------------------------

  // returns a html fragment to be insert into the DOM as soon as everything is ready
  get(s"$base/sub/elkpart") {request:Request => 
    pers.getMessage map { msg:String =>
      ElkPartView(ctx, msg)
    }
  }

  // 
  get(s"$base/api/message") {request:Request => 
    pers.getMessage
  }
  
  post(s"$base/api/message") {request:Request =>
    val newmsg = request.getParam("message", "internal error").replaceAll("[^a-z0-9 _]", "")
    pers.setMessage(newmsg)
    newmsg
  }

  get(s"$base/api/series/:id") { request:Request =>
    val seriesName = request.params("id")
    pers.getSeries(seriesName).map { result =>
      result
    }
  }
  
  // -------------------------------------------------------------------------------------------------  
  for { res <- List("js", "css", "images") } {
    get(s"$base/$res/:*") { request: Request =>
      response.ok.file(s"/static/$res/" + request.params("*"))
    }
  }
  
}
