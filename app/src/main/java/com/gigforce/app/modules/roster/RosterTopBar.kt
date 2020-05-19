package com.gigforce.app.modules.roster

import android.content.Context
import android.os.Build
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import androidx.annotation.RequiresApi
import com.gigforce.app.R
import com.gigforce.app.utils.DropdownAdapter
import com.google.android.material.card.MaterialCardView
import kotlinx.android.synthetic.main.day_view_top_bar.view.*
import java.time.LocalDateTime
import kotlin.collections.ArrayList

@RequiresApi(Build.VERSION_CODES.O)
class RosterTopBar: MaterialCardView {
    constructor(context: Context): super(context)
    constructor(context: Context, attrs: AttributeSet): super(context, attrs)

    val datetime = LocalDateTime.now()

    var monthTemplate = ArrayList<String> (
        listOf("January", "February", "March", "April", "May",
            "June", "July", "August", "September", "October",
            "November", "December")
    )

    var months = ArrayList<String> (
        listOf("January", "February", "March", "April", "May",
            "June", "July", "August", "September", "October",
            "November", "December"))

    var days = ArrayList<String> (
        listOf("Mon", "Tue", "Wed", "Thur", "Fri", "Sat", "Sun")
    )

    init {
        View.inflate(context, R.layout.day_view_top_bar, this)

        year = datetime.year
        month = datetime.monthValue - 1
        date = datetime.dayOfMonth
        day = datetime.dayOfWeek.value - 1
    }

    var year: Int = 0
        set(value) {
            field = value
            for ((index, month) in monthTemplate.withIndex()) {
                months[index] = month + " " + value
            }
            month_selector.setAdapter(ArrayAdapter(this.context, R.layout.simple_spinner_dropdown_item ,months))
        }

    var month: Int = 0
        set(value) {
            // store years from 0 - 11 as index
            field = value
            month_selector.setSelection(value)
        }

    var date: Int = 0
        set(value) {
            field = value
            date_text.text = date.toString()
        }

    var isCurrentDay: Boolean = false
        set(value){
            field = value
            if (!value) {
                date_text.setTextColor(resources.getColor(R.color.gray_color_calendar))
                day_text.setTextColor(resources.getColor(R.color.gray_color_calendar))
            } else {
                date_text.setTextColor(resources.getColor(R.color.colorPrimary))
                day_text.setTextColor(resources.getColor(R.color.colorPrimary))
            }
        }

    var day: Int = 0
        set(value) {
            field = value
            day_text.text = days[value]
        }

    var isAvailable: Boolean = true
        set(value) {
            field = value
            if (isAvailable)
                available_toggle.setIconResource(R.drawable.ic_toggle_on)
            else {
                available_toggle.setIconResource(R.drawable.ic_toggle_off)
            }
        }
}