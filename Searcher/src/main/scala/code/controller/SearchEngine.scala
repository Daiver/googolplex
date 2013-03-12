package org.googolplex

import collection.mutable
import java.util.StringTokenizer
import ru.kolyvan.redis
import ru.kolyvan.redis.Redis
import ru.kolyvan.redis.Conv._

class SearchEngine {

  def search(query: String, databaseClient: Redis) = {
    val keyWords = {
      val keyWordsTokenizer = new StringTokenizer(query.toLowerCase, " \n,.:;\"'/\\+-=!@#$%^&*|_()<>{}[]~`\t", true)
      def toList(res: List[String] = List()): List[String] =
        if (keyWordsTokenizer.hasMoreTokens) toList(keyWordsTokenizer.nextToken() :: res)
        else res

      toList()
    }

    val results = new mutable.LinkedHashMap[String, Float]()
    keyWords.foreach((x: String) => {
      databaseClient.zrange2("pages:KW:" + x, 0, -1).map(
        (data: (redis.Bytes, Float)) => (S(data._1), data._2)).foreach {
          ((d: (String, Float)) => if (results.contains(d._1)) results(d._1) += d._2
          else results(d._1) = d._2)
        }
    })
    //results.toList.sortBy(_._2).map(_._1)
    results.toList.sortBy(_._2).map((x:(String, Float)) => (x._1, S(databaseClient.get("pages:URL:" + x._1 + ":title").get).capitalize))
  }
}