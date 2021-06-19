import java.util.*

class TransactionInfo {
    var bankName: String? = null
    var direction: Direction = Direction.Unknown
    var date: Date? = null
    var tailNumber: String? = null

    var partnerName: String? = null

    var transactionValue: Double? = null
    var transactionCurrency: Currency = Currency.Unknown

    var balanceValue: Double? = null
    var balanceCurrency: Currency = Currency.Unknown


    override fun toString(): String {
        return "bankName=$bankName, direction=$direction, date=$date, partnerName=$partnerName, " +
                "transactionValue=$transactionValue, transactionCurrency=$transactionCurrency, " +
                "balanceValue=$balanceValue, balanceCurrency=$balanceCurrency"
    }

}