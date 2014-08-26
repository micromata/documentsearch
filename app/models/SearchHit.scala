package models

import java.util

case class SearchHit(score: Float,
                     query: String,
                     file: String,
                     folder: String,
                     content: String,
                     highlights: util.List[String])
