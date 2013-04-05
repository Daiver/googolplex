import com.redis._
import org.googolplex.crawler.{ImageDownloader, Crawler}

object Main {

  def main(args: Array[String]) {

    def openConnection(baseId: Int) = {
      val databaseClient = new RedisClient("localhost", 6379)
      databaseClient.select(baseId)
      databaseClient
    }

    def resetDatabase(databaseClient:RedisClient, name: String) = {
      databaseClient.flushdb
      databaseClient.set(name + ":globalindex", 0)
      databaseClient
    }

    val pagesDatabaseClient = openConnection(0)
    resetDatabase(pagesDatabaseClient, "pages")
    val imagesDatabaseClient = openConnection(1)
    resetDatabase(imagesDatabaseClient, "images")


    val majorUrl = "http://habrahabr.ru/"
    val searchDepth = 4
    val imageDownloader = new ImageDownloader(pagesDatabaseClient)
    val crawler = new Crawler(imageDownloader)
    val startTime = System.currentTimeMillis

    imageDownloader.start()
    crawler.grabHost(majorUrl, pagesDatabaseClient, searchDepth)

    println("Time [ms]: " + (System.currentTimeMillis - startTime))


  }

}
