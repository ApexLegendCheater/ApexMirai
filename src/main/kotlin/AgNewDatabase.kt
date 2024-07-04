import org.ktorm.database.Database
import org.ktorm.dsl.*
import org.ktorm.entity.*
import java.time.LocalDate
import java.util.*

val Database.agMachinesNew get() = this.sequenceOf(AgMachinesNew)
val Database.agKeys get() = this.sequenceOf(AgKeys)

val agMachines = AgMachinesNew.aliased("machines")
val agKeys = AgKeys.aliased("agKeys")

// 返回体验卡，判断是否有绑定卡，卡是否过期。若原卡存在，则返回原卡。不存在则新增卡
fun createExperienceCardByQQ(qqStr: String, validateTypeStr: String): String {

    val experienceKey: AgKey? =
        database.agKeys.find { (it.qq eq qqStr) and (it.validate_type eq validateTypeStr) }

    if (experienceKey != null) {
        val expiredKey: Boolean = !(
                experienceKey.used == 0 || experienceKey.keyType == 0 || experienceKey.keyType == 5 ||
                        experienceKey.expirationTime!!.isBefore(LocalDate.now())
                )
        if (expiredKey) {
            experienceKey.keyType = 0
            experienceKey.expirationTime = null
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
    val currentTime = LocalDate.now()
    database.agMachines.filter { (it.machine_code eq machine) }
    val machineKeyList = database.from(agMachines).innerJoin(agKeys, on = agMachines.val_key eq agKeys.val_key).select(
        agMachines.id,
        agMachines.val_key,
        agMachines.machine_code,
        agKeys.qq,
        agKeys.expiration_time,
        agKeys.validate_type,
        agKeys.used,
        agKeys.key_type
    ).where {
        (agMachines.machine_code eq machine) and (agKeys.validate_type eq validateTypeStr) and
                ((agKeys.key_type inList mutableListOf(0, 5)) or (agKeys.expiration_time gte currentTime))
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
    println(machineKeyList)
    if (machineKeyList.size == 1) {
        return machineKeyList[0]
    } else {
        return null
    }
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
        } else {
            database.delete(AgKeys) { it.val_key eq validate.valKey!! }
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
    agKey.expirationTime = calculateNewExpirationTime(agKey.keyType, LocalDate.now())
    agKey.flushChanges()
    return "绑定成功，有效期到${agKey.expirationTime}"
}
