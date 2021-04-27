fun main(){

    println("simple ssg 시작")

    while(true){

        print("명령어 : ")
        val command = readLine()!!.trim()
        val commandBits = command.split("?", limit = 2)
        val url = commandBits[0]
        var paramStr = commandBits[1]
        val paramMap = mutableMapOf<String, String>()

        val paramStrBits = paramStr.split("&")

        for(paramStrBit in paramStrBits){
            val paramStrBitBits = paramStrBit.split("=", limit = 2)
            val key = paramStrBitBits[0]
            val value = paramStrBitBits[1]

            paramMap[key] = value
        }

        when(url) {
            "/system/exit" -> {
                println("프로그램 종료")
                break
            }
            "/article/detail" -> {
                val id = paramMap["id"]!!.toInt()


            }

        }

    }

    println("simple ssg 끝")

}