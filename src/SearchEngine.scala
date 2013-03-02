import java.io.{FileInputStream, FileOutputStream}
import util.Marshal

/**
 * Created with IntelliJ IDEA.
 * User: kirill
 * Date: 02.03.13
 * Time: 14:43
 * To change this template use File | Settings | File Templates.
 */
class SearchEngine {
    def search(query: String, pages: scala.collection.mutable.HashMap[String, StoredPage]) = {
        val tokenizer = CrawlerSupport.get_splited_page(query)
        def tokenizer_toList(res: List[String] = List()): List[String] = {
            if (tokenizer.hasMoreTokens()) {
                tokenizer_toList(res :+ tokenizer.nextToken())
            } else {
                res
            }
        }

        val keyWords = tokenizer_toList()
        def filterFunc(page: StoredPage, words: List[String]): Boolean = {
            if (words.length == 0) {
                return true
            }
            else {
                if (!page.keyWords.contains(words.head)) false
                else (filterFunc(page, words.tail))
            }
        }

        def freqRange(x: StoredPage) = {
            x.keyWords.filter((x: (String, Int)) => keyWords.contains(x._1)).values.sum
        }

        val listtosort = pages.filter((x: (String, StoredPage)) => filterFunc(x._2, keyWords)).values.toList
        listtosort sortWith {
            freqRange(_) > freqRange(_)
        }
    }

    def SerialisePages(fname: String, foo: scala.collection.mutable.HashMap[String, StoredPage]) = {
        val out = new FileOutputStream(fname)
        out.write(Marshal.dump(foo.values.toList))
        out.close
    }

    def DeSerialisePages(fname: String) = {
        val in = new FileInputStream(fname)
        val bytes = Stream.continually(in.read).takeWhile(-1 !=).map(_.toByte).toArray
        val lst = Marshal.load[List[StoredPage]](bytes)
        var pages = scala.collection.mutable.HashMap[String, StoredPage]()
        for (p <- lst) {
            pages.put(p.URL, p)
        }
        pages
    }

}
