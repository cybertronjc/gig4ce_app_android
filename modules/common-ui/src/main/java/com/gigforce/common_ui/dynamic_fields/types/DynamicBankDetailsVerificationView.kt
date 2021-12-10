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
import com.gigforce.common_ui.dynamic_fields.data.DynamicVerificationField
import com.gigforce.common_ui.dynamic_fields.data.FieldTypes
import com.gigforce.common_ui.ext.addMandatorySymbolToTextEnd
import com.gigforce.common_ui.navigation.JoiningVerificationFormsNavigation
import com.gigforce.common_ui.viewmodels.verification.SharedVerificationViewModelEvent
import com.gigforce.core.datamodels.verification.AadhaarDetailsDataModel
import com.gigforce.core.extensions.gone
import com.gigforce.core.extensions.visible
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.parcel.Parcelize
import javax.inject.Inject

@AndroidEntryPoint
class DynamicBankDetailsVerificationView(
    context: Context,
    attrs: AttributeSet?
) : LinearLayout(
    context,
    attrs
), DynamicVerificationFieldView,
View.OnClickListener{

    @Inject
    lateinit var joiningVerificationNavigation: JoiningVerificationFormsNavigation

    private var viewBinding: LayoutDynamicFieldVerificationViewBinding
    private lateinit var viewData: DynamicVerificationField
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
        viewBinding.root.setOnClickListener(this)
    }

    override val fieldType: String
        get() = FieldTypes.BANK_VERIFICATION_VIEW

    override fun bind(
        fieldDetails: DynamicVerificationField
    ) {
        viewData = fieldDetails
        tag = id //setting id of dynamic view as view tag to identify layout at runtime

        setTitle(fieldDetails.title)
        settingFieldAsOptionalOrMandatory(fieldDetails)
        setPrefillTextOrHint(fieldDetails.prefillText, fieldDetails.title)
        updateDocumentStatus(fieldDetails.status)
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

    private fun settingFieldAsOptionalOrMandatory(fieldDetails: DynamicVerificationField) {
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

    override fun updateDocumentStatus(status: String) {
        updateDocumentStatusImage(
            status,
            viewBinding.statusIv,
            viewBinding.verificationSubtitleLabel,
            viewData.prefillText ?: "Upload"
        )
    }

    @Parcelize
    class StateSavingObject(
        val parcelable: Parcelable?,
        val editTextText: String
    ) : View.BaseSavedState(parcelable)

    override fun onClick(v: View?) {
        joiningVerificationNavigation.openBankDetailsVerificationForJoiningFragment(
            viewData.userId
        )
    }
}