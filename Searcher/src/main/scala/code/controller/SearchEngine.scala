package org.googolplex

import java.util.StringTokenizer
import com.redis.RedisClient
import scala.collection._

class SearchEngine {

  def search(query: String, databaseClient: RedisClient) = {
    val keyWords = {
      val keyWordsTokenizer = new StringTokenizer(query.toLowerCase, " \n,.:;\"'/\\+-=!@#$%^&*|_()<>{}[]~`\t", true)
      def toList(res: List[String] = List()): List[String] =
        if (keyWordsTokenizer.hasMoreTokens) toList(keyWordsTokenizer.nextToken() :: res)
        else res

      toList()
    }

    val results = new mutable.LinkedHashMap[String, Double]()
    keyWords.foreach((x: String) => {
      databaseClient.zrangeWithScore("pages:KW:" + x, 0, -1).get.foreach {
        case (url, score) =>
          if (results contains url) results(url) += score
          else results += ((url, score))
      }

    })

    results.toList.sortBy(_._2).map {
      case (url: String, score: Double) => (url, databaseClient.get("pages:URL:" + url + ":title").get.capitalize)
    }
  }

}