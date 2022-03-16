package com.gigforce.common_ui.dynamic_fields.types

import android.content.Context
import android.text.SpannedString
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.LinearLayout
import com.gigforce.common_ui.databinding.LayoutDynamicInputSalaryComponentBinding
import com.gigforce.common_ui.dynamic_fields.DynamicFieldView
import com.gigforce.common_ui.dynamic_fields.data.DataFromDynamicInputField
import com.gigforce.common_ui.dynamic_fields.data.DynamicField
import com.gigforce.common_ui.dynamic_fields.data.FieldTypes
import com.gigforce.common_ui.ext.addMandatorySymbolToTextEnd
import com.gigforce.core.extensions.gone
import com.gigforce.core.extensions.visible

class DynamicInputSalaryComponentView(
    context: Context,
    attrs: AttributeSet?
) : LinearLayout(
    context,
    attrs
), DynamicFieldView {
    private var viewBinding: LayoutDynamicInputSalaryComponentBinding
    private lateinit var viewData: DynamicField
    private var editTextString: String = ""

    override val fieldType: String
        get() = FieldTypes.DROP_DOWN

    override fun setError(error: SpannedString) {
        TODO("Not yet implemented")
    }

    override fun removeError() {
        TODO("Not yet implemented")
    }

    init {
        this.layoutParams =
            LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        viewBinding = LayoutDynamicInputSalaryComponentBinding.inflate(
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
        TODO("Not yet implemented")
    }

    override fun validateDataAndReturnDataElseNull(): DataFromDynamicInputField? {
        TODO("Not yet implemented")
    }
}