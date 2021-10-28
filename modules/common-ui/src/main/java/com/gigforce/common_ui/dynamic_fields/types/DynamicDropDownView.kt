package com.gigforce.common_ui.dynamic_fields.types

import android.content.Context
import android.os.Parcelable
import android.text.SpannedString
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.LinearLayout
import androidx.core.text.bold
import androidx.core.text.buildSpannedString
import com.gigforce.common_ui.R
import com.gigforce.common_ui.databinding.LayoutDynamicFieldDropDownBinding
import com.gigforce.common_ui.dynamic_fields.DynamicFieldView
import com.gigforce.common_ui.dynamic_fields.data.DataFromDynamicInputField
import com.gigforce.common_ui.dynamic_fields.data.DynamicFieldData
import com.gigforce.common_ui.dynamic_fields.data.DynamicField
import com.gigforce.common_ui.dynamic_fields.data.FieldTypes
import com.gigforce.common_ui.ext.addMandatorySymbolToTextEnd
import com.gigforce.core.extensions.gone
import com.gigforce.core.extensions.visible
import kotlinx.android.parcel.Parcelize


class DynamicDropDownView(
    context: Context,
    attrs: AttributeSet?
) : LinearLayout(
    context,
    attrs
), DynamicFieldView {
    private var viewBinding: LayoutDynamicFieldDropDownBinding
    private lateinit var viewData: DynamicField
    private var editTextString: String = ""

    init {
        this.layoutParams =
            LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        viewBinding = LayoutDynamicFieldDropDownBinding.inflate(
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
        setDataOnSpinner(
            fieldDetails.title,
            fieldDetails.dataToShow
        )
    }

    private fun setDataOnSpinner(
        title: String?,
        dataToShow: List<DynamicFieldData>
    ) = viewBinding.spinner.apply {

        val spinnerAdapter: ArrayAdapter<DynamicFieldData> = ArrayAdapter<DynamicFieldData>(
            context,
            android.R.layout.simple_spinner_dropdown_item,
            dataToShow.toMutableList().apply {
                add(
                    0, DynamicFieldData(
                        id = "",
                        value = "Select $title"
                    )
                )
            }
        )

        adapter = spinnerAdapter
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

//    override fun onSaveInstanceState(): Parcelable? {
//        val superState = super.onSaveInstanceState()
//        return StateSavingObject(superState, editTextString)
//    }
//
//    override fun onRestoreInstanceState(state: Parcelable?) {
//        val myState = state as? StateSavingObject
//        super.onRestoreInstanceState(myState?.superState ?: state)
//
//        editTextString = myState?.editTextText ?: ""
//
//        if (editTextString.isNotBlank())
//            viewBinding.editText.setText(editTextString)
//    }

    override fun isEnteredOrSelectedDataValid(): Boolean {

        return if (viewData.mandatory) {
            viewBinding.spinner.childCount != 0 && viewBinding.spinner.selectedItemPosition != 0
        } else {
            true
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

        val valueSelected = if(viewBinding.spinner.childCount != 0 && viewBinding.spinner.selectedItemPosition != 0){
            viewBinding.spinner.selectedItem as DynamicFieldData
        } else{
            DynamicFieldData()
        }

        return DataFromDynamicInputField(
            id = viewData.id,
            title = viewData.title,
            valueId = valueSelected.id,
            value = valueSelected.value,
            fieldType = FieldTypes.DROP_DOWN
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

    @Parcelize
    class StateSavingObject(
        val parcelable: Parcelable?,
        val editTextText: String
    ) : View.BaseSavedState(parcelable)
}