import org.ktorm.database.Database
import org.ktorm.dsl.and
import org.ktorm.dsl.eq
import org.ktorm.entity.*
import org.ktorm.schema.Table
import org.ktorm.schema.date
import org.ktorm.schema.int
import org.ktorm.schema.varchar
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException


interface AgMachine : Entity<AgMachine> {
    companion object : Entity.Factory<AgMachine>()

    val id: Int
    var machineCode: String
    var accessGranted: String
    var expirationTime: LocalDate?
    var qq: String
    var validateType: String
}

object AgMachines : Table<AgMachine>("ag_machines") {
    val id = int("id").primaryKey().bindTo { it.id }
    var machine_code = varchar("machine_code").bindTo { it.machineCode }
    var access_granted = varchar("access_granted").bindTo { it.accessGranted }
    var expiration_time = date("expiration_time").bindTo { it.expirationTime }
    var qq = varchar("qq").bindTo { it.qq }
    var validate_type = varchar("validate_type").bindTo { it.validateType }

}


val JDBC_URL: String = System.getenv("JDBC_URL") ?: System.getProperty("JDBC_URL") ?: error("JDBC_URL is not set")
val JDBC_USER: String = System.getenv("JDBC_USER") ?: System.getProperty("JDBC_USER") ?: error("JDBC_USER is not set")
val JDBC_PSW: String = System.getenv("JDBC_PSW") ?: System.getProperty("JDBC_PSW") ?: error("JDBC_USER is not set")
val Database.agMachines get() = this.sequenceOf(AgMachines)
val database = Database.connect(JDBC_URL, user = JDBC_USER, password = JDBC_PSW)

fun queryExpirationTime(type: String, condition: String): String {
    val testMachine: List<AgMachine> = if (type == "qq") {
        database.agMachines.filter { it.qq eq condition }.toList()
    } else {
        database.agMachines.filter { it.machine_code eq condition }.toList()
    }
    if (testMachine.isEmpty()) {
        return "没有与你相关的授权信息"
    }
    val strBuild: StringBuilder = StringBuilder()
    for (agMachine in testMachine) {
        strBuild.append("[授权类型：${agMachine.validateType}，")
            .append("授权时效：${if (agMachine.expirationTime != null) agMachine.expirationTime else "永久授权"}，")
            .append("机器码：${agMachine.machineCode}]").append("\n")
    }
    return strBuild.toString()
}

fun addAuth(typeString: String, machine: String, expirationStr: String, qqString: String): String {
    var type = typeString
    if (type == "脚本") {
        type = "auto_upgrade_script"
    } else if (type == "自动识别") {
        type = "apex_recoils"
    }

    val dateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd") // 定义日期格式
    var localDate: LocalDate? = null
    try {
        if (expirationStr != "永久") {
            localDate = LocalDate.parse(expirationStr, dateFormat)
        }
    } catch (e: DateTimeParseException) {
        return "Error parsing date: ${e.message}"
    }

    val agMachines: List<AgMachine> =
        database.agMachines.filter { it.machine_code eq machine }.filter { it.validate_type eq type }.toList()
    if (agMachines.isNotEmpty()) {
        for ((index, agMachine) in agMachines.withIndex()) {
            if (index == 0) {
                agMachine.expirationTime = localDate
                agMachine.qq = qqString
                agMachine.flushChanges()
            } else {
                agMachine.delete()
            }
        }
    } else {
        database.agMachines.add(AgMachine {
            machineCode = machine
            expirationTime = localDate
            qq = qqString
            validateType = type
            accessGranted = "1"
        })
    }
    return queryExpirationTime("machine_code", machine)
}
