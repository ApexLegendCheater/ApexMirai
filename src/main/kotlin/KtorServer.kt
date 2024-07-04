import io.ktor.client.engine.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import net.mamoe.mirai.Bot
import net.mamoe.mirai.contact.isAdministrator
import io.ktor.serialization.jackson.*

object KtorServer {
    @JvmStatic
    fun serverStart(bot: Bot?) {
        val server = embeddedServer(Netty, port = 14567) {
            install(ContentNegotiation) {
                jackson()
            }
            routing {
                get("/") {
                    call.respondText("Hello World!", ContentType.Text.Plain)
                }
                get("/groupMember") {
                    val groupId: Long = call.request.queryParameters["groupId"].toString().toLong()
                    val memberList: List<GroupMember>? = bot?.getGroup(groupId)?.members?.map {
                        GroupMember(
                            it.id, it.nameCard, it.isAdministrator()
                        )
                    }
                    call.respond(memberList ?: emptyList())
                }
                // 申请体验keys并绑定qq，同一类型不会重新生成
                get("/createExperienceCardByQQ") {
                    val qq: String = call.request.queryParameters["qq"].toString()
                    val validateType: String = call.request.queryParameters["validateType"].toString()
                    call.respondText { createExperienceCardByQQ(qq, validateType) }
                }
                // key绑定机器码，同一类型未过期时，不能重复绑定
                get("/machineBindKeys") {
                    val keys: String = call.request.queryParameters["key"].toString()
                    val machine: String = call.request.queryParameters["machineCode"].toString()
                    call.respondText { bind(machine, keys) }
                }
                // 校验机器码类型key是否过期
                get("/validate") {
                    val machineCode: String? = call.request.queryParameters["machineCode"]
                    if (machineCode == null) {
                        val result = mapOf("error" to "机器码未提供")
                        call.respond(HttpStatusCode(400, "机器码未提供"), result)
                    } else {
                        val validateType: String = call.request.queryParameters["validateType"].toString()
                        call.respond(
                            mapOf(
                                "machine_code" to machineCode,
                                "validateType" to validateType,
                                "validate" to (validate(machineCode, validateType) != null)
                            )
                        )
                    }
                }
            }
        }
        server.start(wait = false)
    }
}

fun main() {
    KtorServer.serverStart(null)
}