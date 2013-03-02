import java.net.URL
import java.util.regex.Pattern
import java.security.MessageDigest
import scala.util.Marshal
import java.io._


object Main {

    def main(args: Array[String]) = {
        val crawler = new Crawler()
        val major_url = "http://habrahabr.ru/"
        val search_depth = 1
        val pages = crawler.grabHost(major_url, search_depth)
        println("Index size: " + pages.size)
        println("Ready to search")
        val se = new SearchEngine()
        var ok = true
        while (ok) {
            println("Type query:")
            val ln = {
                readLine()
            }
            ok = ln != null
            if (ok) {
                se.search(ln, pages).foreach((x: StoredPage) => println(x.URL))

            }
        }
        //search("java", pages).foreach((x:StoredPage) => println(x.URL))
    }
}
