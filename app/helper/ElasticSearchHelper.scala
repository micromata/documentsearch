package helper

import org.slf4j.{LoggerFactory, Logger}
import org.elasticsearch.common.settings.ImmutableSettings
import org.elasticsearch.node.NodeBuilder._
import org.elasticsearch.node.Node
import org.elasticsearch.client.Client

object ElasticSearchHelper {

  private val log: Logger = LoggerFactory.getLogger(ElasticSearchHelper.getClass)

  private val elasticsearchSettings = ImmutableSettings.settingsBuilder()
    .put("path.data", "target/elasticsearch-data")
    .put("cluster.name", "documentsearch")
    .put("http.port", 9200)

  private val node: Node = nodeBuilder()
    .local(true)
    .settings(elasticsearchSettings.build())
    .node()

  def start() = {
    log.info("Starting ElasticSearch")
    node.start()
  }

  def stop() = {
    log.info("Stopping ElasticSearch")
    node.close()
  }
}
