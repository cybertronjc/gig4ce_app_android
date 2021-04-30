package com.gigforce.app.modules.wallet.models

import com.gigforce.app.core.base.basefirestore.BaseFirestoreDataModel
import java.util.*

data class Invoice(
    var agentName: String = "Agent",
    var gigId: String = "1234",
    var gigerId: String = "giger",
    var startDate: Date? = null,
    var endDate: Date? = null,
    var gigAmount: Int = 3500,
    var invoiceStatus: String = "pending",
    var date: Int = 0,
    var month: Int = 8,
    var year: Int = 0,
    var gigTiming: String = "10:00",
    var isInvoiceGenerated: Boolean = false,
    var invoiceGeneratedTime: String = ""

): BaseFirestoreDataModel(tableName = "invoices") {
}