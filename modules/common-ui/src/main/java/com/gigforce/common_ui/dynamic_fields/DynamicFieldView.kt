package com.gigforce.common_ui.dynamic_fields

import android.text.SpannedString
import com.gigforce.common_ui.dynamic_fields.data.DataFromDynamicInputField
import com.gigforce.common_ui.dynamic_fields.data.DynamicField

interface DynamicFieldView {

    fun bind(
        fieldDetails: DynamicField
    )

    fun isEnteredOrSelectedDataValid() : Boolean

    fun setError(
        error: SpannedString
    )

    fun removeError()

    fun validateDataAndReturnDataElseNull() : DataFromDynamicInputField?
}