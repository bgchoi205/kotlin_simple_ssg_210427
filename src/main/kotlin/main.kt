import Util.getNowDateStr
import java.text.SimpleDateFormat

fun main() {
    println("== SIMPLE SSG 시작 ==")

    articleRepository.makeTestArticles()
    memberRepository.makeTestMember()
    var isLogined = false
    var loginedMember : Member? = null


    while (true) {
        val prompt = if(loginedMember == null){
            print("명령어 : ")
        }else{
            print("${loginedMember.nickName})")
        }

        val command = readLineTrim()

        val rq = Rq(command)

        when (rq.actionPath) {
            "/system/exit" -> {
                println("프로그램을 종료합니다.")
                break
            }

            "/member/join" -> {
                print("아이디 입력 : ")
                val loginId = readLineTrim()
                if(memberRepository.isJoinableId(loginId) == false){
                    println("이미 존재하는 Id 입니다.")
                    continue
                }

                print("비밀번호 입력 : ")
                val loginPw = readLineTrim()
                print("이름 입력 : ")
                val name = readLineTrim()
                print("별명 입력 : ")
                val nickName = readLineTrim()
                val id = memberRepository.joinMember(loginId, loginPw, name, nickName)

                println("$id 번 회원으로 가입 완료")
                println(memberRepository.members)
            }
            "/member/login" -> {
                print("아이디 입력 : ")
                val loginId = readLineTrim()
                val member = memberRepository.getMemberByLoginId(loginId)
                if(member == null){
                    println("존재하지 않는 Id 입니다.")
                    continue
                }
                print("비밀번호 입력 : ")
                val loginPw = readLineTrim()
                if(member.loginPw != loginPw){
                    println("비밀번호가 일치하지 않습니다.")
                    continue
                }
                isLogined = true
                loginedMember = member
                println("${member.nickName}님 환영합니다.")
            }
            "/member/logout" -> {
                isLogined = false
                loginedMember = null
                println("로그아웃")
            }


            "/article/write" -> {
                if(loginedMember == null){
                    println("로그인 후 이용해주세요")
                    continue
                }

                print("제목 : ")
                val title = readLineTrim()
                print("내용 : ")
                val body = readLineTrim()

                val id = articleRepository.addArticle(title, body)

                println("${id}번 게시물이 추가되었습니다.")
            }
            "/article/list" -> {
                if(loginedMember == null){
                    println("로그인 후 이용해주세요")
                    continue
                }
                val page = rq.getIntParam("page", 1)
                val searchKeyword = rq.getStringParam("searchKeyword", "")

                val filteredArticles = articleRepository.getFilteredArticles(searchKeyword, page, 10)

                println("번호 / 작성날짜 / 작성자 / 제목 / 내용")

                for (article in filteredArticles) {

                    println("${article.id} / ${article.regDate} / ${article.title}")
                }
            }
            "/article/detail" -> {
                if(loginedMember == null){
                    println("로그인 후 이용해주세요")
                    continue
                }
                val id = rq.getIntParam("id", 0)

                if (id == 0) {
                    println("id를 입력해주세요.")
                    continue
                }

                val article = articleRepository.getArticleById(id)

                if (article == null) {
                    println("${id}번 게시물은 존재하지 않습니다.")
                    continue
                }

                println("번호 : ${article.id}")
                println("작성날짜 : ${article.regDate}")
                println("갱신날짜 : ${article.updateDate}")
                println("제목 : ${article.title}")
                println("내용 : ${article.body}")
            }
            "/article/modify" -> {
                if(loginedMember == null){
                    println("로그인 후 이용해주세요")
                    continue
                }
                val id = rq.getIntParam("id", 0)

                if (id == 0) {
                    println("id를 입력해주세요.")
                    continue
                }

                val article = articleRepository.getArticleById(id)

                if (article == null) {
                    println("${id}번 게시물은 존재하지 않습니다.")
                    continue
                }

                print("${id}번 게시물 새 제목 : ")
                val title = readLineTrim()
                print("${id}번 게시물 새 내용 : ")
                val body = readLineTrim()

                articleRepository.modifyArticle(id, title, body)

                println("${id}번 게시물이 수정되었습니다.")
            }
            "/article/delete" -> {
                if(loginedMember == null){
                    println("로그인 후 이용해주세요")
                    continue
                }

                val id = rq.getIntParam("id", 0)

                if (id == 0) {
                    println("id를 입력해주세요.")
                    continue
                }

                val article = articleRepository.getArticleById(id)

                if (article == null) {
                    println("${id}번 게시물은 존재하지 않습니다.")
                    continue
                }


                articleRepository.deleteArticle(article)
                println("${id}번 게시물 삭제 완료")
            }
        }
    }

    println("== SIMPLE SSG 끝 ==")
}

// Rq는 UserRequest의 줄임말이다.
// Request 라고 하지 않은 이유는, 이미 선점되어 있는 클래스명 이기 때문이다.
class Rq(command: String) {
    // 데이터 예시
    // 전체 URL : /artile/detail?id=1
    // actionPath : /artile/detail
    val actionPath: String

