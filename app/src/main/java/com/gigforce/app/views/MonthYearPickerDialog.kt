package com.gigforce.app.views

import android.content.Context
import android.content.DialogInterface
import android.view.LayoutInflater
import android.view.View
import android.widget.NumberPicker
import androidx.appcompat.app.AlertDialog
import com.gigforce.app.R
import java.util.*

class MonthYearPickerDialog constructor(
        context: Context,
        private val listener: OnDateSetListener
) : AlertDialog.Builder(context) {

    //Views
    private var alertDialog: AlertDialog? = null
    private lateinit var pickerView: View
    private lateinit var monthPicker: NumberPicker
    private lateinit var yearPicker: NumberPicker

    //Selected
    private var selectedMonth = -1
    private var selectedYear = -1

    private fun initView() {
        pickerView = LayoutInflater.from(context).inflate(R.layout.date_picker_dialog, null)
        setTitle("Select Month & Year")
        setView(pickerView)
        setButtonsAndListeners()
        create()
        initViews()
    }

    private fun setButtonsAndListeners() {
        setPositiveButton("Okay") { dialog: DialogInterface?, which: Int ->
            selectedMonth = monthPicker.value
            selectedYear = yearPicker.value

            prepareDateAndSendBack()

        }
        setNegativeButton("Cancel") { dialog: DialogInterface?, which: Int -> alertDialog?.dismiss() }
    }

    private fun prepareDateAndSendBack() {
        val newCal = Calendar.getInstance()
        newCal.set(Calendar.YEAR, selectedYear)
        newCal.set(Calendar.MONTH, selectedMonth)
        newCal.set(Calendar.DAY_OF_MONTH, 1)
        newCal.set(Calendar.HOUR_OF_DAY, 0)
        newCal.set(Calendar.MINUTE, 0)
        newCal.set(Calendar.SECOND, 0)
        newCal.set(Calendar.MILLISECOND, 0)

        listener.onDateSet(newCal.time, selectedYear, selectedMonth)
    }

    override fun show(): AlertDialog {
        if (alertDialog == null) {
            alertDialog = create()
        }

        if (!alertDialog!!.isShowing) {
            alertDialog?.show()
        }

        return alertDialog!!
    }

    private fun initViews() {
        val calendar = Calendar.getInstance()
        monthPicker = pickerView.findViewById(R.id.picker_month)
        yearPicker = pickerView.findViewById(R.id.picker_year)

//        monthPicker.minValue = 1
//        monthPicker.maxValue = 12

       // selectedMonth = calendar[Calendar.MONTH] + 1
       // monthPicker.value = selectedMonth
       // selectedYear = calendar[Calendar.YEAR]
//        yearPicker.minValue = selectedYear - 5
//        yearPicker.maxValue = selectedYear
    //     yearPicker.value = selectedYear
    }

    fun updateMonthAndYear(monthOfYear: Int, year: Int) {
            monthPicker.value = monthOfYear
            yearPicker.value = year
    }

    fun setMinMonth(month : Int){

    }

    init {
        initView()
    }


    /**
     * The listener used to indicate the user has finished selecting a date.
     */
    interface OnDateSetListener {

        fun onDateSet(date: Date, year: Int, month: Int)
    }
}