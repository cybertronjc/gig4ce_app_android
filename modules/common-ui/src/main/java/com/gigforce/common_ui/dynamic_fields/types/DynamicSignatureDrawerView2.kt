package com.gigforce.common_ui.dynamic_fields.types

import android.content.Context
import android.text.SpannedString
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.core.text.bold
import androidx.core.text.buildSpannedString
import androidx.fragment.app.FragmentManager
import com.gigforce.common_ui.R
import com.gigforce.common_ui.databinding.LayoutDynamicFieldSignatureView2Binding
import com.gigforce.common_ui.databinding.LayoutDynamicFieldSignatureViewBinding
import com.gigforce.common_ui.dynamic_fields.DynamicFieldView
import com.gigforce.common_ui.dynamic_fields.data.DataFromDynamicInputField
import com.gigforce.common_ui.dynamic_fields.data.DynamicField
import com.gigforce.common_ui.dynamic_fields.data.FieldTypes
import com.gigforce.common_ui.ext.addMandatorySymbolToTextEnd
import com.gigforce.common_ui.signature.FullScreenSignatureDialogFragment
import com.gigforce.common_ui.signature.FullScreenSignatureImageCatpureDialogFragment
import com.gigforce.core.extensions.gone
import com.gigforce.core.extensions.visible


class DynamicSignatureDrawerView2(
    context: Context,
    attrs: AttributeSet?
) : LinearLayout(
    context,
    attrs
), DynamicFieldView,
    FullScreenSignatureImageCatpureDialogFragment.FullScreenSignatureImageCaptureDialogFragmentListener {

    private var viewBinding: LayoutDynamicFieldSignatureView2Binding
    private lateinit var viewData: DynamicField
    private lateinit var fragmentManager : FragmentManager
    private var signatureImagePath : String? = null

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

    override fun bind(
        fieldDetails: DynamicField
    ) {
        viewData = fieldDetails
        tag = id //setting id of dynamic view as view tag to identify layout at runtime

        setTitle(fieldDetails.title)
        settingFieldAsOptionalOrMandatory(fieldDetails)
        setPrefillTextOrHint(
            fieldDetails.defaultSelectedDate,
            fieldDetails.prefillText,
            fieldDetails.title
        )
    }

    fun setFragmentManager(
        fragmentManager : FragmentManager
    ){
        this.fragmentManager = fragmentManager
    }

    private fun setPrefillTextOrHint(
        defaultSelectedDate : String?,
        prefillText: String?,
        title: String?
    ) {
        //ignored
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

    override fun isEnteredOrSelectedDataValid(): Boolean {
        return if(viewData.mandatory){
            signatureImagePath != null
        } else
            true
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

    override fun validateDataAndReturnDataElseNull(): DataFromDynamicInputField? {
        return if (isEnteredOrSelectedDataValid()) {
            removeError()
            getUserEnteredOrSelectedData()
        } else {
            checkDataAndSetError()
            null
        }
    }

    private fun getUserEnteredOrSelectedData(): DataFromDynamicInputField {
        return DataFromDynamicInputField(
            id = viewData.id,
            title = viewData.title,
            value = signatureImagePath,
            fieldType = FieldTypes.DATE_PICKER
        )
    }

    private fun checkDataAndSetError() {

        if (viewData.mandatory) {

            if (!isEnteredOrSelectedDataValid()) {

                setError(buildSpannedString {
                    bold {
                        append(
                            resources.getString(R.string.common_note_with_colon)
                        )
                    }
                    append(" Please select ${viewData.title}")
                })
            } else {
                removeError()
            }
        }
    }

    private fun setListenersOnView() = viewBinding.apply {

        this.signatureLayout.setOnClickListener {
            FullScreenSignatureImageCatpureDialogFragment.launch(fragmentManager,this@DynamicSignatureDrawerView2)
        }
    }

    override fun onSignatureImageCaptured(
        uploadedSignatureImageUrl: String,
        signaturePathInFirebase: String
    ) {

    }
}