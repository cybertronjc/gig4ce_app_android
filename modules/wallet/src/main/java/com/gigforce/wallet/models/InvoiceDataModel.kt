package com.gigforce.wallet.models

import java.util.*

data class InvoiceDataModel(
    var billedByPan: String = "",
    var billedToAddress: String = "",
    var billedToGST: String = "",
    var billedToPan: String = "",
    var dueAmount: Long = 0,
    var gigerBankAccount: String = "",
    var gigerBankIFSC: String = "",
    var invoiceDate: Date = Date(),
    var invoiceLink: String = "",
    var invoiceNo: String = "",
    var invoicedAmount: Long = 0,
    var items: List<InvoiceItem> = emptyList(),
    var userId: String = ""
) {
}

data class InvoiceItem(
    var amount: Long = 0,
    var item: String = "",
    var paymentCycle: String = ""
){}