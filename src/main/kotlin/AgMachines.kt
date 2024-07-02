import org.ktorm.schema.Table
import org.ktorm.schema.date
import org.ktorm.schema.int
import org.ktorm.schema.varchar

object AgMachines : Table<AgMachine>("ag_machines") {
    val id = int("id").primaryKey().bindTo { it.id }
    var machine_code = varchar("machine_code").bindTo { it.machineCode }
    var access_granted = varchar("access_granted").bindTo { it.accessGranted }
    var expiration_time = date("expiration_time").bindTo { it.expirationTime }
    var qq = varchar("qq").bindTo { it.qq }
    var validate_type = varchar("validate_type").bindTo { it.validateType }

}
