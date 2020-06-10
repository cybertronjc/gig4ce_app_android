package com.gigforce.app.modules.wallet.models

import com.gigforce.app.core.base.basefirestore.BaseFirestoreDataModel

data class Invoice(
    var agent_name: String = "Agent",
    var gigId: Int = 1234,
    var startDate: String = "DD-MM-YYYY",
    var endDate: String = "DD-MM-YYYY",
    var gigAmount: Int = 3500,
    var invoiceStatus: String = "pending"
): BaseFirestoreDataModel(tableName = "invoices") {
}