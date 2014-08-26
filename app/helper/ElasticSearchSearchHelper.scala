package helper

import models.{SearchHit, SearchResult}
import org.elasticsearch.index.query.QueryBuilders
import scala.collection.JavaConversions._
import org.slf4j.{LoggerFactory, Logger}

object ElasticSearchSearchHelper {

  private val log: Logger = LoggerFactory.getLogger(ElasticSearchSearchHelper.getClass)

  private def emptyResult = SearchResult(List())

  def search(query: String): SearchResult = {
    if (ElasticSearchSyncHelper.isSyncing == false) {
      try {
        val result = ElasticSearchHelper.client.prepareSearch(ElasticSearchHelper.index).
          setTypes(ElasticSearchHelper.indexType).
          addField("_name").
          addField("_folder").
          addField("content").
          addHighlightedField("content", 100, 100). // this is magic :)
          setQuery(QueryBuilders.multiMatchQuery(query, "_name", "_folder", "content")).
          execute().actionGet()
        SearchResult(result.getHits.toList.sortBy(_.score()).reverse.map {
          entry =>
            val file = if (entry.field("_name") != null) entry.field("_name").getValue[String] else ""
            val folder = if (entry.field("_folder") != null) entry.field("_folder").getValue[String] else ""
            val content = if (entry.field("content") != null) entry.field("content").getValue[String] else ""

            val contentHighlights = entry.highlightFields().get("content")
            val highlights: List[String] = if (contentHighlights != null) {
              contentHighlights.fragments().map { t => t.string()}.toList
            } else {
              List()
            }
            SearchHit(entry.score, query, file, folder, content, highlights)
        }.toList)
      } catch {
        case o_O: Exception =>
          log.warn("Unable to search in ElasticSearch", o_O)
          emptyResult
      }
    } else {
      emptyResult
    }
  }

}
