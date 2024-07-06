import org.ktorm.entity.Entity
import org.ktorm.schema.*
import java.time.LocalDateTime

interface AgKey : Entity<AgKey> {
    companion object : Entity.Factory<AgKey>()

    val id: Int
    var valKey: String
    var qq: String?
    var expirationTime: LocalDateTime?
    var validateType: String
    var used: Int
    var keyType: Int
    var lastValTime: LocalDateTime?
    var externalCard: Int
}

open class AgKeys(alias: String?) : Table<AgKey>("ag_keys", alias) {
    companion object : AgKeys(null)

    override fun aliased(alias: String) = AgKeys(alias)
    val id = int("id").primaryKey().bindTo { it.id }
    var val_key = varchar("val_key").bindTo { it.valKey }
    var qq = varchar("qq").bindTo { it.qq }
    var expiration_time = datetime("expiration_time").bindTo { it.expirationTime }
    var validate_type = varchar("validate_type").bindTo { it.validateType }
    var used = int("used").bindTo { it.used }
    var key_type = int("key_type").bindTo { it.keyType }
    var last_val_time = datetime("last_val_time").bindTo { it.lastValTime }
    var external_card = int("external_card").bindTo { it.externalCard }
}

interface AgMachineNew : Entity<AgMachineNew> {
    companion object : Entity.Factory<AgMachineNew>()

    val id: Int
    var valKey: String?
    var machineCode: String
}

open class AgMachinesNew(alias: String?) : Table<AgMachineNew>("ag_machines_new", alias) {
    companion object : AgMachinesNew(null)

    override fun aliased(alias: String) = AgMachinesNew(alias)
    val id = int("id").primaryKey().bindTo { it.id }
    var val_key = varchar("val_key").bindTo { it.valKey }
    var machine_code = varchar("machine_code").bindTo { it.machineCode }
}


data class AgMachinesKeys(
    var id: Int?,
    var valKey: String?,
    var machineCode: String?,
    var qq: String?,
    var expirationTime: LocalDateTime?,
    var validateType: String?,
    var used: Int?,
    var keyType: Int?,
    var lastValTime: LocalDateTime?,
    var externalCard: Int
)
