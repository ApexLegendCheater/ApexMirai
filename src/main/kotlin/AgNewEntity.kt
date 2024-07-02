import org.ktorm.entity.Entity
import org.ktorm.schema.Table
import org.ktorm.schema.date
import org.ktorm.schema.int
import org.ktorm.schema.varchar
import java.time.LocalDate

interface AgKey : Entity<AgKey> {
    companion object : Entity.Factory<AgKey>()

    val id: Int

    var valKey: String

    var qq: String

    var expirationTime: LocalDate?

    var validateType: String

    var used: Int

    var keyType: Int

}

object AgKeys : Table<AgKey>("ag_keys") {
    val id = int("id").primaryKey().bindTo { it.id }
    var valKey = varchar("val_vey").bindTo { it.valKey }
    var qq = varchar("qq").bindTo { it.qq }
    var expiration_time = date("expiration_time").bindTo { it.expirationTime }
    var validate_type = varchar("validate_type").bindTo { it.validateType }
    var used = int("used").bindTo { it.used }
    var keyType = int("keyType").bindTo { it.keyType }
}

interface AgMachineNew : Entity<AgMachineNew> {
    companion object : Entity.Factory<AgMachineNew>()

    val id: Int
    var valKey: String
    var machineCode: String
}

object AgMachinesNew : Table<AgMachineNew>("ag_machines_new") {
    val id = int("id").primaryKey().bindTo { it.id }
    var val_key = varchar("val_key").bindTo { it.valKey }
    var machine_code = varchar("machine_code").bindTo { it.machineCode }
}
