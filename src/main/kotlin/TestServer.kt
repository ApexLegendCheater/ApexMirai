import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable
import net.mamoe.mirai.Bot
import net.mamoe.mirai.contact.isAdministrator

@Serializable
data class GroupMember(val id: Long, val name: String, val isAdmin: Boolean)

fun serverStart(bot: Bot) {
    val server = embeddedServer(Netty, port = 8080) {
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
        }
    }
    server.start(wait = false)
}