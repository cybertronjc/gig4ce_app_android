package com.gigforce.common_ui.dynamic_fields

import android.content.Context
import android.widget.LinearLayout
import com.gigforce.common_ui.dynamic_fields.data.DataFromDynamicInputField
import com.gigforce.common_ui.dynamic_fields.data.FieldTypes
import com.gigforce.common_ui.dynamic_fields.data.DynamicField
import com.gigforce.common_ui.dynamic_fields.types.DynamicDatePickerView
import com.gigforce.common_ui.dynamic_fields.types.DynamicDropDownView
import com.gigforce.common_ui.dynamic_fields.types.DynamicRadioButtonGroupView
import com.gigforce.common_ui.dynamic_fields.types.DynamicTextFieldView
import com.gigforce.core.logger.GigforceLogger
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DynamicFieldsInflaterHelper @Inject constructor(
    private val logger: GigforceLogger
) {

    companion object {
        const val TAG = "DynamicFieldsInflaterHelper"
    }

    fun inflateDynamicFields(
        context: Context,
        containerLayout: LinearLayout,
        fields: List<DynamicField>
    ) = fields.apply {
        containerLayout.removeAllViews()

        fields.forEach {

            compareFieldTypeAndInflateRequiredLayout(
                context,
                containerLayout,
                it
            )
        }
    }

    private fun compareFieldTypeAndInflateRequiredLayout(
        context: Context,
        containerLayout: LinearLayout,
        it: DynamicField
    ) {
        when (it.fieldType) {
            FieldTypes.TEXT_FIELD -> inflateTextField(context, containerLayout, it)
            FieldTypes.DATE_PICKER -> inflateDatePicker(context, containerLayout, it)
            FieldTypes.DROP_DOWN -> inflateDropDown(context, containerLayout, it)
            FieldTypes.RADIO_BUTTON -> inflateRadioButtons(context, containerLayout, it)
            else -> {
                logger.d(
                    TAG,
                    "skipping inflating ${it.id},${it.title} as it lacks fieldtype ${it.fieldType} doesnt match any present in app"
                )
            }
        }
    }

    private fun inflateRadioButtons(
        context: Context,
        containerLayout: LinearLayout,
        it: DynamicField
    ) {
        val view = DynamicRadioButtonGroupView(context, null)
        containerLayout.addView(view)
        view.bind(it)
    }

    private fun inflateDropDown(
        context: Context,
        containerLayout: LinearLayout,
        it: DynamicField
    ) {
        val view = DynamicDropDownView(context, null)
        containerLayout.addView(view)
        view.bind(it)
    }

    private fun inflateDatePicker(
        context: Context,
        containerLayout: LinearLayout,
        it: DynamicField
    ) {
        val view = DynamicDatePickerView(context, null)
        containerLayout.addView(view)
        view.bind(it)
    }

    private fun inflateTextField(
        context: Context,
        containerLayout: LinearLayout,
        it: DynamicField
    ) {
        val view = DynamicTextFieldView(context, null)
        containerLayout.addView(view)
        view.bind(it)
    }

    fun inflateDynamicField(
        context: Context,
        containerLayout: LinearLayout,
        field: DynamicField
    ) = compareFieldTypeAndInflateRequiredLayout(context, containerLayout, field)


    fun validateDynamicFieldsReturnFieldValueIfValid(
        container: LinearLayout
    ): List<DataFromDynamicInputField>? {

        val dynamicFieldsData = mutableListOf<DataFromDynamicInputField>()
        for (i in 0 until container.childCount) {

            val dynamicFieldView = container.getChildAt(i) as DynamicFieldView
            val dataFromField = dynamicFieldView.validateDataAndReturnDataElseNull() ?: return null
            dynamicFieldsData.add(dataFromField)
        }

        return dynamicFieldsData
    }


}