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



class Persistency {
  val logger=org.slf4j.LoggerFactory.getLogger(getClass)
  import scala.concurrent.ExecutionContext.Implicits.global
  
  val indexName=s"dummy-global"
  
  def _deleteIndex(client:ElasticClient):TwitterFuture[DeleteIndexResponse] = {
    client.execute{deleteIndex(indexName)}
  }.as[TwitterFuture[DeleteIndexResponse]]
  
  def _createIndex(client:ElasticClient):TwitterFuture[CreateIndexResponse] = {
    client.execute{
      createIndex(indexName) mappings {
        mapping("params") as Seq(
            textField("message")
            )
        mapping("cells") as Seq(
            keywordField("name"),
            dateField("time"),
            doubleField("value")
        )
      }
    }.as[TwitterFuture[CreateIndexResponse]]
  }
  
  def initializeIfNeeded(client:ElasticClient):Unit = {
    logger.info(s"Checking if some initialization is required at elasticsearch side")
    client
      .execute{ indexExists(indexName) }
      .collect{ case idx => 
        if (idx.isExists) {
          logger.info(s"$indexName index already exists")
        } else {
          logger.info(s"$indexName index is missing")
          _createIndex(client)
        }
      }
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
//    client
//      .execute {get(1).from(s"$indexName/params")}
//      .andThen {case Failure(err) => logger.error(s"couldn't get params", err)}
//      .map(_.sourceField("message").toString)
    TwitterFuture {"truc"}
  }
  
  def setMessage(msg:String) = client.execute {
    update(1).in(s"$indexName/params").docAsUpsert("message" -> msg)
  }
}
