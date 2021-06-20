import org.ansj.splitWord.analysis.NlpAnalysis
import java.util.*
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

        transactionInfo.date = date

        // resolve bank name
        transactionInfo.bankName = BankMessageFilter.resolveBankName(number, content)
        // resolve direction
        transactionInfo.direction = BankMessageFilter.resolveDirection(content)


        // resolve partnerName
        transactionInfo.partnerName = BankMessageFilter.resolvePartnerName(content)

        // resolve bank card tail number
        transactionInfo.tailNumber = BankMessageFilter.resolveTailNumber(content)

        // resolve transaction value
        transactionInfo.transactionValue = BankMessageFilter.resolveTransactionValue(content)

        // resolve transactionCurrency
        transactionInfo.transactionCurrency = BankMessageFilter.resolveTransactionCurrency(content)

        // resolve balance currency
        transactionInfo.balanceCurrency = BankMessageFilter.resolveBalanceCurrency(content)

        // resolve balance value
        transactionInfo.balanceValue = BankMessageFilter.resolveBalanceValue(content)

        return transactionInfo
    }


}