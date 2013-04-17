package code.controller

import org.googolplex.SearchEngine
import com.redis.RedisClient

object Main {
  def main(args: Array[String]) {
    println("query> ")
    val query = readLine()
    search(query) foreach println
  }

  def search(query: String) = {
    val searchEngine = new SearchEngine
    val redisClient = new RedisClient("locslhost", 6379)
    searchEngine.search(query, redisClient)
  }

}