package org.googolplex

import java.util.StringTokenizer
import com.redis.RedisClient
import scala.collection._
import scala._

object SearchEngine {

  def searchPages(query: String, databaseClient: RedisClient) = {
    databaseClient.select(0)
    val results = search(query, databaseClient)
    results.map {
      case (url: String, score: Double) => (url, databaseClient.hget("page:" + url, "title").get.capitalize)
    }
  }

  def searchImages(query: String, databaseClient: RedisClient) = {
    databaseClient.select(1)
    search(query, databaseClient)
  }

  private def search(query: String, databaseClient: RedisClient) = {
    val keyWords = {
      val keyWordsTokenizer = new StringTokenizer(query.toLowerCase, " \n,.:;\"'/\\+-=!@#$%^&*|_()<>{}[]~`\t", true)
      def toList(res: List[String] = List()): List[String] =
        if (keyWordsTokenizer.hasMoreTokens) toList(keyWordsTokenizer.nextToken() :: res)
        else res

      toList()
    }

    val results = new mutable.LinkedHashMap[String, Double]()
    keyWords.foreach((word: String) => {
      databaseClient.zrangeWithScore("keyword:" + word, 0, -1).get.foreach {
        case (url, score) =>
          if (results contains url) results(url) += score
          else results += ((url, score))
      }
    })

    results.toList.sortBy(_._2)(Ordering.fromLessThan(_ > _))
  }

}