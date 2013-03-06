import java.net.URL
import java.util.regex.Pattern
import java.security.MessageDigest
import ru.kolyvan.redis.Conv._
import ru.kolyvan.redis.Redis
import scala.util.Marshal
import java.io._
//import com.redis._
//import Conv._

object Main {

    def redis_tst() = {
        //val r = new RedisClient("localhost", 6379)
        val r = Redis()
        //println(r.set("key", B("KILL THEM ALL")))
        //println(r.set("key2", B(1024)))

        println(S(r.get("key")).get)
        //println()
        for(x <-  r.keys("*"))
            println(S(r.get(x)))

        //println(rcl.keys("*").get)
    }

    def main(args: Array[String]) = {
        redis_tst()

        /*val crawler = new Crawler()
        val major_url = "http://habrahabr.ru/"
        val search_depth = 1
        val pages = crawler.grabHost(major_url, search_depth)
        println("Index size: " + pages.size)
        println("Ready to search")
        val se = new SearchEngine()
        var ok = true
        while (ok) Redis("localhost", 6379){
            println("Type query:")
            val ln = {
                readLine()
            }
            ok = ln != null
            if (ok) {
                se.search(ln, pages).foreach((x: StoredPage) => println(x.URL))

            }
        }*/
        //search("java", pages).foreach((x:StoredPage) => println(x.URL))
    }
}
