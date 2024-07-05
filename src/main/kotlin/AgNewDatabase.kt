import org.ktorm.database.Database
import org.ktorm.dsl.*
import org.ktorm.entity.add
import org.ktorm.entity.find
import org.ktorm.entity.sequenceOf
import java.time.LocalDateTime
import java.util.*

val Database.agMachinesNew get() = this.sequenceOf(AgMachinesNew)
val Database.agKeys get() = this.sequenceOf(AgKeys)

val agMachines = AgMachinesNew.aliased("machines")
val agKeys = AgKeys.aliased("agKeys")

val validateTypeMap = mapOf(
    "自动识别" to "apex_recoils",
    "ai" to "ai",
    "升级脚本" to "auto_upgrade_script",
)

// 返回体验卡，判断是否有绑定卡，卡是否过期。若原卡存在，则返回原卡。不存在则新增卡
fun createExperienceCardByQQ(qqStr: String, validateTypeStrOri: String): String {
    if (!validateTypeMap.keys.contains(validateTypeStrOri)) {
        val keysAsString = validateTypeMap.keys.joinToString(
            prefix = "[",
            postfix = "]",
            separator = ","
        )
        return "只支持申请:$keysAsString"
    }
    val validateTypeStr = validateTypeMap[validateTypeStrOri]!!
    val experienceKey: AgKey? =
        database.agKeys.find { (it.qq eq qqStr) and (it.validate_type eq validateTypeStr) and (it.key_type eq 0) }

    if (experienceKey != null) {
        if (experienceKey.used == 1) {
            experienceKey.used = 0
            val agMachineNew = database.agMachinesNew.find { (AgMachinesNew.val_key eq experienceKey.valKey) }
            if (agMachineNew != null) {
                println("从${agMachineNew.machineCode}中解绑${experienceKey.valKey}")
                agMachineNew.valKey = null
                agMachineNew.flushChanges()
            }
            println("重置${experienceKey.valKey}")
            experienceKey.flushChanges()
        }
        return experienceKey.valKey
    } else {
        val uuid: String = UUID.randomUUID().toString()
        database.agKeys.add(AgKey {
            valKey = uuid
            qq = qqStr
            expirationTime = null
            validateType = validateTypeStr
            used = 0
            keyType = 0
        })
        return uuid
    }
}

fun validate(machine: String, validateTypeStr: String): AgMachinesKeys? {
    val authList: List<AgMachinesKeys> = getAuthList("machine", machine).filter { it.validateType == validateTypeStr }
    println(authList)
    if (authList.size == 1) {
        val agMachinesKeys = authList[0]
        if (GroupMemberCache.memberInAnyGroup(agMachinesKeys.qq!!.toLong())) {
            return authList[0]
        }
    }
    return null
}

fun getAuthList(type: String, condition: String): List<AgMachinesKeys> {
    val currentTime = LocalDateTime.now()
    val conditionOn = if (type == "qq") {
        agKeys.qq eq condition
    } else {
        agMachines.machine_code eq condition
    }
    return database.from(agMachines).innerJoin(agKeys, on = agMachines.val_key eq agKeys.val_key).select(
        agMachines.id,
        agMachines.val_key,
        agMachines.machine_code,
        agKeys.qq,
        agKeys.expiration_time,
        agKeys.validate_type,
        agKeys.used,
        agKeys.key_type
    ).where {
        conditionOn and
                ((agKeys.key_type eq 5) or (agKeys.expiration_time gte currentTime) or agKeys.expiration_time.isNull())
    }.map { rowSet ->
        AgMachinesKeys(
            rowSet[agMachines.id],
            rowSet[agMachines.val_key],
            rowSet[agMachines.machine_code],
            rowSet[agKeys.qq],
            rowSet[agKeys.expiration_time],
            rowSet[agKeys.validate_type],
            rowSet[agKeys.used],
            rowSet[agKeys.key_type],
        )
    }
}

fun getAuthStr(type: String, condition: String): String {
    val authList: List<AgMachinesKeys> = getAuthList(type, condition)
    if (authList.isEmpty()) {
        return "没有与你相关的授权信息"
    }
    val strBuild: StringBuilder = StringBuilder()
    for (agMachine in authList) {
        strBuild.append("[授权类型：${agMachine.validateType}，")
            .append("授权时效：${if (agMachine.expirationTime != null) agMachine.expirationTime else "永久授权"}，")
            .append("机器码：${agMachine.machineCode}]").append("\n")
    }
    return strBuild.toString()
}


fun bind(machine: String, keys: String): String {
    val agKey = database.agKeys.find { AgKeys.val_key eq keys }
    if (agKey == null) {
        return "key不可用"
    }
    if (agKey.used == 1) {
        return "key已被使用过"
    }
    val validate: AgMachinesKeys? = validate(machine, agKey.validateType)
    if (validate != null) {
        if (validate.keyType != 0) {
            return "当前绑定的${agKey.validateType}类型的key并未过期，暂不可再次绑定"
        }
    }

    val agMachineNew = database.agMachinesNew.find { AgMachinesNew.machine_code eq machine }
    if (agMachineNew == null) {
        database.agMachinesNew.add(AgMachineNew {
            valKey = keys
            machineCode = machine
        })
    } else {
        agMachineNew.valKey = keys
        agMachineNew.flushChanges()
    }
    agKey.used = 1
    agKey.expirationTime = calculateNewExpirationTime(agKey.keyType, LocalDateTime.now())
    agKey.flushChanges()
    return "绑定成功，有效期到${agKey.expirationTime ?: "永久"}"
}
