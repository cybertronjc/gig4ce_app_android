package com.gigforce.common_ui.dynamic_fields.types

import android.content.Context
import android.os.Parcelable
import android.text.SpannedString
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.RadioButton
import androidx.core.text.bold
import androidx.core.text.buildSpannedString
import androidx.core.view.ViewCompat
import com.gigforce.common_ui.R
import com.gigforce.common_ui.databinding.LayoutDynamicFieldRadioButtonBinding
import com.gigforce.common_ui.dynamic_fields.DynamicFieldView
import com.gigforce.common_ui.dynamic_fields.data.DataFromDynamicInputField
import com.gigforce.common_ui.dynamic_fields.data.DynamicFieldData
import com.gigforce.common_ui.dynamic_fields.data.DynamicField
import com.gigforce.common_ui.dynamic_fields.data.FieldTypes
import com.gigforce.common_ui.ext.addMandatorySymbolToTextEnd
import com.gigforce.core.extensions.gone
import com.gigforce.core.extensions.visible
import kotlinx.android.parcel.Parcelize


class DynamicRadioButtonGroupView(
    context: Context,
    attrs: AttributeSet?
) : LinearLayout(
    context,
    attrs
), DynamicFieldView {
    private var viewBinding: LayoutDynamicFieldRadioButtonBinding
    private lateinit var viewData: DynamicField

    init {
        this.layoutParams =
            LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        viewBinding = LayoutDynamicFieldRadioButtonBinding.inflate(
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
        showOptionsInRadioButton(fieldDetails.data)
    }

    private fun showOptionsInRadioButton(
        dataToShow: List<DynamicFieldData>
    ) = viewBinding.radioGroup.apply {

        for (data in dataToShow){
            val radioButton = RadioButton(context)
            radioButton.id = ViewCompat.generateViewId()
            radioButton.tag = data.id
            radioButton.text = data.value
            addView(radioButton)
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

    override fun isEnteredOrSelectedDataValid(): Boolean {
        return if (viewData.mandatory) {
            viewBinding.radioGroup.checkedRadioButtonId != View.NO_ID
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
        val valueSelected = if (viewBinding.radioGroup.checkedRadioButtonId != View.NO_ID) {
            viewBinding.radioGroup.findViewById<RadioButton>(viewBinding.radioGroup.checkedRadioButtonId).text.toString()
        } else {
            null
        }

        val valueSelectedId = if (viewBinding.radioGroup.checkedRadioButtonId != View.NO_ID) {
            viewBinding.radioGroup.findViewById<RadioButton>(viewBinding.radioGroup.checkedRadioButtonId).tag.toString()
        } else {
            null
        }

        return DataFromDynamicInputField(
            id = viewData.id,
            title = viewData.title,
            valueId = valueSelectedId,
            value = valueSelected,
            fieldType = FieldTypes.RADIO_BUTTON
        )
    }

    private fun checkDataAndSetError() {

        if (viewData.mandatory) {

            if (viewBinding.radioGroup.checkedRadioButtonId == View.NO_ID) {

                setError(buildSpannedString {
                    bold {
                        append(
                            resources.getString(R.string.common_note_with_colon)
                        )
                    }
                    append(" Please select a value for ${viewData.title}")
                })
            } else {

                removeError()
            }
        }
    }

    @Parcelize
    class StateSavingObject(
        val parcelable: Parcelable?,
        val editTextText: String
    ) : View.BaseSavedState(parcelable)

}