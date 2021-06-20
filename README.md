## sms-filter
extract transaction info from bank SMS

```kotlin
val message = Message(
    "【南京银行】您尾号3742的账号于6月6日23时12分收入114514.00元，余额19198.10元，摘要：付款商户:易方达基金公司",
    SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse("2021-06-06 23:12:49"),
    "106925499813895302"
)
println(message.extractTransactionInfo())
```
Output:
```
bankName=南京银行, direction=In, date=Sun Jun 06 23:12:49 CST 2021, partnerName=付款商户:易方达基金公司, transactionValue=114514.0, transactionCurrency=人民币, balanceValue=19198.1, balanceCurrency=人民币
```
