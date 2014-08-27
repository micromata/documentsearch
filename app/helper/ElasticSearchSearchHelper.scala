package helper

import models.{SearchHit, SearchResult}
import org.elasticsearch.index.query.QueryBuilders
import scala.collection.JavaConversions._
import org.slf4j.{LoggerFactory, Logger}

object ElasticSearchSearchHelper {

  private val log: Logger = LoggerFactory.getLogger(ElasticSearchSearchHelper.getClass)

  private def emptyResult(query: String) = SearchResult(query, List())

  def search(query: String): SearchResult = {
    if (ElasticSearchSyncHelper.isSyncing == false) {
      try {
        // query
        val result = ElasticSearchHelper.client.prepareSearch(ElasticSearchHelper.index).
          setTypes(ElasticSearchHelper.indexType).
          addField("_name").
          addField("_folder").
          addField("content").
          addHighlightedField("content", 100, 100). // this is magic :)
          setQuery(QueryBuilders.multiMatchQuery(query, "_name", "_folder", "content")).
          execute().actionGet()

        // extract
        val hits = result.getHits.toList.sortBy(_.score()).reverse.map {
          searchHit =>
            val file = if (searchHit.field("_name") != null) searchHit.field("_name").getValue[String] else ""
            val folder = if (searchHit.field("_folder") != null) searchHit.field("_folder").getValue[String] else ""
            val content = if (searchHit.field("content") != null) searchHit.field("content").getValue[String] else ""

            val contentHighlights = searchHit.highlightFields().get("content")
            val highlights: List[String] = if (contentHighlights != null) {
              contentHighlights.fragments().map { t => t.string()}.toList
            } else {
              List()
            }
            SearchHit(searchHit.score, file, folder, content, highlights)
        }.toList

        // result
        SearchResult(query, hits)
      } catch {
        case o_O: Exception =>
          log.warn("Unable to search in ElasticSearch", o_O)
          emptyResult(query) // maybe reasonable error handling?
      }
    } else {
      emptyResult(query) // maybe reasonable error handling?
    }
  }

}
