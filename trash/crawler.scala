import com.sun.xml.internal.messaging.saaj.util.ByteOutputStream
import java.net.URL
import java.util.regex.Pattern
import java.util.Scanner
import java.security.MessageDigest
import scala.util.Marshal
import scala.io.Source
import scala.collection.immutable
import java.io._

object Appp  {

    case class StoredPage (URL : String, page_html : String, keyWords : scala.collection.mutable.HashMap[String, Int], links : List[String], images : List[String], hash : Array[Byte]) 
    def md5(s: String) = {
        MessageDigest.getInstance("MD5").digest(s.getBytes)
    }

    def get_splited_page(s: String) = {
        new java.util.StringTokenizer(s.toLowerCase, " \n,./\\+-=()<>!@#$%^&*|", true)
    }

    class Crawler {
        def openResourceInputStream(url : String) = {
            val connection = new URL(url).openConnection()
            connection.setRequestProperty("User-Agent", "Mozilla/5.0 (compatible;)")
            connection.connect()
            connection.getInputStream
        }

        val hrefPattern = Pattern.compile("""http:\/\/[A-Za-z0-9-_]+\.[A-Za-z0-9-_:%&\?\/.=]+""")
        val imagePattern = Pattern.compile("(src|href)[^><\"]*\"([^\"\']*\\.(gif|jpg|png))\"")
        val bodyPattern = Pattern.compile("<script>(\\S+)</script>")

        def grabUrl(url : String) = {
            val pageScanner = new Scanner(openResourceInputStream(url))
            val raw_page = scala.io.Source.fromInputStream(openResourceInputStream(url)).getLines().mkString("\n")//getPage()
            val href_matcher = hrefPattern.matcher(raw_page)
            val image_matcher = imagePattern.matcher(raw_page)

            def getHref(res : List[String] = List[String]()) : List[String] = {
                if (href_matcher.find()) {
                    getHref(res :+ href_matcher.group(0).toString())
                }
                else {
                    res
                }
            }
            def getImages(res : List[String] = List[String]()) : List[String] = {
                if (image_matcher.find()) {
                    getHref(res :+ image_matcher.group(2).toString())
                }
                else {
                    res
                }
            }
            
            var keyWords = new scala.collection.mutable.HashMap[String, Int]()//TODO: FIX IT
            
            val tokens = get_splited_page(raw_page)
            while (tokens.hasMoreTokens()) {
                val word = tokens.nextToken()
                if (!keyWords.contains(word)) keyWords.put(word, 0)
                keyWords(word) += 1
            }
            val hrefs = getHref().filter((x : String) => !(x.endsWith(".jpg") || x.endsWith(".ico") || x.endsWith(".png") || x.endsWith(".gif")))
            val images = getImages()
            StoredPage(url, "", keyWords, hrefs, images, md5(raw_page))
        }
    }

    def search(query : String, pages : scala.collection.mutable.HashMap[String, StoredPage]) = {
        val tokenizer = get_splited_page(query)
        def tokenizer_toList(res: List[String] = List()): List[String] = {
            if (tokenizer.hasMoreTokens()) {
                tokenizer_toList(res :+ tokenizer.nextToken())
            } else {
                res
            }
        }

        val keyWords = tokenizer_toList() 
        def filterFunc(page : StoredPage, words: List[String]) : Boolean = {
            if (words.length == 0) {
                return true
            }
            else {
                if (! page.keyWords.contains(words.head)) false
                else (filterFunc(page, words.tail))
            }
        }

        def freqRange(x : StoredPage) = {
            x.keyWords.filter((x : (String, Int)) => keyWords.contains(x._1)).values.sum
        }

        val listtosort = pages.filter((x : (String, StoredPage)) => filterFunc(x._2, keyWords)).values.toList
        listtosort sortWith {freqRange(_) > freqRange(_) }
    }
    
    def SerialisePages(fname : String, foo : scala.collection.mutable.HashMap[String, StoredPage]) = {
        val out = new FileOutputStream(fname)
        out.write(Marshal.dump(foo.values.toList))
        out.close
    }

    def DeSerialisePages(fname : String) = {
        val in = new FileInputStream(fname)
        val bytes = Stream.continually(in.read).takeWhile(-1 !=).map(_.toByte).toArray
        val lst = Marshal.load[List[StoredPage]](bytes)
        var pages = scala.collection.mutable.HashMap[String, StoredPage]()
        for(p <- lst) {
            pages.put(p.URL, p)
        }
        pages
    }

    def main(args : Array[String]) = {
        def grabHost(major_url : String, max_depth : Int = 1) = {
            val crawler = new Crawler()
            var pages = scala.collection.mutable.HashMap[String, StoredPage]()
            println("Start grabing " + major_url)
            def walker(url : String, depth : Int) : Unit = {
                if (!pages.contains(url)) {
                val page = crawler.grabUrl(url)
                println(pages.size  + " walking page url " + page.URL + "  num of hrefs " + page.links.length + " hash " + page.hash.toList)
                pages.put(url, page)
                if (depth < max_depth)
                    page.links.filter(_.startsWith(major_url)).par.foreach((x:String) => {
                        try {    
                            walker(x, depth+1)
                        } catch {
                            case e: Exception => println(e)
                        }
                    })
                } else {
                    println("passing " + url) 
                    /*val page = pages(url)
                    if (depth < max_depth)
                        page.links.filter(_.startsWith(major_url)).par.foreach((x:String) => {
                            try {    
                                walker(x, depth+1)
                            } catch {
                                case e: Exception => println(e)
                            }
                        })*/
                }
            }
            walker(major_url, 0)
            pages
        }
        val major_url = "http://habrahabr.ru/"
        val pages = grabHost(major_url, 2)
        println("Index size: " + pages.size)
        println("Ready to search")
        var ok = true
        while( ok ) {
            println("Type query:")
            val ln = readLine()
            ok = ln != null
            if( ok ) {
                search(ln, pages).foreach((x:StoredPage) => println(x.URL))

            }
        }
        //search("java", pages).foreach((x:StoredPage) => println(x.URL))
    }
}
