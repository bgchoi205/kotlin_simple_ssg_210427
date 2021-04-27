import java.lang.NullPointerException
import java.lang.NumberFormatException
import java.text.SimpleDateFormat

fun main(){

    println("simple ssg 시작")

    articleRepository.makeTestArticles()

    while(true){

        print("명령어 : ")
        val command = readLine()!!.trim()

        val rq = Rq(command)

//        println(rq.getStringParam("title", "1") == "1") // true
//        println(rq.getIntParam("id", -1) == -1) // true

        when (rq.actionPath) {
            "/system/exit" -> {
                println("프로그램을 종료합니다.")
                break
            }
            "/article/write" -> {
                print("제목 입력 : ")
                val title = readLine()!!.trim()
                print("내용 입력 : ")
                val body = readLine()!!.trim()
                articleRepository.addArticle(title, body)
            }
            "/article/list" -> {
                var page = 1
                var searchKeyword = ""

                val rq = Rq(command)
                rq.getIntParam("page",-1)


                val takeCount = 10
                val offsetCount = (page - 1) * takeCount

                val filteredArticles = getFilteredArticles(searchKeyword, offsetCount, takeCount)

                for (article in filteredArticles) {
                    println("${article.id} / ${article.regDate} / ${article.updateDate} / ${article.title}")
                }
            }
            "/article/detail" -> {
                val id = rq.getIntParam("id", 0)

                if (id == 0) {
                    println("id를 입력해주세요.")
                    continue
                }

                val article = articleRepository.articles[id - 1]

                println(article)
            }
        }

    }


}

// 게시물 관련 시작

val articles = mutableListOf<Article>()

data class Article(
    val id: Int,
    val regDate: String,
    val updateDate: String,
    val title: String,
    val body: String
)

fun getArticleById(id: Int): Article? {
    for (article in articles) {
        if (article.id == id) {
            return article
        }
    }

    return null
}

fun getFilteredArticles(searchKeyword: String, offsetCount: Int, takeCount: Int): List<Article> {
    var filtered1Articles = articles

    if (searchKeyword.isNotEmpty()) {
        filtered1Articles = mutableListOf()

        for (article in articles) {
            if (article.title.contains(searchKeyword)) {
                filtered1Articles.add(article)
            }
        }
    }

    val filtered2Articles = mutableListOf<Article>()

    val startIndex = filtered1Articles.lastIndex - offsetCount
    var endIndex = startIndex - (takeCount - 1)

    if (endIndex < 0) {
        endIndex = 0
    }

    for (i in startIndex downTo endIndex) {
        filtered2Articles.add(filtered1Articles[i])
    }

    return filtered2Articles
}

object articleRepository {
    val articles = mutableListOf<Article>()
    var lastId = 0

    fun addArticle(title: String, body: String) {
        val id = ++lastId
        val regDate = Util.getNowDateStr()
        val updateDate = Util.getNowDateStr()
        articles.add(Article(id, regDate, updateDate, title, body))
    }

    fun makeTestArticles() {
        for (id in 1..100) {
            addArticle("제목_$id", "내용_$id")
        }
    }
}
// 게시물 관련 끝

// 유틸 관련 시작
fun readLineTrim() = readLine()!!.trim()

object Util {
    fun getNowDateStr(): String {
        val format = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")

        return format.format(System.currentTimeMillis())
    }
}
// 유틸 관련 끝

class Rq(command:String){

    val actionPath : String
    val paramMap : Map<String, String>

    init{
        val commandBits = command.split("?", limit = 2)

        actionPath = commandBits[0].trim()

        val queryStr = if(commandBits.lastIndex == 1 && commandBits[1].isNotEmpty()){
            commandBits[1].trim()
        }else {
            ""
        }

        paramMap = if (queryStr.isEmpty()) {
            mapOf()
        } else {
            val paramMapTemp = mutableMapOf<String, String>()

            // queryStr = id=1&body=2&title=3&age
            val queryStrBits = queryStr.split("&")

            for (queryStrBit in queryStrBits) {
                // queryStrBit = id=1
                val queryStrBitBits = queryStrBit.split("=", limit = 2)
                val paramName = queryStrBitBits[0]
                val paramValue = if (queryStrBitBits.lastIndex == 1 && queryStrBitBits[1].isNotEmpty()) {
                    queryStrBitBits[1].trim()
                } else {
                    ""
                }

                if (paramValue.isNotEmpty()) {
                    paramMapTemp[paramName] = paramValue
                }
            }

            paramMapTemp.toMap()
        }
    }

    fun getStringParam(name : String, default : String): String {

        return paramMap[name] ?: default


//        return if(paramMap[name] == null){
//            default
//        }
//        else{
//            paramMap[name]!!
//        }


//        return try{
//            paramMap[name]!!
//        }
//        catch(e:NullPointerException){
//            default
//        }
    }

    fun getIntParam(name : String, default : Int): Int {
//        return  paramMap[name]!!.toInt() ?: default

        return if(paramMap[name] != null){
            try{
            paramMap[name]!!.toInt()
            }
            catch(e : NumberFormatException){
                default
            }
        }
        else{
            default
        }
    }

}

