package com.gigforce.common_ui.dynamic_fields

import android.text.SpannedString
import com.gigforce.common_ui.R
import com.gigforce.common_ui.dynamic_fields.data.DataFromDynamicInputField
import com.gigforce.common_ui.dynamic_fields.data.DynamicField
import com.gigforce.common_ui.dynamic_fields.data.DynamicVerificationField
import com.gigforce.common_ui.dynamic_fields.data.VerificationStatus
import com.gigforce.common_ui.viewmodels.verification.SharedVerificationViewModelEvent
import com.gigforce.common_ui.views.GigforceImageView
import com.gigforce.core.datamodels.verification.VerificationUserSubmittedData

interface BaseDynamicFieldView {

    fun setError(
        error: SpannedString
    )

    fun removeError()
}

interface DynamicFieldView : BaseDynamicFieldView {

    fun bind(
        fieldDetails: DynamicField
    )

    fun isEnteredOrSelectedDataValid(): Boolean

    fun validateDataAndReturnDataElseNull(): DataFromDynamicInputField?
}

interface DynamicVerificationFieldView : BaseDynamicFieldView {

    val fieldType : String

    fun bind(
        fieldDetails: DynamicVerificationField
    )

    fun updateDocumentStatus(
        status : String
    )

    fun updateDocumentStatusImage(
        status : String,
        imageView: GigforceImageView
    ){
        when (status) {
            VerificationStatus.VERIFIED -> imageView.loadImage(R.drawable.ic_success_round_green)
            VerificationStatus.REJECTED -> imageView.loadImage(R.drawable.ic_rejected_round_red)
            VerificationStatus.UNDER_PROCESSING -> imageView.loadImage(R.drawable.ic_pending_round_yellow)
            else -> imageView.loadImage(R.drawable.ic_pending_round_yellow)
        }

    }
}