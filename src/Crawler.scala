import java.net.URL
import java.security.MessageDigest
import java.util.regex.Pattern

/**
 * Created with IntelliJ IDEA.
 * User: kirill
 * Date: 02.03.13
 * Time: 14:28
 * To change this template use File | Settings | File Templates.
 */

case class StoredPage(URL: String, page_html: String, keyWords: scala.collection.mutable.HashMap[String, Int], links: List[String], images: List[String], hash: Array[Byte])

object CrawlerSupport {
    def get_splited_page(s: String) = {
        new java.util.StringTokenizer(s.toLowerCase, " \n,./\\+-=()<>!@#$%^&*|", true)
    }

    def md5(s: String) = {
        MessageDigest.getInstance("MD5").digest(s.getBytes)
    }
}

class Crawler {

    def openResourceInputStream(url: String) = {
        val connection = new URL(url).openConnection()
        connection.setRequestProperty("User-Agent", "Mozilla/5.0 (compatible;)")
        connection.connect()
        connection.getInputStream
    }

    val hrefPattern = Pattern.compile( """http:\/\/[A-Za-z0-9-_]+\.[A-Za-z0-9-_:%&\?\/.=]+""")
    val imagePattern = Pattern.compile("(src|href)[^><\"]*\"([^\"\']*\\.(gif|jpg|png))\"")
    //val bodyPattern = Pattern.compile("<script>(\\S+)</script>")

    def grabUrl(url: String) = {
        val raw_page = scala.io.Source.fromInputStream(openResourceInputStream(url)).getLines().mkString("\n") //getPage()
        val href_matcher = hrefPattern.matcher(raw_page)
        val image_matcher = imagePattern.matcher(raw_page)

        def getHref(res: List[String] = List[String]()): List[String] = {
            if (href_matcher.find()) {
                getHref(res :+ href_matcher.group(0))
            }
            else {
                res
            }
        }
        def getImages(res: List[String] = List[String]()): List[String] = {
            if (image_matcher.find()) {
                getHref(res :+ image_matcher.group(2))
            }
            else {
                res
            }
        }

        var keyWords = new scala.collection.mutable.HashMap[String, Int]() //TODO: FIX IT

        val tokens = CrawlerSupport.get_splited_page(raw_page)
        while (tokens.hasMoreTokens()) {
            val word = tokens.nextToken()
            if (!keyWords.contains(word)) keyWords.put(word, 0)
            keyWords(word) += 1
        }
        val hrefs = getHref().filter((x: String) => !(x.endsWith(".jpg") || x.endsWith(".ico") || x.endsWith(".png") || x.endsWith(".gif")))
        val images = getImages()
        StoredPage(url, "", keyWords, hrefs, images, CrawlerSupport.md5(raw_page))
    }

    def grabHost(major_url: String, max_depth: Int = 1) = {
        var pages = scala.collection.mutable.HashMap[String, StoredPage]()
        def walker(url: String, depth: Int): Unit = {
            if (!pages.contains(url)) {
                val page = grabUrl(url)
                println(pages.size + " walking page url " + page.URL + "  num of hrefs " + page.links.length)
                pages.put(url, page)
                if (depth < max_depth)
                    page.links.filter(_.startsWith(major_url)).par.foreach((x: String) => {
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
        walker(major_url, 0)
        pages
    }
}