import ru.kolyvan.redis.Redis
import ru.kolyvan.redis.Conv._

object Main {

  def resetDatabase(databaseClient: Redis) = {
    databaseClient.flushdb()
    databaseClient set("pages:globalindex", B(0))
    databaseClient incr "pages:globalindex"
  }

  def main(args: Array[String]) {
    val databaseClient = Redis("localhost", 6379)
    //resetDatabase(dbc)
    val majorUrl = "http://habrahabr.ru/"
    val searchDepth = 3
    val crawler = new Crawler()
    crawler.grabHost(majorUrl, databaseClient, searchDepth)
  }

}
