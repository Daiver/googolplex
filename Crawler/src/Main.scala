import ru.kolyvan.redis.Redis
import ru.kolyvan.redis.Conv._

object Main {

  def resetDatabase(databaseClient: Redis) = {
    println("Starting flush")
    databaseClient.flushdb()
    databaseClient set("pages:globalindex", B(0))
    databaseClient incr "pages:globalindex"
    println("Finish flushing")
  }

  def main(args: Array[String]) {
    val databaseClient = Redis("localhost", 6379)
    resetDatabase(databaseClient)
    val majorUrl = "http://habrahabr.ru/"
    val searchDepth = 4
    val crawler = new Crawler()
    crawler.grabHost(majorUrl, databaseClient, searchDepth)
  }

}
