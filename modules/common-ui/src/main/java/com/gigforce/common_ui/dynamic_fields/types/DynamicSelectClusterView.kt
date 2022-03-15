package com.gigforce.common_ui.dynamic_fields.types

import android.content.Context
import android.text.SpannedString
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.LinearLayout
import com.gigforce.common_ui.databinding.LayoutDynamicSelectClusterViewBinding
import com.gigforce.common_ui.dynamic_fields.DynamicFieldView
import com.gigforce.common_ui.dynamic_fields.data.DataFromDynamicInputField
import com.gigforce.common_ui.dynamic_fields.data.DynamicField
import com.gigforce.common_ui.dynamic_fields.data.FieldTypes

class DynamicSelectClusterView(
    context: Context,
    attrs: AttributeSet?
) : LinearLayout(
    context,
    attrs
), DynamicFieldView {
    private var viewBinding: LayoutDynamicSelectClusterViewBinding
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
        viewBinding = LayoutDynamicSelectClusterViewBinding.inflate(
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

    }

    override fun isEnteredOrSelectedDataValid(): Boolean {
        TODO("Not yet implemented")
    }

    override fun validateDataAndReturnDataElseNull(): DataFromDynamicInputField? {
        TODO("Not yet implemented")
    }
}