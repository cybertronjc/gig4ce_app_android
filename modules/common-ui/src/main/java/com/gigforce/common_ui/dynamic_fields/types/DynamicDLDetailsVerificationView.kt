package com.gigforce.common_ui.dynamic_fields.types

import android.content.Context
import android.os.Parcelable
import android.text.SpannedString
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.core.text.bold
import androidx.core.text.buildSpannedString
import com.gigforce.common_ui.R
import com.gigforce.common_ui.databinding.LayoutDynamicFieldVerificationViewBinding
import com.gigforce.common_ui.dynamic_fields.DynamicVerificationFieldView
import com.gigforce.common_ui.dynamic_fields.data.DynamicField
import com.gigforce.common_ui.ext.addMandatorySymbolToTextEnd
import com.gigforce.common_ui.viewmodels.verification.SharedVerificationViewModelEvent
import com.gigforce.core.datamodels.verification.AadhaarDetailsDataModel
import com.gigforce.core.datamodels.verification.DrivingLicenseDetailsDataModel
import com.gigforce.core.extensions.gone
import com.gigforce.core.extensions.visible
import kotlinx.android.parcel.Parcelize

class DynamicDLDetailsVerificationView(
    context: Context,
    attrs: AttributeSet?
) : LinearLayout(
    context,
    attrs
), DynamicVerificationFieldView {

    private var viewBinding: LayoutDynamicFieldVerificationViewBinding
    private lateinit var viewData: DynamicField
    private var drivingLicenseDetails : DrivingLicenseDetailsDataModel? = null

    init {
        this.layoutParams =
            LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        viewBinding = LayoutDynamicFieldVerificationViewBinding.inflate(
            LayoutInflater.from(context),
            this,
            true
        )
    }

    override fun bind(
        fieldDetails: DynamicField
    ) {
        viewData = fieldDetails
        tag = id //setting id of dynamic view as view tag to identify layout at runtime

        setTitle(fieldDetails.title)
        settingFieldAsOptionalOrMandatory(fieldDetails)
        setPrefillTextOrHint(fieldDetails.prefillText, fieldDetails.title)
    }

    private fun setPrefillTextOrHint(
        prefillText: String?,
        title: String?
    ) {
        viewBinding.verificationSubtitleLabel.text = prefillText
    }

    private fun setTitle(title: String?) {
        viewBinding.titleTextview.text = title
    }

    private fun settingFieldAsOptionalOrMandatory(fieldDetails: DynamicField) {
        if (fieldDetails.mandatory) {
            viewBinding.optionalTextview.gone()
            viewBinding.titleTextview.addMandatorySymbolToTextEnd()
        } else {
            viewBinding.optionalTextview.visible()
        }
    }


    override fun onRestoreInstanceState(state: Parcelable?) {
        val myState = state as? StateSavingObject
        super.onRestoreInstanceState(myState?.superState ?: state)
    }

    override fun isEnteredOrSelectedDataValid(): Boolean {
        if (viewData.mandatory) {

            if (drivingLicenseDetails == null) {
                return false
            }
        }

        return true
    }


    override fun setError(
        error: SpannedString
    ) {
        viewBinding.errorLayout.root.visible()
        viewBinding.errorLayout.errorTextview.text = error
    }

    override fun removeError() {
        viewBinding.errorLayout.errorTextview.text = null
        viewBinding.errorLayout.root.gone()
    }

    override fun validateDataAndReturnDataElseNull(): DrivingLicenseDetailsDataModel? {
        return if (isEnteredOrSelectedDataValid()) {
            removeError()
            getUserEnteredOrSelectedData()
        } else {
            checkDataAndSetError()
            null
        }
    }

    private fun getUserEnteredOrSelectedData(): DrivingLicenseDetailsDataModel? {
        return drivingLicenseDetails
    }

    private fun checkDataAndSetError() {

        if (viewData.mandatory) {

            if (drivingLicenseDetails == null) {
                setError(buildSpannedString {
                    bold {
                        append(
                            resources.getString(R.string.common_note_with_colon)
                        )
                    }
                    append(" Please fill a non zero value for ${viewData.title}")
                })
            } else {
                removeError()
            }
        }
    }

    override fun handleVerificationResult(event: SharedVerificationViewModelEvent) {

        if (event is SharedVerificationViewModelEvent.DrivingLicenseInfoSubmitted) {
            drivingLicenseDetails = event.drivingLicenseDetails
            showDocumentStatusAsSubmitted()
        }
    }

    private fun showDocumentStatusAsSubmitted() {

    }

    @Parcelize
    class StateSavingObject(
        val parcelable: Parcelable?,
        val editTextText: String
    ) : View.BaseSavedState(parcelable)
}