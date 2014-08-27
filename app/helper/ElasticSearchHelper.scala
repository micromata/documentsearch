package helper

import org.slf4j.{LoggerFactory, Logger}
import org.elasticsearch.common.settings.ImmutableSettings
import org.elasticsearch.node.NodeBuilder._
import org.elasticsearch.node.Node
import org.elasticsearch.client.Client
import java.io.File
import org.apache.commons.io.FileUtils
import org.elasticsearch.client.transport.TransportClient
import org.elasticsearch.common.transport.InetSocketTransportAddress

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

  val client: Client = node.client()

  // TODO use this client for remote ElasticSearch server
  private val remoteClient: Client =
    new TransportClient(ImmutableSettings.settingsBuilder().build())
    .addTransportAddress(new InetSocketTransportAddress("localhost", 9300))

  val index = "documentsearch"

  val indexType = "document"

  def start() = {
    log.info("Starting ElasticSearch")
    node.start()
    try {
      log.info("delete/create search index")
      val tmpFile = File.createTempFile("documentsearch", "sh")
      FileUtils.copyInputStreamToFile(getClass.getClassLoader.getResourceAsStream("createIndex.sh"), tmpFile)
      Runtime.getRuntime.exec("chmod u+x " + tmpFile.getAbsolutePath)
      Runtime.getRuntime.exec(tmpFile.getAbsolutePath)
      tmpFile.deleteOnExit()
    } catch {
      case o_O: Exception =>
        log.warn("Unable to delete/create search index", o_O)
    }
  }

  def stop() = {
    log.info("Stopping ElasticSearch")
    node.close()
  }
}
