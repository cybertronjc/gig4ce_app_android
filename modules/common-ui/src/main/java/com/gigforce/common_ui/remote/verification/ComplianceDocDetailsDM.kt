package com.gigforce.common_ui.remote.verification

import com.gigforce.core.SimpleDVM
import com.gigforce.core.datamodels.CommonViewTypes

data class ComplianceDocDetailsDM(
    val type: String = "",
    val name: String = "",
    val value: String = "",
): SimpleDVM(
    CommonViewTypes.COMPLIANCE_CARD)
