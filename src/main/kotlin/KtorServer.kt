import io.ktor.client.engine.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import net.mamoe.mirai.Bot
import net.mamoe.mirai.contact.isAdministrator


object KtorServer {
    @JvmStatic
    fun serverStart(bot: Bot) {
        val server = embeddedServer(Netty, port = 14567) {
            install(ContentNegotiation) {
                json()
            }
            routing {
                get("/") {
                    call.respondText("Hello World!", ContentType.Text.Plain)
                }
                get("/groupMember") {
                    val groupId: Long = call.request.queryParameters["groupId"].toString().toLong()
                    val memberList: List<GroupMember>? = bot.getGroup(groupId)?.members?.map {
                        GroupMember(
                            it.id,
                            it.nameCard,
                            it.isAdministrator()
                        )
                    }
                    call.respond(memberList ?: emptyList())
                }
                get("/createExperienceCardByQQ") {
                    val qq: String = call.request.queryParameters["qq"].toString()
                    val validateType: String = call.request.queryParameters["validateType"].toString()
                    createExperienceCardByQQ(qq, validateType)
                }
                post("/validate") {
                    val machineCode: String? = call.request.queryParameters["machine_code"]
                    if (machineCode == null) {
                        val result = mapOf("error" to "机器码未提供")
                        call.respond(HttpStatusCode(400, "机器码未提供"), result)
                    } else {
                        val validateType: String = call.request.queryParameters["validateType"].toString()

                    }


                }
            }
        }
        server.start(wait = false)
    }
}
