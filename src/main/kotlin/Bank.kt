import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import java.io.File

@Serializable
class Bank(val name:String, val alias:Array<String>, val numbers: Array<String>) {
    override fun toString(): String {
        return "name=$name, alias=$alias, numbers=$numbers"
    }
}

object BankLoader {
    private val path: String = ClassLoader.getSystemResource("bank.json").path
    private val banks = Json.decodeFromString<Array<Bank>>(File(path).readText())

    fun findBankByNumber(number:String): Bank? {
        for(bank in banks) {
            for(bankNumber in bank.numbers) {
                if(bankNumber == number) {
                    return bank
                }
            }
        }
        return null
    }

    fun findBankByBankName(name:String, searchAlias:Boolean):Bank? {
        for(bank in banks) {
            if(bank.name == name){
                return bank
            }
            if(searchAlias) {
                for(bankName in bank.alias) {
                    if(bankName == name) {
                        return bank
                    }
                }
            }
        }
        return null
    }
}