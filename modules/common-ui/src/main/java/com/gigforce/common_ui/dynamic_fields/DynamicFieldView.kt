package com.gigforce.common_ui.dynamic_fields

import android.text.SpannedString
import com.gigforce.common_ui.dynamic_fields.data.DataFromDynamicInputField
import com.gigforce.common_ui.dynamic_fields.data.DynamicField
import com.gigforce.common_ui.viewmodels.verification.SharedVerificationViewModelEvent
import com.gigforce.core.datamodels.verification.VerificationUserSubmittedData

interface BaseDynamicFieldView {

    fun bind(
        fieldDetails: DynamicField
    )

    fun isEnteredOrSelectedDataValid(): Boolean

    fun setError(
        error: SpannedString
    )

    fun removeError()
}

interface DynamicFieldView : BaseDynamicFieldView {

    fun validateDataAndReturnDataElseNull(): DataFromDynamicInputField?
}

interface DynamicVerificationFieldView : BaseDynamicFieldView {

    fun handleVerificationResult(
        event: SharedVerificationViewModelEvent
    )

    fun validateDataAndReturnDataElseNull(): VerificationUserSubmittedData?
}