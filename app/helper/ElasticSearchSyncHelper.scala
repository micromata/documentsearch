package helper

import global.Global
import org.apache.commons.lang3.StringUtils
import org.elasticsearch.index.query.QueryBuilders
import org.elasticsearch.common.unit.TimeValue
import org.slf4j.{LoggerFactory, Logger}

object ElasticSearchSyncHelper {

  private val log: Logger = LoggerFactory.getLogger(ElasticSearchSyncHelper.getClass)

  private var syncing: Boolean = false

  def sync(): Unit = {
    val command = Global.cronCommand
    if (StringUtils.isNotEmpty(command)) {
      log.info("Sync: Executing cron command")
      try {
        new ProcessBuilder(command).directory(Global.documentBaseDir).start().waitFor()
      } catch {
        case o_O: Exception =>
          log.warn("Sync: Unable to run cron command: " + command, o_O)
      }
    }
    log.info("Sync: Syncing files to ElasticSearch")
    syncing = true
    log.info("Sync: Clean old index")
    try {
      // clear old index
      ElasticSearchHelper.client.prepareDeleteByQuery(ElasticSearchHelper.index).
        setQuery(QueryBuilders.matchAllQuery()).
        setTimeout(TimeValue.timeValueSeconds(5)).
        setTypes(ElasticSearchHelper.indexType).execute().actionGet()
    } catch {
      case o_O: Exception => log.warn("Sync: Unable to clear index", o_O)
    }
    // re index
    log.info("Sync: re-index documents")
    val bulkIndex = ElasticSearchHelper.client.prepareBulk()
    // TODO Bulk Index
    syncing = false
    log.info("Sync: Syncing files to ElasticSearch - successfully")
  }
}
