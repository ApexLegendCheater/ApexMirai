import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.jayway.jsonpath.JsonPath
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.coroutines.runBlocking

val historyMessagesMap = mutableMapOf<String, JsonArray>()
val MAX_HISTORY_LENGTH: Int = (System.getenv("MAX_HISTORY_LENGTH") ?: "50").toInt()
val MAX_TOTAL_WORDS: Int = (System.getenv("MAX_TOTAL_WORDS") ?: "5000").toInt()
val BASE_URL: String = System.getenv("API_URL") ?: "https://apikeyplus.com/v1"
val API_KEY: String = System.getenv("API_KEY") ?: ""

fun aiMsg(group: String, msg: String): String = runBlocking {
    val client = HttpClient(CIO)
    // 构造新的消息
    val newMessage = JsonObject().apply {
        addProperty("role", "user")
        addProperty("content", msg)
    }

    // 获取或初始化对应分组的历史消息
    val historyMessages = historyMessagesMap.getOrPut(group) { JsonArray() }

    // 将新的消息添加到历史消息中
    historyMessages.add(newMessage)
    // 计算总字数
    var totalWords: Int = historyMessages.sumOf { it.asJsonObject["content"].asString.length }

    // 检查历史消息数量和总字数，并移除最旧的消息以保持长度和字数限制
    while (historyMessages.size() > MAX_HISTORY_LENGTH || totalWords > MAX_TOTAL_WORDS) {
        val removedMessage = historyMessages.remove(0).asJsonObject
        totalWords -= removedMessage["content"].asString.length
    }

    // 构造 JSON 请求体，包括历史消息
    val requestBody = JsonObject().apply {
        addProperty("model", "gpt-3.5-turbo")
        add("messages", historyMessages)
    }
    val data = Gson().toJson(requestBody)

    var retry = 0
    do {
        try {
            val response: HttpResponse = client.post("$BASE_URL/chat/completions") {
                contentType(ContentType.Application.Json)
                header("Authorization", "Bearer $API_KEY")
                setBody(data)
            }

            val responseBody = response.bodyAsText()
            println("Status Code: ${response.status.value}")
            println("JSON Response: $responseBody")

            // 解析 JSON 响应
            // 使用 JsonPath 解析 JSON 响应
            val messageContent = JsonPath.read<String>(responseBody, "$.choices[0].message.content")
            println("Message Content: $messageContent")

            return@runBlocking messageContent
        } catch (e: Exception) {
            println("Error: ${e.message}")
        } finally {
            client.close()
        }
        retry++
    } while (retry < 3)
    return@runBlocking ""
}