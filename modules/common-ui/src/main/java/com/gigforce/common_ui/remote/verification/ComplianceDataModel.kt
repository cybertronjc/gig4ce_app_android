package com.gigforce.common_ui.remote.verification

data class ComplianceDataModel(
    val complianceNumbers: List<ComplianceDocDetailsDM> = emptyList(),
    val complianceDocuments: List<ComplianceDocumentDetailDM> = emptyList()
)
