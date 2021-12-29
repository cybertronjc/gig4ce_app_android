package com.gigforce.common_ui.dynamic_fields.types

import android.content.Context
import android.text.SpannedString
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.fragment.app.FragmentManager
import com.gigforce.common_ui.R
import com.gigforce.common_ui.databinding.LayoutDynamicFieldSignatureView2Binding
import com.gigforce.common_ui.dynamic_fields.DynamicVerificationFieldView
import com.gigforce.common_ui.dynamic_fields.data.DataFromDynamicInputField
import com.gigforce.common_ui.dynamic_fields.data.DynamicVerificationField
import com.gigforce.common_ui.dynamic_fields.data.FieldTypes
import com.gigforce.common_ui.ext.addMandatorySymbolToTextEnd
import com.gigforce.common_ui.navigation.signature.SignatureNavigation
import com.gigforce.core.extensions.gone
import com.gigforce.core.extensions.visible
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class DynamicSignatureDrawerView2(
    context: Context,
    attrs: AttributeSet?
) : LinearLayout(
    context,
    attrs
), DynamicVerificationFieldView {


    @Inject lateinit var signatureNavigation : SignatureNavigation
    private var viewBinding: LayoutDynamicFieldSignatureView2Binding
    private lateinit var viewData: DynamicVerificationField

    init {
        this.layoutParams = LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )

        viewBinding = LayoutDynamicFieldSignatureView2Binding.inflate(
            LayoutInflater.from(context),
            this,
            true
        )

        setListenersOnView()
    }

    override val fieldType: String get() = FieldTypes.SIGNATURE_DRAWER_2

    override fun bind(
        fieldDetails: DynamicVerificationField
    ) {
        viewData = fieldDetails
        tag = id //setting id of dynamic view as view tag to identify layout at runtime

        setTitle(fieldDetails.title)
        settingFieldAsOptionalOrMandatory(fieldDetails)
        updateDocumentStatus(fieldDetails.status)
    }


    private fun setPrefillTextOrHint(
        prefillText: String?,
        title: String?
    ) {
       viewBinding.subtitleTextview.text = prefillText ?: "Tap to upload"
    }


    private fun setTitle(title: String?) {
        viewBinding.titleTextview.text = title
    }

    private fun settingFieldAsOptionalOrMandatory(
        fieldDetails: DynamicVerificationField
    ) {
        if (fieldDetails.mandatory) {
            viewBinding.optionalTextview.gone()
            viewBinding.titleTextview.addMandatorySymbolToTextEnd()
        } else {
            viewBinding.optionalTextview.visible()
        }
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



    fun signatureCapturedUpdateStatus(
        signaturePathOnFirebase : String,
        fullImageUrl : String
    ){
        viewBinding.statusImageview.loadImage(R.drawable.ic_success_round_green)
    }

    private fun setListenersOnView() = viewBinding.apply {

        this.signatureLayout.setOnClickListener {
            signatureNavigation.openCaptureSignatureFragment(
                viewData.userId
            )
        }
    }

    override fun updateDocumentStatus(status: String) {
        updateDocumentStatusImage(
            status,
            viewBinding.statusImageview,
            viewBinding.subtitleTextview,
            viewData.prefillText ?: "Upload"
        )
    }

}