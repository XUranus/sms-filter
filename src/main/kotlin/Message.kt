import org.ansj.splitWord.analysis.NlpAnalysis
import java.util.*
import java.util.stream.Collector
import java.util.stream.Collectors

class Message (val content:String?, val date: Date?, val number: String?) {
    override fun toString(): String {
        return "content=$content, date=$date, number=$number"
    }

    fun extractTransactionInfo(): TransactionInfo? {
        val transactionInfo = TransactionInfo()
        if(number.isNullOrBlank() && content.isNullOrBlank()) {
            return null
        }

        if(date != null) {
            transactionInfo.date = date
        }


        // resolve bank name from text content
        if(!content.isNullOrEmpty()) {
            arrayOf("(?<=[).*(?=])", "(?<=【).*(?=】)").forEach { pattern ->
                Regex(pattern).findAll(content).forEach {
                    val bank = BankLoader.findBankByBankName(it.value, true)
                    if(bank != null) {
                        transactionInfo.bankName = bank.name
                    }
                }
            }
        }

        // resolve bank name from number
        if(!number.isNullOrBlank()) {
            val bank = BankLoader.findBankByNumber(number)
            if (bank != null) {
                transactionInfo.bankName = bank.name
            }
        }

        // resolve direction
        if(!content.isNullOrEmpty()) {
            arrayOf("收入.*", "收到.*", "存入.*", "汇入.*", "收到转账.*").forEach { pattern ->
                Regex(pattern).findAll(content).forEach { _ ->
                    transactionInfo.direction = Direction.In
                }
            }
            arrayOf("支出.*", "取出.*", "消费.*", "汇出.*", "转出.*", "转给.*", "转到.*", "转账到.*", "转账至.*", "转账给.*","向.*转账").forEach { pattern ->
                Regex(pattern).findAll(content).forEach { _ ->
                    transactionInfo.direction = Direction.Out
                }
            }
        }


        // resolve partnerName
        if(!content.isNullOrEmpty()) {
            arrayOf(
                "(?<=转账至).*(?=人民币)",
                "(?<=向).*(?=进行了)",
                "(?<=支付宝快捷支付-).*",

                "(?<=由).*(?=汇入)",
                "(?<=摘要-).*",
                "(?<=摘要:).*",

                "(?<=-:).*(?=])",
                "(?<=-:).*(?=】)",
                "(?<=().*(?=))",
                "(?<=().*(?=))",

                "(?<=于:).*(?=向)",
                "(?<=于:).*(?=完成)",
                "微信零钱提现财付通",


            ).forEach { pattern ->
                Regex(pattern).findAll(content).forEach { it ->
                    transactionInfo.partnerName = it.value
                }
            }
            // use ansj
            if(transactionInfo.partnerName.isNullOrEmpty()) {
                val ansjResult = NlpAnalysis.parse(content)
                val name = ansjResult.terms.stream().filter { it.natureStr == "nr" }.findFirst()
                val corporationNames = ansjResult.terms.stream().filter{it.natureStr == "nt"}.collect(Collectors.toList())
                if(name.isPresent) {
                    var nameStr = name.get().name.replace("】", "");
                    if(nameStr.startsWith("向") && nameStr.length >= 3) {
                        nameStr = nameStr.replace("向","")
                        transactionInfo.partnerName = nameStr
                    }
                } else if(corporationNames.isNotEmpty()){
                    for(corporationName in corporationNames) {
                        if(!corporationName.name.contains("银行") && !corporationName.name.contains("贵公司")) {
                            var stb = StringBuffer()
                            stb.append(corporationName.name)
                            var temp = corporationName.from()
                            while(temp.natureStr == "n" || temp.natureStr == "an" || temp.natureStr == "ns") {
                                stb.insert(0, temp.name)
                                temp = temp.from()
                            }
                        }
                    }
                }
            }
        }

        // resolve bank card tail number
        if(!content.isNullOrEmpty()) {
            arrayOf(
                "(?<=尾号|账户|信用卡|尾号为|您)\\\\d{4}",
                "(?<=您)\\\\d{4}(?=账户)"
            ).forEach { pattern ->
                Regex(pattern).findAll(content).forEach {
                    transactionInfo.tailNumber = it.value
                }
            }
        }

        // resolve transactionValue
        if(!content.isNullOrEmpty()) {
            arrayOf(
                "(?<=收入|支出|美元|人民币|金额|存入|取出|转出|金额为|消费|汇入的)(-)?([0-9]{1,}(,[0-9]{3})*)(\\.[0-9]{1,2})?",
                "(-)?([0-9]{1,}(,[0-9]{3})*)(\\.[0-9]{1,2})?(?=元)"
            ).forEach { pattern ->
                Regex(pattern).findAll(content).forEach {
                    transactionInfo.transactionValue = it.value.toDoubleOrNull()
                }
            }
        }

        // resolve transactionCurrency
        if(!content.isNullOrEmpty()) {
            Currency.values().forEach { currency ->
                Regex(currency.toString()).findAll(content).forEach { _ ->
                    transactionInfo.transactionCurrency = currency
                }
            }
        }

        // TODO::resolve balanceValue
        transactionInfo.balanceCurrency = Currency.CNY

        // resolve balanceCurrency
        if(!content.isNullOrEmpty()) {
            arrayOf("(?<=余额)\\d").forEach { pattern ->
                Regex(pattern).findAll(content).forEach {
                    transactionInfo.balanceValue = it.value.toDoubleOrNull()
                }
            }
        }

        return transactionInfo
    }


}