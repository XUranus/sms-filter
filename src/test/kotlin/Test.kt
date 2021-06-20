import org.junit.jupiter.api.Test
import kotlinx.serialization.*
import kotlinx.serialization.json.Json
import java.io.File
import java.text.SimpleDateFormat

class Test {

    @Test
    fun testFromFile() {
        val path = ClassLoader.getSystemResource("sms.json").path
        val list = Json.decodeFromString<Array<Map<String,String>>>(File(path).readText())

        for(item in list) {
            val message = Message(
                item.getOrDefault("content",""),
                SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(item.getOrDefault("date","")),
                item.getOrDefault("number","")
            )
            val result = message.extractTransactionInfo()

            println(message)
            println(result)
        }
    }

    @Test
    fun testOne() {
        val message = Message(
            "【南京银行】您尾号3742的账号于6月6日23时12分收入114514.00元，余额19198.10元，摘要：付款商户:易方达基金公司",
            SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse("2021-06-06 23:12:49"),
            "106925499813895302"
        )
        println(message.extractTransactionInfo())
    }

}