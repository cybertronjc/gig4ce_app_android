package com.gigforce.app.modules.wallet.models

import com.gigforce.app.core.base.basefirestore.BaseFirestoreDataModel

data class Invoice(
    var agentName: String = "Agent",
    var gigId: String = "1234",
    var startDate: String = "DD-MM-YYYY",
    var endDate: String = "DD-MM-YYYY",
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