import org.ansj.splitWord.analysis.NlpAnalysis
import java.util.stream.Collectors

object BankMessageFilter {

    fun resolveBankName(number:String?, content:String?): String? {
        // resolve bank name from number
        if(!number.isNullOrBlank()) {
            val bank = BankLoader.findBankByNumber(number)
            if (bank != null) {
                return bank.name
            }
        }

        // resolve bank name from text content
        if(!content.isNullOrEmpty()) {
            arrayOf("(?<=\\[).*(?=\\])", "(?<=【).*(?=】)").forEach { pattern ->
                Regex(pattern).findAll(content).forEach {
                    val bank = BankLoader.findBankByBankName(it.value, true)
                    if(bank != null) {
                        return bank.name
                    }
                }
            }
        }

        return null
    }


    fun resolveDirection(content: String?): Direction {
        if(content.isNullOrEmpty()) {
            return Direction.Unknown
        } else {
            if(arrayOf(
                    "收入", "收到", "存入", "汇入", "收到转账","转入","支付给您","入账"
                ).any { pattern ->
                    Regex(pattern).containsMatchIn(content)
                }) {
                return Direction.In
            }

            if(arrayOf(
                "支出", "取出", "消费", "汇出", "转出", "转给", "转到",
                "转账到", "转账至", "转账给", "向.*转账", "转出交易", "自动扣费","支取","扣年费","扣取年费","扣收年费","转支交易"
            ).any { pattern ->
                Regex(pattern).containsMatchIn(content)
            }) {
                return Direction.Out
            }

            return Direction.Unknown
        }
    }

    fun resolveTailNumber(content: String?): String? {
        if(content.isNullOrEmpty()) {
            return null
        } else {
            arrayOf(
                Regex("(?<=尾号|账户|信用卡|尾号为?)\\d"),
                Regex("(?<=您)\\d(?=账户)")
            ).forEach { pattern ->
                pattern.findAll(content).forEach {
                    return it.value
                }
            }
        }
        return null
    }

    fun resolveTransactionValue(content: String?):Double? {
        if(content.isNullOrEmpty()) {
            return null
        } else {
            arrayOf(
                Regex("(?<=(收入|入账|支出|金额|存入|交易|取出|转出|转入|金额为|消费|汇入的?|支取|扣除年费)(\\(\\S{0,100}\\))?(（\\S{0,100}）)?(RMB|美元|人民币|港币|澳门币)?)(-)?([0-9]+(,[0-9]{3})*)(\\.[0-9]{1,2})?"),
                Regex("(?<=转账至个人账户\\d{0,4}人民币).*(?=元)")
            ).forEach { pattern ->
                pattern.findAll(content).forEach {
                    return it.value.replace(",","").toDoubleOrNull()
                }
            }
        }
        return null
    }

    fun resolveTransactionCurrency(content: String?): Currency {
        if(!content.isNullOrEmpty()) {
            arrayOf(
                Regex("港币"),Regex("美元"), Regex("澳门币")
            ).forEach { pattern ->
                pattern.findAll(content).forEach {
                    when (it.value) {
                        "美元" -> return Currency.USD
                        "港币" -> return Currency.HKD
                        "澳门币" -> return Currency.MOP
                    }
                }
            }
        }
        return Currency.CNY
    }

    fun resolveBalanceValue(content: String?):Double? {
        if(!content.isNullOrEmpty()) {
            arrayOf(
                Regex("(?<=余额为?)(-)?([0-9]+(,[0-9]{3})*)(\\.[0-9]{1,2})?")
            ).forEach { pattern ->
                    pattern.findAll(content).forEach {
                    return it.value.replace(",","").toDoubleOrNull()
                }
            }
        }
        return null
    }

    //TODO::
    fun resolveBalanceCurrency(content: String?): Currency {
        if(!content.isNullOrEmpty()) {
            if(arrayOf(
                    Regex("(?<=余额为?)(人民币)?(-)?([0-9]+(,[0-9]{3})*)(\\.[0-9]{1,2})?元")).any { pattern -> pattern.containsMatchIn(content) }) {
                return Currency.CNY
            }
        }
        return Currency.CNY
    }

    fun resolvePartnerName(content:String?):String? {
        if(!content.isNullOrEmpty()) {
            arrayOf(
                Regex("(?<=摘要：).*(?=，)"),
                Regex("(?<=摘要：).*$"),
                Regex("(?<=向)国家金库淄博市临淄区支库(?=完成\\S{0,4}交易)"),
                Regex("(?<=\\()消费财付通-.*(?=\\))"),
                Regex("(?<=收入\\().*(?=\\))"),
                Regex("(?<=支出\\().*(?=\\))"),
                Regex("(?<=\\[转入).*(?=\\])"),
                Regex("(?<=\\[存管).*(?=\\])"),
                Regex("(?<=\\[转入).*(?=\\])"),
                Regex("(?<=\\[消费:).*(?=\\])"),
                Regex("(?<=\\[)网上.*(?=\\])"),

                Regex("(?<=\\().*财付通.*(?=\\))"),
                Regex("(?<=\\().*支付宝.*(?=\\))"),

                Regex("(?<=向).*(?=完成)"),
                Regex("(?<=由).*(?=汇入)"),
                Regex("(?<=转账至).*(?=人民币)"),
                Regex("(?<=向).*(?=进行了)"),

                Regex("微信零钱提现"),
                Regex("互联汇入"),
                Regex("互联汇出"),
                Regex("他行汇入"),
                Regex("提现商盟商务"),


                ).forEach { pattern ->
                pattern.findAll(content).forEach { it ->
                    return it.value
                }
            }

            // use ansj
            val ansjResult = NlpAnalysis.parse(content)
            val name = ansjResult.terms.stream().filter { it.natureStr == "nr" }.findFirst()
            val corporationNames = ansjResult.terms.stream().filter{it.natureStr == "nt"}.collect(Collectors.toList())
            if(name.isPresent) {
                var nameStr = name.get().name.replace("】", "");
                if(nameStr.startsWith("向") && nameStr.length >= 3) {
                    nameStr = nameStr.replace("向","")
                    return nameStr
                }
            } else if(corporationNames.isNotEmpty()){
                for(corporationName in corporationNames) {
                    if(!corporationName.name.contains("银行") && !corporationName.name.contains("贵公司")) {
                        val stb = StringBuffer()
                        stb.append(corporationName.name)
                        var temp = corporationName.from()
                        while(temp.natureStr == "n" || temp.natureStr == "an" || temp.natureStr == "ns") {
                            stb.insert(0, temp.name)
                            temp = temp.from()
                        }
                        return stb.toString()
                    }
                }
            }
        }

        return null
    }
}