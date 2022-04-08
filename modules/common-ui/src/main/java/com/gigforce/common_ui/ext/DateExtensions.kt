package com.gigforce.common_ui.ext

import android.app.DatePickerDialog
import android.content.Context
import android.view.View
import com.gigforce.common_ui.core.IValueChangeListener
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.ZoneId
import java.util.*


fun LocalDate.toDate() : Date{
   return Date.from(atStartOfDay(ZoneId.systemDefault()).toInstant());
}

fun View.transformIntoDatePicker(context: Context, format: String, minDate: Date? = null, valueChangeListener : IValueChangeListener) {

   isFocusableInTouchMode = false

   isClickable = true

   isFocusable = false

   val myCalendar = java.util.Calendar.getInstance()

   val datePickerOnDataSetListener =

      android.app.DatePickerDialog.OnDateSetListener { _, year, monthOfYear, dayOfMonth ->

         myCalendar.set(java.util.Calendar.YEAR, year)

         myCalendar.set(java.util.Calendar.MONTH, monthOfYear)

         myCalendar.set(java.util.Calendar.DAY_OF_MONTH, dayOfMonth)

         val sdf = SimpleDateFormat(format)

         valueChangeListener.valueChangeListener(sdf.format(myCalendar.time))

      }

   setOnClickListener {

      DatePickerDialog(

         context, datePickerOnDataSetListener, myCalendar

            .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),

         myCalendar.get(Calendar.DAY_OF_MONTH)

      ).run {

         minDate?.time?.also { datePicker.minDate = it }

         show()

      }

   }

}