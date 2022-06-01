package com.gigforce.app.tl_work_space.date_filter_bottomsheet

import androidx.core.util.Pair
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.MaterialDatePicker
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize
import java.util.concurrent.TimeUnit

@Parcelize
class DateRangeValidator constructor(
    private val maxDiffBetweenDays: Int
) : CalendarConstraints.DateValidator {

    @IgnoredOnParcel
    private lateinit var rangePicker: MaterialDatePicker<*>

    fun setMaterialDatePicker(
        rangePicker: MaterialDatePicker<*>
    ) {
        this.rangePicker = rangePicker
    }

    override fun isValid(date: Long): Boolean {
        if (::rangePicker.isInitialized.not()) {
            throw IllegalStateException("call setMaterialDatePicker() before validating dates")
        }

        val rangeSelected = rangePicker.selection ?: return true
        val selectedRangeTimeStamps = rangeSelected as Pair<Long,Long>
        if(selectedRangeTimeStamps.first == null || selectedRangeTimeStamps.second == null) {
            return true
        }

        val daysDiffInMillis = selectedRangeTimeStamps.second - selectedRangeTimeStamps.first
        return TimeUnit.MILLISECONDS.toDays(daysDiffInMillis) <= maxDiffBetweenDays
    }
}