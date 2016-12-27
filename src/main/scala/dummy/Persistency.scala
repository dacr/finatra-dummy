package dummy

import com.sksamuel.elastic4s.ElasticClient
import com.sksamuel.elastic4s.ElasticDsl._
import com.sksamuel.elastic4s.mappings.FieldType._
import com.sksamuel.elastic4s.ElasticsearchClientUri
import com.sksamuel.elastic4s.analyzers._
import scala.concurrent.Future

import org.elasticsearch.action.admin.indices.delete.DeleteIndexResponse
import org.elasticsearch.action.admin.indices.create.CreateIndexResponse



object Persistency {
  val logger=org.slf4j.LoggerFactory.getLogger(getClass)
  import scala.concurrent.ExecutionContext.Implicits.global
  
  val indexName=s"dummy-global"
  
  def _deleteIndex(client:ElasticClient):Future[DeleteIndexResponse] = {
    client.execute{deleteIndex(indexName)}
  }
  def _createIndex(client:ElasticClient):Future[CreateIndexResponse] = {
    client.execute{
      createIndex(indexName) mappings {
        mapping("params") as Seq(
            )
      }
    }
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
    val defaultUri = "http://localhost:9300/"
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
}
