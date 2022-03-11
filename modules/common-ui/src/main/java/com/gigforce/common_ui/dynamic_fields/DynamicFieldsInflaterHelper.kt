package com.gigforce.common_ui.dynamic_fields

import android.content.Context
import android.widget.LinearLayout
import com.gigforce.common_ui.dynamic_fields.data.*
import com.gigforce.common_ui.dynamic_fields.types.*
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

    fun inflateVerificationDynamicFields(
        context: Context,
        containerLayout: LinearLayout,
        fields: List<DynamicVerificationField>,
    ){
        containerLayout.removeAllViews()

        fields.forEach {

            compareVerificationFieldTypeAndInflateRequiredLayout(
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
            FieldTypes.TEXT_FIELD -> inflateTextField(
                context,
                containerLayout,
                it
            )
            FieldTypes.DATE_PICKER -> inflateDatePicker(
                context,
                containerLayout,
                it
            )
            FieldTypes.DROP_DOWN -> inflateMultiSelectDropDown(
                context,
                containerLayout,
                it
            )
            FieldTypes.RADIO_BUTTON -> inflateRadioButtons(
                context,
                containerLayout,
                it
            )
            FieldTypes.SIGNATURE_DRAWER -> inflateSignatureDrawer(
                context,
                containerLayout,
                it
            )
            else -> {
                logger.d(
                    TAG,
                    "skipping inflating ${it.id},${it.title} as it lacks fieldtype ${it.fieldType} doesnt match any present in app"
                )
            }
        }
    }




    private fun compareVerificationFieldTypeAndInflateRequiredLayout(
        context: Context,
        containerLayout: LinearLayout,
        it: DynamicVerificationField
    ) {
        when (it.fieldType) {
            FieldTypes.AADHAAR_VERIFICATION_VIEW -> inflateAadhaarVerificationView(
                context,
                containerLayout,
                it
            )
            FieldTypes.BANK_VERIFICATION_VIEW -> inflateBankDetailsField(
                context,
                containerLayout,
                it
            )
            FieldTypes.DL_VERIFICATION_VIEW -> inflateDLField(
                context,
                containerLayout,
                it
            )
            FieldTypes.PAN_VERIFICATION_VIEW -> inflatePANField(
                context,
                containerLayout,
                it
            )
            FieldTypes.SIGNATURE_DRAWER_2 -> inflateSignatureDrawer2(
                context,
                containerLayout,
                it
            )
            FieldTypes.VACCINATION_CERTIFICATE_VIEW -> inflateVaccineCertificateUploadView(
                context,
                containerLayout,
                it
            )
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

    private fun inflateMultiSelectDropDown(
        context: Context,
        containerLayout: LinearLayout,
        it: DynamicField
    ) {
        val view = DynamicMultiSelectDropDown(context, null)
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
        it: DynamicField
    ) {
        val view = DynamicSignatureDrawerView(context, null)
        containerLayout.addView(view)
//        view.bind(it)
    }

    private fun inflateSignatureDrawer2(
        context: Context,
        containerLayout: LinearLayout,
        it: DynamicVerificationField
    ) {
        val view = DynamicSignatureDrawerView2(context, null)
        containerLayout.addView(view)
        view.bind(it)
    }

    private fun inflateVaccineCertificateUploadView(
        context: Context,
        containerLayout: LinearLayout,
        it: DynamicVerificationField
    ) {
        val view = DynamicVaccineCertificateUploadVerificationView(context, null)
        containerLayout.addView(view)
        view.bind(it)
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

    fun signatureCapturedUpdateStatus(
       dynamicFieldsContainer : LinearLayout,
       signatureImagePathInFirebase : String,
       signatureImageFullUrl : String
    ){

        for (i in 0 until dynamicFieldsContainer.childCount) {

            val dynamicFieldView = dynamicFieldsContainer.getChildAt(i) as DynamicVerificationFieldView

            if (dynamicFieldView.fieldType == FieldTypes.SIGNATURE_DRAWER_2 ) {
                (dynamicFieldView as DynamicSignatureDrawerView2).signatureCapturedUpdateStatus(
                    signatureImagePathInFirebase,
                    signatureImageFullUrl
                )
            }
        }
    }
}