    // 데이터 예시
    // 전체 URL : /artile/detail?id=1&title=안녕
    // paramMap : {id:"1", title:"안녕"}
    private val paramMap: Map<String, String>

    // 객체 생성시 들어온 command 를 ?를 기준으로 나눈 후 추가 연산을 통해 actionPath와 paramMap의 초기화한다.
    // init은 객체 생성시 자동으로 딱 1번 실행된다.
    init {
        // ?를 기준으로 둘로 나눈다.
        val commandBits = command.split("?", limit = 2)

        // 앞부분은 actionPath
        actionPath = commandBits[0].trim()

        // 뒷부분이 있다면
        val queryStr = if (commandBits.lastIndex == 1 && commandBits[1].isNotEmpty()) {
            commandBits[1].trim()
        } else {
            ""
        }

        paramMap = if (queryStr.isEmpty()) {
            mapOf()
        } else {
            val paramMapTemp = mutableMapOf<String, String>()

            val queryStrBits = queryStr.split("&")

            for (queryStrBit in queryStrBits) {
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

    fun getStringParam(name: String, default: String): String {
        return paramMap[name] ?: default
    }

    fun getIntParam(name: String, default: Int): Int {
        return if (paramMap[name] != null) {
            try {
                paramMap[name]!!.toInt()
            } catch (e: NumberFormatException) {
                default
            }
        } else {
            default
        }
    }
}

// 회원 관련 시작

data class Member(
    val memberId : Int,
    val regDate : String,
    val loginId : String,
    val loginPw : String,
    val name : String,
    val nickName : String,
){

}


object memberRepository {
    val members = mutableListOf<Member>()
    var lastMember = 0

    fun joinMember(loginId : String, loginPw : String, name : String, nickName : String) : Int{
        val memberId = ++lastMember
        val regDate = getNowDateStr()
        members.add(Member(memberId, regDate, loginId, loginPw, name, nickName))
        return memberId
    }

    fun makeTestMember(){
        for(i in 1..20){
            val loginId = "user$i"
            val loginPw = "$i"
            val name = "이름$i"
            val nickName = "별명$i"
            joinMember(loginId, loginPw, name, nickName)
        }
    }

    fun isJoinableId(loginId : String) : Boolean{
        val member = getMemberByLoginId(loginId)
        return member == null
    }

    fun getMemberByLoginId(loginId : String) : Member?{
        for(member in members){
            if(member.loginId == loginId){
                return member
            }
        }
        return null
    }

}
// 회원 관련 끝

// 게시물 관련 시작
data class Article(
    val id: Int,
    val regDate: String,
    var updateDate: String,
    var title: String,
    var body: String
)

object articleRepository {
    private val articles = mutableListOf<Article>()
    private var lastId = 0

    fun deleteArticle(article: Article) {
        articles.remove(article)
    }

    fun getArticleById(id: Int): Article? {
        for (article in articles) {
            if (article.id == id) {
                return article
            }
        }

        return null
    }

    fun addArticle(title: String, body: String): Int {
        val id = ++lastId
        val regDate = Util.getNowDateStr()
        val updateDate = Util.getNowDateStr()

        articles.add(Article(id, regDate, updateDate, title, body))

        return id
    }

    fun makeTestArticles() {
        for (id in 1..20) {
            val title = "제목_$id"
            val body = "내용_$id"
            addArticle(title, body)
        }
    }

    fun modifyArticle(id: Int, title: String, body: String) {
        val article = getArticleById(id)!!

        article.title = title
        article.body = body
        article.updateDate = Util.getNowDateStr()
    }

    fun getFilteredArticles(searchKeyword: String, page: Int, itemsCountInAPage: Int): List<Article> {
        val filtered1Articles = getSearchKeywordFilteredArticles(articles, searchKeyword)
        val filtered2Articles = getPageFilteredArticles(filtered1Articles, page, itemsCountInAPage)

        return filtered2Articles
    }

    private fun getSearchKeywordFilteredArticles(articles: List<Article>, searchKeyword: String): List<Article> {
        val filteredArticles = mutableListOf<Article>()

        for (article in articles) {
            if (article.title.contains(searchKeyword)) {
                filteredArticles.add(article)
            }
        }

        return filteredArticles
    }

    private fun getPageFilteredArticles(articles: List<Article>, page: Int, itemsCountInAPage: Int): List<Article> {
        val filteredArticles = mutableListOf<Article>()

        val offsetCount = (page - 1) * itemsCountInAPage

        val startIndex = articles.lastIndex - offsetCount
        var endIndex = startIndex - (itemsCountInAPage - 1)

        if (endIndex < 0) {
            endIndex = 0
        }

        for (i in startIndex downTo endIndex) {
            filteredArticles.add(articles[i])
        }

        return filteredArticles
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