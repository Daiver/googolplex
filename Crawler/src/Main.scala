import com.redis._

object Main {

  def resetDatabase(databaseClient: RedisClient) {
    println("Starting flush")
    databaseClient.flushdb
    databaseClient set("pages:globalindex", 0)
    databaseClient incr "pages:globalindex"
    println("Finish flushing")
  }

  def main(args: Array[String]) {
    val databaseClient = new RedisClient("localhost", 6379)
    resetDatabase(databaseClient)
    val majorUrl = "http://habrahabr.ru/"
    val searchDepth = 4
    val crawler = new Crawler()
    val startTime = System.currentTimeMillis
    crawler.grabHost(majorUrl, databaseClient, searchDepth)
    println("Time [ms]: " + (System.currentTimeMillis - startTime))
  }

}
