package com.gigforce.common_ui.dynamic_fields.types

import android.app.DatePickerDialog
import android.content.Context
import android.text.SpannedString
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.DatePicker
import android.widget.LinearLayout
import androidx.core.text.bold
import androidx.core.text.buildSpannedString
import com.gigforce.common_ui.R
import com.gigforce.common_ui.databinding.LayoutDynamicFieldDatePickerBinding
import com.gigforce.common_ui.dynamic_fields.DynamicFieldView
import com.gigforce.common_ui.dynamic_fields.data.DataFromDynamicInputField
import com.gigforce.common_ui.dynamic_fields.data.DynamicField
import com.gigforce.common_ui.dynamic_fields.data.FieldTypes
import com.gigforce.common_ui.ext.addMandatorySymbolToTextEnd
import com.gigforce.common_ui.ext.toDate
import com.gigforce.core.extensions.gone
import com.gigforce.core.extensions.visible
import java.time.LocalDate
import java.time.format.DateTimeFormatter


class DynamicDatePickerView(
    context: Context,
    attrs: AttributeSet?
) : LinearLayout(
    context,
    attrs
), DynamicFieldView {

    private val dateFormatter = DateTimeFormatter.ISO_LOCAL_DATE //YYYY-MM-DD
    private val dateFormatterForViewing = DateTimeFormatter.ofPattern("dd/MMM/yy")

    private var viewBinding: LayoutDynamicFieldDatePickerBinding
    private lateinit var viewData: DynamicField
    private var selectedDate: LocalDate? = null

    override val fieldType: String
        get() = FieldTypes.DATE_PICKER

    init {
        this.layoutParams = LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )

        viewBinding = LayoutDynamicFieldDatePickerBinding.inflate(
            LayoutInflater.from(context),
            this,
            true
        )

        setListenersOnView()
    }

    private val datePicker: DatePickerDialog by lazy {

        val defaultSelectedDate = if (viewData.defaultSelectedDate != null) {
            getDateFromText(viewData.defaultSelectedDate!!)
        } else {
            LocalDate.now()
        }

        val datePickerDialog = DatePickerDialog(
            context, { _: DatePicker?, year: Int, month: Int, dayOfMonth: Int ->
                val date = LocalDate.of(
                    year,
                    month + 1 ,
                    dayOfMonth
                )

                viewBinding.selectedDateLabel.text = dateFormatterForViewing.format(date)
                selectedDate = date
            },
            defaultSelectedDate.year,
            defaultSelectedDate.monthValue - 1,
            defaultSelectedDate.dayOfMonth
        )

        if(viewData.minDateAvailableForSelection != null){

            val minimumDateAvailableForSelection = getDateFromText(viewData.minDateAvailableForSelection!!)
            datePickerDialog.datePicker.minDate = minimumDateAvailableForSelection.toDate().time
        }

        if(viewData.maxDateAvailableForSelection != null){

            val maxDateAvailableForSelection = getDateFromText(viewData.maxDateAvailableForSelection!!)
            datePickerDialog.datePicker.maxDate = maxDateAvailableForSelection.toDate().time
        }

        datePickerDialog
    }

    private fun getDateFromText(
        dateText : String
    ) = if (dateText == "today") {
        LocalDate.now()
    } else {
        LocalDate.parse(dateText, dateFormatter)
    }

    override fun bind(
        fieldDetails: DynamicField
    ) {
        viewData = fieldDetails
        tag = id //setting id of dynamic view as view tag to identify layout at runtime

        setTitle(fieldDetails.title)
        settingFieldAsOptionalOrMandatory(fieldDetails)
        setPrefillTextOrHint(
            fieldDetails.defaultSelectedDate,
            fieldDetails.prefillText,
            fieldDetails.title
        )
    }

    private fun setPrefillTextOrHint(
        defaultSelectedDate : String?,
        prefillText: String?,
        title: String?
    ) {

        if(defaultSelectedDate != null){
            selectedDate =  getDateFromText(defaultSelectedDate)
            viewBinding.selectedDateLabel.text = dateFormatterForViewing.format(selectedDate)
        } else if (prefillText != null) {
            viewBinding.selectedDateLabel.text = prefillText
        } else {
            val hint = "Select $title"
            viewBinding.selectedDateLabel.text = hint
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
        return if(viewData.mandatory){
            selectedDate != null
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

        val dateSelectedDate = selectedDate?.run {  dateFormatter.format(selectedDate)}
        return DataFromDynamicInputField(
            id = viewData.id,
            title = viewData.title,
            value = dateSelectedDate,
            fieldType = FieldTypes.DATE_PICKER
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

    private fun setListenersOnView() = viewBinding.apply {

        changeDateBtn.setOnClickListener {
            datePicker.show()
        }
    }
}