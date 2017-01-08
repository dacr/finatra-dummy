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



import org.elasticsearch.action.admin.indices.delete.DeleteIndexResponse
import org.elasticsearch.action.admin.indices.create.CreateIndexResponse
import com.sksamuel.elastic4s.indexes.RichIndexResponse
import com.sksamuel.elastic4s.bulk.RichBulkResponse


class Persistency {
  val logger=org.slf4j.LoggerFactory.getLogger(getClass)
  import scala.concurrent.ExecutionContext.Implicits.global
  
  val indexName=s"dummy-global"
  
  def _deleteIndex(client:ElasticClient):ScalaFuture[DeleteIndexResponse] = {
    logger.info(s"deleting index $indexName (we're in debug mode)")
    client.execute{deleteIndex(indexName)}
  }
  
  def _createIndex(client:ElasticClient):ScalaFuture[CreateIndexResponse] = {
    logger.info(s"creating index $indexName")
    client.execute{
      createIndex(indexName) mappings(
        mapping("params") as Seq(
            textField("message")
            ),
        mapping("cells") as Seq(
            keywordField("name"),
            dateField("time"),
            doubleField("value")
        )
      )
    }
  }
  
  def _populate(client:ElasticClient):ScalaFuture[List[RichBulkResponse]] = {
    logger.info(s"populating $indexName")
    val params = client.execute{
      bulk {
        indexInto(indexName / "params").fields("message" -> "default - refreshed").id(1)
      }
    }
    val series = _populateFakeSeries(client)
    ScalaFuture.sequence(params::series)
  }
  def _populateFakeSeries(client:ElasticClient) = {
    def now=System.currentTimeMillis()
    val sampleData=1.to(7200).map(i=> now+i*1000 -> scala.math.random*10d)
    val bulks = for {
      grouped <- sampleData.grouped(500).toList
    } yield {
      client.execute{
        bulk {
          for {
            (time, value) <- grouped
          } yield {
          indexInto(indexName / "cells").fields(
                "name"->"x",
                "time"->time,
                "value"->value
              )
          }
        }
      }
    }
    bulks
  }
  
  def initializeIfNeeded(client:ElasticClient) = {
    logger.info(s"Checking if some initialization is required at elasticsearch side")
    val r = client
      .execute{ indexExists(indexName) }
      .flatMap{ idx => 
        if (idx.isExists) {
          logger.info(s"$indexName index already exists")
          for {
           di <-_deleteIndex(client)
           ci <-_createIndex(client)
          } yield {
              _populate(client)
          }
        } else {
          logger.info(s"$indexName index is missing")
          for {
            ci <-_createIndex(client)
          } yield {
            _populate(client)
          }
        }
      }
    r.await
    logger.info("initializeIfNeeded RESULT = "+r.value)
  }
  
  lazy val client = {
    import scala.util.Properties._
    val elkUriConfigKey="ELK_URI"
    val defaultUri = "elasticsearch://localhost:9300"
    val uri =
      envOrNone(elkUriConfigKey)
        .orElse(propOrNone(elkUriConfigKey))
        .getOrElse(defaultUri)
    val elasticUri = ElasticsearchClientUri(uri)
    logger.info(s"Connecting to $elasticUri")
    val client = ElasticClient.transport(elasticUri)
    initializeIfNeeded(client)   
    client
  }
  
  def getMessage:TwitterFuture[String] = {
    client
      .execute {get(1).from(indexName / "params")}
      .map(_.sourceField("message").toString)
      .as[TwitterFuture[String]]
  }
  
  def setMessage(msg:String) = client.execute {
    update(1).in(indexName / "params").docAsUpsert("message" -> msg)
  }
}
