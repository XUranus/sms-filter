import org.junit.jupiter.api.Test
import kotlinx.serialization.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import java.io.File
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.util.*

class Test {

    @Test
    fun test() {
        val path = ClassLoader.getSystemResource("sms.json").path
        val list = Json.decodeFromString<Array<Map<String,String>>>(File(path).readText())
        for(item in list) {
            val message = Message(
                item.getOrDefault("content",""),
                SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(item.getOrDefault("date","0000-00-00 00:00:00")),
                item.getOrDefault("number","")
            )
            //println(message)

            println(message.extractTransactionInfo())
        }
    }
}