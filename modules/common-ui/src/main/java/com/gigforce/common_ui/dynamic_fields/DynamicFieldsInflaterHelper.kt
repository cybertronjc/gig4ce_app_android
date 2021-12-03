package com.gigforce.common_ui.dynamic_fields

import android.content.Context
import android.widget.LinearLayout
import androidx.fragment.app.FragmentManager
import com.gigforce.common_ui.dynamic_fields.data.*
import com.gigforce.common_ui.dynamic_fields.types.*
import com.gigforce.common_ui.viewmodels.verification.SharedVerificationViewModelEvent
import com.gigforce.core.datamodels.verification.*
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
        fields: List<BaseDynamicField>,
        fragmentManger : FragmentManager
    ) = fields.apply {
        containerLayout.removeAllViews()

        fields.forEach {

            compareFieldTypeAndInflateRequiredLayout(
                context,
                containerLayout,
                it,
                fragmentManger
            )
        }
    }

    private fun compareFieldTypeAndInflateRequiredLayout(
        context: Context,
        containerLayout: LinearLayout,
        it: BaseDynamicField,
        fragmentManger : FragmentManager
    ) {
        when (it.fieldType) {
            FieldTypes.TEXT_FIELD -> inflateTextField(context, containerLayout, it as DynamicField)
            FieldTypes.DATE_PICKER -> inflateDatePicker(context, containerLayout,  it as DynamicField)
            FieldTypes.DROP_DOWN -> inflateDropDown(context, containerLayout, it as DynamicField)
            FieldTypes.RADIO_BUTTON -> inflateRadioButtons(context, containerLayout, it as DynamicField)
            FieldTypes.SIGNATURE_DRAWER -> inflateSignatureDrawer(context,containerLayout,it as DynamicField,fragmentManger)
            FieldTypes.SIGNATURE_DRAWER_2 -> inflateSignatureDrawer2(context,containerLayout,it as DynamicField,fragmentManger)
            FieldTypes.AADHAAR_VERIFICATION_VIEW -> inflateAadhaarVerificationView(context,containerLayout,it as DynamicVerificationField)
            FieldTypes.BANK_VERIFICATION_VIEW -> inflateBankDetailsField(context,containerLayout,it as DynamicVerificationField)
            FieldTypes.DL_VERIFICATION_VIEW -> inflateDLField(context,containerLayout,it as DynamicVerificationField)
            FieldTypes.PAN_VERIFICATION_VIEW -> inflatePANField(context,containerLayout,it as DynamicVerificationField)
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

    private fun inflateAadhaarVerificationView(
        context: Context,
        containerLayout: LinearLayout,
        it: DynamicVerificationField
    ) {
        val view = DynamicAadhaarVerificationView(context, null)
        containerLayout.addView(view)
        view.bind(it)
    }

    private fun inflateBankDetailsField(
        context: Context,
        containerLayout: LinearLayout,
        it: DynamicVerificationField
    ) {
        val view = DynamicBankDetailsVerificationView(context, null)
        containerLayout.addView(view)
        view.bind(it)
    }

    private fun inflateDLField(
        context: Context,
        containerLayout: LinearLayout,
        it: DynamicVerificationField
    ) {
        val view = DynamicDLDetailsVerificationView(context, null)
        containerLayout.addView(view)
        view.bind(it)
    }

    private fun inflatePANField(
        context: Context,
        containerLayout: LinearLayout,
        it: DynamicVerificationField
    ) {
        val view = DynamicPANDetailsVerificationView(context, null)
        containerLayout.addView(view)
        view.bind(it)
    }

    private fun inflateSignatureDrawer(
        context: Context,
        containerLayout: LinearLayout,
        it: DynamicField,
        fragmentManger : FragmentManager
    ) {
        val view = DynamicSignatureDrawerView(context, null)
        containerLayout.addView(view)
//        view.bind(it)
        view.setFragmentManager(fragmentManger)
    }

    private fun inflateSignatureDrawer2(
        context: Context,
        containerLayout: LinearLayout,
        it: DynamicField,
        fragmentManger : FragmentManager
    ) {
        val view = DynamicSignatureDrawerView2(context, null)
        containerLayout.addView(view)
        view.bind(it)
        view.setFragmentManager(fragmentManger)
    }

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

    fun handleVerificationSubmissionEvent(
       verificationRelatedDynamicFieldsContainer : LinearLayout,
       event : SharedVerificationViewModelEvent
    ){

        for (i in 0 until verificationRelatedDynamicFieldsContainer.childCount) {

            val dynamicFieldView = verificationRelatedDynamicFieldsContainer.getChildAt(i) as DynamicVerificationFieldView

            if (dynamicFieldView.fieldType == FieldTypes.AADHAAR_VERIFICATION_VIEW && event is SharedVerificationViewModelEvent.AadhaarCardInfoSubmitted) {
                dynamicFieldView.updateDocumentStatus(VerificationStatus.UNDER_PROCESSING)
            } else if (dynamicFieldView.fieldType == FieldTypes.BANK_VERIFICATION_VIEW && event is SharedVerificationViewModelEvent.BankDetailsInfoSubmitted) {
                dynamicFieldView.updateDocumentStatus(VerificationStatus.UNDER_PROCESSING)
            } else if (dynamicFieldView.fieldType == FieldTypes.DL_VERIFICATION_VIEW && event is SharedVerificationViewModelEvent.DrivingLicenseInfoSubmitted) {
                dynamicFieldView.updateDocumentStatus(VerificationStatus.UNDER_PROCESSING)
            } else if (dynamicFieldView.fieldType == FieldTypes.PAN_VERIFICATION_VIEW && event is SharedVerificationViewModelEvent.PanCardInfoSubmitted) {
                dynamicFieldView.updateDocumentStatus(VerificationStatus.UNDER_PROCESSING)
            }

        }
    }
}