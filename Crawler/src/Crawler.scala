import collection.mutable
import io.Source
import java.net.URL
import java.security.MessageDigest
import java.util.regex.Pattern
import java.util.{StringTokenizer, Date}
import ru.kolyvan.redis.Redis
import ru.kolyvan.redis.Conv._

class StoredPage(val url: String,
                 val pageHtml: String,
                 val keyWords: mutable.HashMap[String, Int],
                 val links: List[String],
                 val images: List[String],
                 val hash: Array[Byte],
                 val grabDate: Date) {

  def saveIntoDB(databaseClient: Redis) {
    val index = {
      val existingKeys = databaseClient.keys("pages:*:url:" + url)
      if (existingKeys.length > 0) existingKeys(0).split(":")(1)
      else {
        val res = S(databaseClient get "pages:globalindex").get
        databaseClient incr "pages:globalindex"
        res
      }

    }

    println("index:" + index)
    val pageKey = "pages:" + index + ":"
    println(pageKey + "url:" + url)
    databaseClient.set(pageKey + "url:" + url, B(grabDate.toString))
    keyWords foreach {
      case (k, v) => databaseClient.zadd("pages:KW:" + k, v, B(url))
    }
    //databaseClient.set(page_key + "KW:" + x._1, B(x._2))
  }
}

class Crawler {

  object CrawlerSupport {
    def splitPage(s: String) =
      new StringTokenizer(s.toLowerCase, " \n,.:;\"'/\\+-=!@#$%^&*|_()<>{}[]~`\t", true)

    def md5(s: String) =
      MessageDigest.getInstance("MD5").digest(s.getBytes)
  }

  val hyperLinkPattern = Pattern.compile("""http:\/\/[A-Za-z0-9-_]+\.[A-Za-z0-9-_:%&\?\/.=]+""")

  def openResourceInputStream(url: String) = {
    val connection = new URL(url).openConnection()
    connection.setRequestProperty("User-Agent", "Mozilla/5.0 (compatible;)")
    connection.connect()
    connection.getInputStream
  }

  def grabUrl(url: String) = {
    val rawPage = Source.fromInputStream(openResourceInputStream(url)).getLines().mkString("\n")
    val hyperLinkMatcher = hyperLinkPattern.matcher(rawPage)

    def getHyperLinks(links: List[String] = List()): List[String] =
      if (hyperLinkMatcher.find()) getHyperLinks(hyperLinkMatcher.group(0) :: links)
      else links

    val keyWords = new mutable.HashMap[String, Int]()
    val tokens = CrawlerSupport splitPage rawPage

    while (tokens.hasMoreTokens) {
      val word = tokens.nextToken()
      if (!(keyWords contains word)) keyWords put(word, 0)
      keyWords(word) += 1
    }

    val (images, hyperLinks) = getHyperLinks().partition(link =>
      (link endsWith ".jpg") || (link endsWith ".ico") || (link endsWith ".png") || (link endsWith ".gif"))

    new StoredPage(url, "", keyWords, hyperLinks, images, CrawlerSupport.md5(rawPage), new Date)
  }

  def grabHost(majorURL: String, databaseClient: Redis, maxDepth: Int = 1) {
    //var pages = scala.collection.mutable.HashMap[String, StoredPage]()
    def walker(url: String, depth: Int) {
      //if (!pages.contains(url)) {
      if (databaseClient.keys("pages:*:URL:" + url).length == 0) {
        val page = grabUrl(url)
        println("walking page url " + page.url + "  num of hrefs " + page.links.length)
        page.saveIntoDB(databaseClient)
        //pages.put(url, page)
        if (depth < maxDepth)
          page.links.filter(_ startsWith majorURL).foreach((x: String) => {
            try {
              walker(x, depth + 1)
            } catch {
              case e: Exception => println(e)
            }
          })
      } else {
        println("passing " + url)
      }
    }

    walker(majorURL, 0)
    //pages
  }
}