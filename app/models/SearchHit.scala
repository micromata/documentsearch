package models

case class SearchHit(score: Float,
                     file: String,
                     folder: String,
                     content: String,
                     highlights: List[String])
