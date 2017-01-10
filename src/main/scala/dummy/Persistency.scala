package dummy

import com.sksamuel.elastic4s.ElasticClient
import com.sksamuel.elastic4s.ElasticDsl._
import com.sksamuel.elastic4s.mappings.FieldType._
import com.sksamuel.elastic4s.ElasticsearchClientUri
import com.sksamuel.elastic4s.analyzers._
import scala.util.{Success,Failure,Try}

import com.twitter.bijection.Conversion._
import com.twitter.bijection.twitter_util.UtilBijections.twitter2ScalaFuture
import com.twitter.util.{Future => TwitterFuture}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{Future => ScalaFuture}
import scala.concurrent.duration._


import org.elasticsearch.action.admin.indices.delete.DeleteIndexResponse
import org.elasticsearch.action.admin.indices.create.CreateIndexResponse
//import org.elasticsearch.action.update.UpdateResponse
import com.sksamuel.elastic4s.indexes.RichIndexResponse
import com.sksamuel.elastic4s.bulk.RichBulkResponse
import com.sksamuel.elastic4s.update.RichUpdateResponse



class Persistency {
  val logger=org.slf4j.LoggerFactory.getLogger(getClass)
  import scala.concurrent.ExecutionContext.Implicits.global
  
  val now=System.currentTimeMillis()
  val sdf=new java.text.SimpleDateFormat("YYYY-MM-dd-H-m-s")
  
  val paramIndexName="dummy-global"
  val seriesIndexName="dummy-series-"+sdf.format(now)
  
  def _deleteIndex(implicit client:ElasticClient):ScalaFuture[DeleteIndexResponse] = {
    logger.info(s"deleting index $paramIndexName (we're in debug mode)")
    client.execute{deleteIndex(paramIndexName)}
  }
  
  def _createIndex(implicit client:ElasticClient):ScalaFuture[List[CreateIndexResponse]] = {
    logger.info(s"creating index $paramIndexName")
    val paramIndex = client.execute{
      createIndex(paramIndexName) mappings(
        mapping("params") as Seq(
            textField("message")
            )
      )
    }
    logger.info(s"creating index $seriesIndexName")
    val seriesIndex = client.execute{
      createIndex(seriesIndexName) mappings(
        mapping("cells") as Seq(
            keywordField("name"),
            dateField("time"),
            doubleField("value")
        )
      )
    }
    ScalaFuture.sequence(paramIndex::seriesIndex::Nil)
  }
  
//  def _populate(implicit client:ElasticClient):ScalaFuture[List[RichBulkResponse]] = {
//    val params = _populateParams
//    val series = _populateFakeSeries
//    ScalaFuture.sequence(params::series)
//  }
  
  def _populateParams(implicit client:ElasticClient):ScalaFuture[RichBulkResponse] = {
    logger.info(s"populating $paramIndexName with params")
    val params = client.execute{
      bulk {
        indexInto(paramIndexName / "params").fields("message" -> "default - refreshed").id(1)
      }
    }
    params
  }
  
  def _populateFakeSeries(implicit client:ElasticClient) = {
    case class Cell(name:String, time:Long, value:Double)
    def mkcell(name:String, timeindex:Int) = Cell(name, now+timeindex*1000,scala.math.random*10d) 
    def sampleData={
      Stream.from(1).take(7200).map(ti => mkcell("x",ti)::mkcell("y",ti)::mkcell("z",ti)::Nil)
    }
    def bulks = for {
      grouped <- sampleData.grouped(500)
    } yield {
      client.execute{
        bulk {
          for {
            cells <- grouped
            cell <- cells
          } yield {
          indexInto(seriesIndexName / "cells").fields(
                "name"->cell.name,
                "time"->cell.time,
                "value"->cell.value
              )
          }
        }
      }
    }
    bulks
  }
  
  def initializeIfNeeded(implicit client:ElasticClient) = {
    logger.info(s"Checking if some initialization is required at elasticsearch side")
    client
      .execute{ indexExists(paramIndexName) }
      .flatMap{ idx => 
        if (idx.isExists) {
          logger.info(s"$paramIndexName index already exists")
          _deleteIndex.flatMap(_ => _createIndex ).flatMap(_ => _populateParams) // TODO - no results check
        } else {
          logger.info(s"$paramIndexName index is missing")
          _createIndex.flatMap(_ => _populateParams)   // TODO - no results check
        }
      }
  }
  
  lazy val clientFuture:TwitterFuture[ElasticClient] = ScalaFuture {
    import scala.util.Properties._
    val elkUriConfigKey="ELK_URI"
    val defaultUri = "elasticsearch://localhost:9300"
    val uri =
      envOrNone(elkUriConfigKey)
        .orElse(propOrNone(elkUriConfigKey))
        .getOrElse(defaultUri)
    val elasticUri = ElasticsearchClientUri(uri)
    logger.info(s"Connecting to $elasticUri")
    implicit val client = ElasticClient.transport(elasticUri)
    client
  }.flatMap(cl => initializeIfNeeded(cl).map(_ => cl)).as[TwitterFuture[ElasticClient]] // to avoid await for init...
  
  def getMessage:TwitterFuture[String] = clientFuture.flatMap{ client=>
    client
      .execute {get(1).from(paramIndexName / "params")}
      .map(_.sourceField("message").toString)
      .as[TwitterFuture[String]]
  }
  
  def setMessage(msg:String) = clientFuture.flatMap{ client=> 
      client
        .execute {update(1).in(paramIndexName / "params").docAsUpsert("message" -> msg)}
        .as[TwitterFuture[RichUpdateResponse]]
  }
  
}
