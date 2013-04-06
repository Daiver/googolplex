import com.redis._
import org.googolplex.crawler.{ImageDownloader, Crawler}

object Main {

  def main(args: Array[String]) {

    def openConnection(baseId: Int) = {
      val databaseClient = new RedisClient("192.168.56.100", 6379)
      databaseClient.select(baseId)
      databaseClient
    }

    def resetDatabase(databaseClient: RedisClient, name: String) = {
      databaseClient.flushdb
      databaseClient.set(name + ":globalindex", 1)
      databaseClient.set("images:globalindex", 1)

      databaseClient
    }

    val pagesDatabaseClient = openConnection(0)
    resetDatabase(pagesDatabaseClient, "pages")
    val imagesDatabaseClient = openConnection(1)
    resetDatabase(imagesDatabaseClient, "images")

    val majorUrl = "http://habrahabr.ru/"
    val searchDepth = 4
    val imageDownloader = new ImageDownloader(imagesDatabaseClient)
    val crawler = new Crawler(imageDownloader)
    val startTime = System.currentTimeMillis

    imageDownloader.start()
    crawler.grabHost(majorUrl, pagesDatabaseClient, searchDepth)
    println("Stop time [ms]: " + (System.currentTimeMillis - startTime))
  }

}
