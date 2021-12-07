package com.gigforce.common_ui.dynamic_fields

import android.text.SpannedString
import android.widget.TextView
import com.gigforce.common_ui.R
import com.gigforce.common_ui.dynamic_fields.data.DataFromDynamicInputField
import com.gigforce.common_ui.dynamic_fields.data.DynamicField
import com.gigforce.common_ui.dynamic_fields.data.DynamicVerificationField
import com.gigforce.common_ui.dynamic_fields.data.VerificationStatus
import com.gigforce.common_ui.viewmodels.verification.SharedVerificationViewModelEvent
import com.gigforce.common_ui.views.GigforceImageView
import com.gigforce.core.datamodels.verification.VerificationUserSubmittedData

interface BaseDynamicFieldView {

    val fieldType : String

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



    fun bind(
        fieldDetails: DynamicVerificationField
    )

    fun updateDocumentStatus(
        status : String
    )

    fun updateDocumentStatusImage(
        status : String,
        imageView: GigforceImageView,
        statusTextView : TextView,
        defaultStatusString : String
    ){
        when (status) {
            VerificationStatus.VERIFIED -> {
                imageView.loadImage(R.drawable.ic_success_round_green)
                statusTextView.text = "Verified"
            }
            VerificationStatus.REJECTED -> {
                imageView.loadImage(R.drawable.ic_rejected_round_red)
                statusTextView.text = "Rejected"
            }
            VerificationStatus.UNDER_PROCESSING -> {
                imageView.loadImage(R.drawable.ic_pending_round_yellow)
                statusTextView.text = "Under Processing"
            }
            else -> {
                imageView.loadImage(R.drawable.ic_pending_round_yellow)
                statusTextView.text = if(defaultStatusString.isNotBlank()) defaultStatusString else "Upload"
            }
        }

    }
}