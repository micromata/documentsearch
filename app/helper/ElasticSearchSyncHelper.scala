package helper

import global.Global
import org.apache.commons.lang3.StringUtils
import org.elasticsearch.index.query.QueryBuilders
import org.elasticsearch.common.unit.TimeValue
import org.slf4j.{LoggerFactory, Logger}
import java.io.File
import org.apache.commons.io.FileUtils
import org.elasticsearch.action.index.IndexRequestBuilder
import org.elasticsearch.common.xcontent.XContentFactory._

object ElasticSearchSyncHelper {

  private val log: Logger = LoggerFactory.getLogger(ElasticSearchSyncHelper.getClass)

  private var syncing: Boolean = false

  def isSyncing = syncing

  def sync(): Unit = {
    val command = Global.cronCommand
    if (StringUtils.isNotEmpty(command)) {
      log.info("Sync: Executing cron command")
      try {
        val file = new File(Global.documentBaseDir.getAbsolutePath + "/update.sh")
        FileUtils.write(file, "#!/bin/sh\n\n" + command)
        Runtime.getRuntime.exec("chmod u+x " + file.getAbsolutePath)
        Runtime.getRuntime.exec(file.getAbsolutePath).waitFor()
        FileUtils.deleteQuietly(file)
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
    val requests = createIndexRequestRecursively(Global.documentBaseDir)
    if (requests.size > 0) {
      requests.foreach(indexAction => bulkIndex.add(indexAction))
      val response = bulkIndex.setTimeout(TimeValue.timeValueSeconds(5)).execute().actionGet()
      if (response.hasFailures) {
        log.warn("Sync: Warnings during bulk indexing documents: " + response.buildFailureMessage())
      }
    }
    syncing = false
    log.info("Sync: Syncing files to ElasticSearch - successfully")
  }

  private def createIndexRequestRecursively(file: File): List[IndexRequestBuilder] = {
    if (file.exists()) {
      // file exist -> handle file as file or directory
      if (file.isDirectory) {
        // is directory -> recursively call this method for all containing files
        file.listFiles().map {
          f =>
            createIndexRequestRecursively(f)
        }.flatten.toList
      } else {
        if (file.isFile == true && file.getName.startsWith(".") == false) {
          // is a valid file -> prepare index request
          List(ElasticSearchHelper.client.prepareIndex(ElasticSearchHelper.index,
            ElasticSearchHelper.indexType, HashHelper.hashFileName(file))
            .setSource(// _body
              jsonBuilder()
                .startObject()
                .field("_name", file.getName)
                .field("_folder", file.getParentFile.getAbsolutePath.replace(Global.documentFolder, ""))
                .field("content", HashHelper.base64(file))
                .endObject()
            ).setCreate(true)
          )
        } else {
          // is not a valid file (e.g. .DS_Store)
          List() // empty
        }
      }
    } else {
      // file does not exist
      List() // empty
    }
  }

}
