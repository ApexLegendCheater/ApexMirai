import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import io.ktor.http.*
import io.ktor.serialization.jackson.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import net.mamoe.mirai.Bot
import net.mamoe.mirai.contact.isAdministrator
import java.text.SimpleDateFormat
import java.time.LocalDateTime

object KtorServer {
    @JvmStatic
    fun serverStart(bot: Bot?) {
        val server = embeddedServer(Netty, port = 14567) {
            install(ContentNegotiation) {
                jackson {
                    // 注册 Kotlin 模块
                    registerKotlinModule()
                    // 注册 Java Time 模块
                    registerModule(JavaTimeModule())
                    // 禁用将日期写成时间戳
                    disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                    dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
                    // 创建并注册自定义模块
                    val customModule = SimpleModule().apply {
                        addSerializer(LocalDateTime::class.java, CustomLocalDateTimeSerializer())
                        addDeserializer(LocalDateTime::class.java, CustomLocalDateTimeDeserializer())
                    }
                    registerModule(customModule)
                }
            }
            routing {
                get("/") {
                    call.respondText("Hello World!", ContentType.Text.Plain)
                }
                get("/groupMember") {
                    val groupId: Long = call.request.queryParameters["groupId"].toString().toLong()
                    val memberList: List<GroupMember>? = bot?.getGroup(groupId)?.members?.map {
                        GroupMember(
                            it.id, it.nameCard, it.nick, it.isAdministrator()
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
                get("/createKeyExt") {
                    val createNumber: Int = call.request.queryParameters["createNumber"]!!.toInt()
                    val keyType: String = call.request.queryParameters["keyType"].toString()
                    val validateType: String = call.request.queryParameters["validateType"].toString()
                    call.respondText {
                        createKeysExt(
                            createNumber,
                            validateType,
                            keyType
                        ).joinToString(separator = "\n")
                    }
                }
                get("/createKey") {
                    val qq: String = call.request.queryParameters["qq"].toString()
                    val keyType: String = call.request.queryParameters["keyType"].toString()
                    val validateType: String = call.request.queryParameters["validateType"].toString()
                    call.respondText {
                        createKeys(
                            keyType,
                            validateType,
                            qq
                        )
                    }
                }
                // key绑定机器码，同一类型未过期时，不能重复绑定
                get("/machineBindKeys") {
                    val keys: String = call.request.queryParameters["key"].toString()
                    val machine: String = call.request.queryParameters["machineCode"].toString()
                    val bind: String = bind(machine, keys)
                    if (bind.startsWith("绑定成功")) {
                        call.respondText { bind }
                    } else {
                        call.respondText(status = HttpStatusCode.fromValue(500), provider = { bind })
                    }
                }
                // 校验机器码类型key是否过期
                get("/validate") {
                    val machineCode: String? = call.request.queryParameters["machineCode"]
                    if (machineCode == null) {
                        val result = mapOf("error" to "机器码未提供")
                        call.respond(HttpStatusCode(400, "机器码未提供"), result)
                    } else {
                        val validateType: String = call.request.queryParameters["validateType"].toString()
                        val validate: AgMachinesKeys? = validate(machineCode, validateType)
                        call.respond(
                            mapOf(
                                "machine_code" to machineCode,
                                "validate_type" to validateType,
                                "access_granted" to (validate != null),
                                "expiration_time" to validate?.expirationTime
                            )
                        )
                    }
                }
            }
        }
        server.start(wait = true)
    }
}

fun main() {
    KtorServer.serverStart(null)
}