package com.gigforce.common_ui.dynamic_fields.types

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
import com.gigforce.common_ui.R
import com.gigforce.common_ui.databinding.LayoutDynamicFieldTextFieldBinding
import com.gigforce.common_ui.dynamic_fields.DynamicFieldView
import com.gigforce.common_ui.dynamic_fields.data.DataFromDynamicInputField
import com.gigforce.common_ui.dynamic_fields.data.InputTypes
import com.gigforce.common_ui.dynamic_fields.data.DynamicField
import com.gigforce.common_ui.dynamic_fields.data.FieldTypes
import com.gigforce.common_ui.ext.addMandatorySymbolToTextEnd
import com.gigforce.core.extensions.*
import kotlinx.android.parcel.Parcelize


class DynamicTextFieldView(
    context: Context,
    attrs: AttributeSet?
) : LinearLayout(
    context,
    attrs
), DynamicFieldView {
    private var viewBinding: LayoutDynamicFieldTextFieldBinding
    private lateinit var viewData: DynamicField
    private var editTextString: String = ""

    init {
        this.layoutParams =
            LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        viewBinding = LayoutDynamicFieldTextFieldBinding.inflate(
            LayoutInflater.from(context),
            this,
            true
        )

        viewBinding.editText.onTextChanged {
            editTextString = it
        }
    }

    override fun bind(
        fieldDetails: DynamicField
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
                "${resources.getString(R.string.common_type)} $title ${resources.getString(R.string.common_here)}"
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

    private fun settingFieldAsOptionalOrMandatory(fieldDetails: DynamicField) {
        if (fieldDetails.mandatory) {
            viewBinding.optionalTextview.gone()
            viewBinding.titleTextview.addMandatorySymbolToTextEnd()
        } else {
            viewBinding.optionalTextview.visible()
        }
    }

    private fun setInputType(inputType: String?) {
        viewBinding.editText.inputType = when (inputType) {
            InputTypes.INPUT_TYPE_NUMBER -> InputType.TYPE_CLASS_NUMBER
            InputTypes.INPUT_TYPE_NUMBER_WITH_DECIMAL -> InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
            else -> InputType.TYPE_CLASS_TEXT
        }
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

    override fun isEnteredOrSelectedDataValid(): Boolean {
        val valueFilled = viewBinding.editText.getTextIfNotBlankElseNull()

        if (viewData.mandatory) {

            val isInputTypeNumber = viewData.inputType == InputTypes.INPUT_TYPE_NUMBER_WITH_DECIMAL
                    || viewData.inputType == InputTypes.INPUT_TYPE_NUMBER

            if (isInputTypeNumber) {
                val valueInFloat = valueFilled?.toFloatOrNull()
                if (valueInFloat == null || valueInFloat <= 0.0f) {
                    return false
                }
            } else {
                if (valueFilled.isNullOrBlank()) {
                    return false
                }
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

    override fun validateDataAndReturnDataElseNull(): DataFromDynamicInputField? {
        return if(isEnteredOrSelectedDataValid()){
            removeError()
            getUserEnteredOrSelectedData()
        } else{
            checkDataAndSetError()
            null
        }
    }

    private fun getUserEnteredOrSelectedData(): DataFromDynamicInputField {
        val valueFilled = viewBinding.editText.text?.toString()
        return DataFromDynamicInputField(
            id = viewData.id,
            title = viewData.title,
            value = valueFilled,
            fieldType = FieldTypes.TEXT_FIELD
        )
    }

    private fun checkDataAndSetError() {

        val valueFilled = viewBinding.editText.getTextIfNotBlankElseNull()

        if (viewData.mandatory) {

            val isInputTypeNumber =
                viewData.inputType == InputTypes.INPUT_TYPE_NUMBER_WITH_DECIMAL ||
                        viewData.inputType == InputTypes.INPUT_TYPE_NUMBER

            if (isInputTypeNumber) {
                val valueInFloat = valueFilled?.toFloatOrNull()
                if (valueInFloat == null || valueInFloat <= 0.0f) {

                    setError(buildSpannedString {
                        bold { append(
                            resources.getString(R.string.common_note_with_colon)
                        )}
                        append(" Please fill a non zero value for ${viewData.title}")
                    })
                }
            } else {
                if (valueFilled.isNullOrBlank()) {

                    setError(buildSpannedString {
                        bold { append(
                            resources.getString(R.string.common_note_with_colon)
                        )}
                        append(" Please fill a value for ${viewData.title}")
                    })
                }
            }
        }
    }

    @Parcelize
    class StateSavingObject(
        val parcelable: Parcelable?,
        val editTextText: String
    ) : View.BaseSavedState(parcelable)
}