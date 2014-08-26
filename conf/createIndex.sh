#!/bin/sh
curl -X DELETE http://localhost:9200/documentsearch

curl -X POST http://localhost:9200/documentsearch -d '{
  "mappings" : {
    "document" : {
      "properties" : {
        "content" : {
          "type" : "attachment",
          "fields" : {
            "content"  : { "store" : "yes" },
            "author"   : { "store" : "yes" },
            "title"    : { "store" : "yes" },
            "date"     : { "store" : "yes" },
            "keywords" : { "store" : "yes", "analyzer" : "keyword" },
            "content_type" : { "store" : "yes" },
            "content_length" : { "store" : "yes" }
          }
        },
        "_name"        : { "type": "string", "store" : "yes" },
        "_folder"      : { "type": "string", "store" : "yes" }
      }
    }
  }
}'
