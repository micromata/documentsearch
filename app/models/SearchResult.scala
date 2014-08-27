package models

case class SearchResult(query: String, hits: List[SearchHit])