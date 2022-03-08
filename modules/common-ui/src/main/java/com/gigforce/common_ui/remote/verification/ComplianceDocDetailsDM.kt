package com.gigforce.common_ui.remote.verification

import com.gigforce.core.SimpleDVM
import com.gigforce.core.datamodels.CommonViewTypes

data class ComplianceDocDetailsDM(
    val type: String = "",
    val name: String = "",
    val value: String = "",
): SimpleDVM(
    CommonViewTypes.COMPLIANCE_NUMBER_CARD)

data class ComplianceDocumentDetailDM(
    val type: String = "",
    val name: String = "",
    val jobProfile: String = "",
    val dateOfGeneration: String = "",
    val path: String = ""
): SimpleDVM(
    CommonViewTypes.COMPLIANCE_DOC_CARD
)