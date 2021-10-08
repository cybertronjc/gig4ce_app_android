package com.gigforce.lead_management.common_views

import android.content.Context
import android.os.Parcelable
import android.text.InputFilter
import android.text.InputType
import android.text.SpannedString
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.core.text.bold
import androidx.core.text.buildSpannedString
import com.gigforce.common_ui.ext.addMandatorySymbolToTextEnd
import com.gigforce.common_ui.viewdatamodels.leadManagement.DataFromDynamicInputField
import com.gigforce.common_ui.viewdatamodels.leadManagement.JobProfileDependentDynamicInputField
import com.gigforce.core.extensions.*
import com.gigforce.lead_management.R
import com.gigforce.lead_management.databinding.LayoutDynamicFieldBinding
import kotlinx.android.parcel.Parcelize

class JobProfileRelatedDynamicFieldView(
    context: Context,
    attrs: AttributeSet?
) : LinearLayout(
    context,
    attrs
) {
    private var viewBinding: LayoutDynamicFieldBinding
    private lateinit var viewData: JobProfileDependentDynamicInputField
    private var editTextString: String = ""

    init {
        this.layoutParams =
            LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        viewBinding = LayoutDynamicFieldBinding.inflate(
            LayoutInflater.from(context),
            this,
            true
        )

        viewBinding.editText.onTextChanged {
            editTextString = it
        }
    }

    fun bind(
        fieldDetails: JobProfileDependentDynamicInputField
    ) {
        viewData = fieldDetails
        tag = id //setting id of dynamic view as view tag to identify layout at runtime

        setTitle(fieldDetails.title)
        settingFieldAsOptionalOrMandatory(fieldDetails)
        setInputType(fieldDetails.inputType)
        setMaxLengthInEdittext(fieldDetails.maxLength)
        setPrefillTextOrHint(fieldDetails.prefillText, fieldDetails.title)
    }

    private fun setPrefillTextOrHint(
        prefillText: String?,
        title: String?
    ) {
        if (prefillText != null) {
            viewBinding.editText.setText(prefillText)
        } else {
            val hint =
                "${resources.getString(R.string.type)} $title ${resources.getString(R.string.here)}"
            viewBinding.editText.setHint(hint)
        }
    }

    private fun setMaxLengthInEdittext(maxLength: Int) {
        if (maxLength > 0) {
            viewBinding.editText.filters = arrayOf<InputFilter>(InputFilter.LengthFilter(maxLength))
        }
    }

    private fun setTitle(title: String?) {
        viewBinding.titleTextview.text = title
    }

    private fun settingFieldAsOptionalOrMandatory(fieldDetails: JobProfileDependentDynamicInputField) {
        if (fieldDetails.mandatory) {
            viewBinding.optionalTextview.gone()
            viewBinding.titleTextview.addMandatorySymbolToTextEnd()
        } else {
            viewBinding.optionalTextview.visible()
        }
    }

    private fun setInputType(inputType: String?) {
        viewBinding.editText.inputType = when (inputType) {
            JobProfileDependentDynamicInputField.INPUT_TYPE_NUMBER -> InputType.TYPE_CLASS_NUMBER
            JobProfileDependentDynamicInputField.INPUT_TYPE_NUMBER_WITH_DECIMAL -> InputType.TYPE_NUMBER_FLAG_DECIMAL
            else -> InputType.TYPE_CLASS_TEXT
        }
    }

    private fun setError(
        error: SpannedString
    ) {
        viewBinding.errorLayout.root.visible()
        viewBinding.errorLayout.errorTextview.text = error
    }

    private fun removeError() {
        viewBinding.errorLayout.errorTextview.text = null
        viewBinding.errorLayout.root.gone()
    }

    fun validateDataReturnIfValid(): DataFromDynamicInputField? {
        val valueFilled = viewBinding.editText.getTextIfNotBlankElseNull()

        if (viewData.mandatory) {

            val isInputTypeNumber =
                viewData.inputType == JobProfileDependentDynamicInputField.INPUT_TYPE_NUMBER_WITH_DECIMAL ||
                        viewData.inputType == JobProfileDependentDynamicInputField.INPUT_TYPE_NUMBER

            if (isInputTypeNumber) {
                val valueInFloat = valueFilled?.toFloatOrNull()
                if (valueInFloat == null || valueInFloat <= 0.0f) {

                    setError(buildSpannedString {
                        bold { append(
                            resources.getString(R.string.note_with_colon)
                        )}
                        append(" Please fill a non zero value for ${viewData.title}")
                    })
                    return null
                }
            } else {
                if (valueFilled.isNullOrBlank()) {

                    setError(buildSpannedString {
                        bold { append(
                            resources.getString(R.string.note_with_colon)
                        )}
                        append(" Please fill a value for ${viewData.title}")
                    })
                    return null
                }
            }
        }

        removeError()
        return DataFromDynamicInputField(
            id = viewData.id,
            title = viewData.title,
            value = valueFilled
        )
    }

    fun getFilledData(): DataFromDynamicInputField {
        val valueFilled = viewBinding.editText.text?.toString()
        return DataFromDynamicInputField(
            id = viewData.id,
            title = viewData.title,
            value = valueFilled
        )
    }


    override fun onSaveInstanceState(): Parcelable? {
        val superState = super.onSaveInstanceState()
        return StateSavingObject(superState, editTextString)
    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        val myState = state as? StateSavingObject
        super.onRestoreInstanceState(myState?.superState ?: state)

        editTextString = myState?.editTextText ?: ""

        if (editTextString.isNotBlank())
            viewBinding.editText.setText(editTextString)
    }


    @Parcelize
    class StateSavingObject(
        val parcelable: Parcelable?,
        val editTextText: String
    ) : View.BaseSavedState(parcelable)

}