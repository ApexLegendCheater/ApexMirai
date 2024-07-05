import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.SerializerProvider
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

// 自定义 LocalDateTime 序列化器
class CustomLocalDateTimeSerializer : JsonSerializer<LocalDateTime>() {
    override fun serialize(value: LocalDateTime, gen: JsonGenerator, serializers: SerializerProvider) {
        val formattedDateTime = value.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
        gen.writeString(formattedDateTime)
    }
}

// 自定义 LocalDateTime 反序列化器
class CustomLocalDateTimeDeserializer : JsonDeserializer<LocalDateTime>() {
    override fun deserialize(p: JsonParser, ctxt: DeserializationContext): LocalDateTime {
        val dateTimeString = p.text.replace(" ", "T")
        return LocalDateTime.parse(dateTimeString)
    }
}
