import collection.immutable.HashMap
import collection.mutable
import io.Source
import java.net.URL
import java.security.MessageDigest
import java.util.regex.Pattern
import java.util.{StringTokenizer, Date}
import com.redis._

class StoredPage(val url: String,
                 val title: String,
                 val pageHtml: String,
                 val keyWords: mutable.HashMap[String, Int],
                 val links: List[String],
                 val images: List[String],
                 val hash: Array[Byte],
                 val grabDate: Date) {

  def saveIntoDB(databaseClient: RedisClient) = {
    val pageKey = "page:" + url
    val pageInfo =
      if (!(databaseClient exists pageKey)) {
        val pageIndex = databaseClient incr "pages:globalindex"
        HashMap("title" -> title, "id" -> pageIndex, "hash" -> hash, "date" -> grabDate)
      }
      else {
        HashMap("title" -> title, "hash" -> hash, "date" -> grabDate)
      }

    println(pageKey)

    databaseClient.hmset(pageKey, pageInfo)

    keyWords.foreach {
      case (word, count) =>
        databaseClient.zadd("page:keyword:" + word, count, url)
    }

    pageKey
  }
}

class Crawler {

  val md5 = MessageDigest.getInstance("MD5")
  val hyperLinkPattern = Pattern.compile( """http:\/\/[A-Za-z0-9-_]+\.[A-Za-z0-9-_:%&\?\/.=]+""")
  val hardSplitters = List("\n", "\t", " ")

  def splitPage(s: String) =
    new StringTokenizer(s.toLowerCase, " \n,.:;\"'/\\+-=!@#$%^&*|_()<>{}[]~`\t", true)

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
    val tokens = splitPage(rawPage)

    var inTag = false
    var readTitle = false
    var curTag = ""
    var title = ""

    while (tokens.hasMoreTokens) {
      val word = tokens.nextToken()

      if ((word equals "<") && !inTag) {
        inTag = true
        if (tokens.hasMoreTokens) curTag = tokens.nextToken()
        readTitle = false
      }
      if (readTitle) {
        title += word
      }
      if ((word equals ">") && inTag) {
        inTag = false
        if (curTag equals "title") {
          readTitle = true
        }
      }

      if (!(keyWords contains word)) keyWords put(word, 0)
      keyWords(word) += 1
    }

    val (images, hyperLinks) = getHyperLinks().partition(link =>
      (link endsWith ".jpg") || (link endsWith ".ico") || (link endsWith ".png") || (link endsWith ".gif"))
    println("title! " + title)
    new StoredPage(url, title, "", keyWords, hyperLinks, images, md5.digest(rawPage.getBytes), new Date)
  }

  def grabHost(majorURL: String, databaseClient: RedisClient, maxDepth: Int = 1) {

    def saveImages(page: StoredPage, pageId: String) = {
      page.images.foreach((x: String) => {
        try {
          val hash = LooksLike.hashImage(x)
          //databaseClient.push
        } catch {
          case e: Exception => println(e)
        }
      })
    }

    def walker(url: String, depth: Int) {
      if (!(databaseClient exists ("page:" + url))) {
        val page = grabUrl(url)
        val pageId = page.saveIntoDB(databaseClient)
        println("walking page url " + page.url + "  number of links" + page.links.length)
        saveImages(page, pageId)
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
    println("Number of crawled pages: " + (databaseClient get "pages:globalindex").get)
  }
}

object LooksLike {

  import java.awt.image.BufferedImage
  import javax.imageio.ImageIO

  def hashImage(imageUrl: String): BigInt = {
    val image = ImageIO.read(f)
    val hash = hashImage(image)
  }

  def hashImage(image: BufferedImage): BigInt = {
    def foldPixels(image: BufferedImage, f: (BigInt, Int) => BigInt) = {
      val w = image.getWidth
      val h = image.getHeight
      def fold(i: Int, j: Int, acc: BigInt): BigInt =
        if (i == h) acc
        else if (j == w) fold(i + 1, 0, acc)
        else fold(i, j + 1, f(acc, image.getRGB(i, j)))

      fold(0, 0, 0)
    }
    val normalizedImage = {
      val w = 8
      val h = w
      val scaledGrayImage = new BufferedImage(w, h, BufferedImage.TYPE_BYTE_GRAY)
      scaledGrayImage.createGraphics().drawImage(image, 0, 0, w, h, null)
      scaledGrayImage
    }
    val imageSize = normalizedImage.getWidth * normalizedImage.getHeight
    val averagePixelColor = foldPixels(normalizedImage, (s, c) => s + c) / imageSize
    def hash(h: BigInt, c: Int) = (h << 1) + (if (c < averagePixelColor) 0 else 1)

    foldPixels(normalizedImage, hash)
  }

}