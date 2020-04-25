package com.gigforce.app.modules.roster

import android.content.Context
import android.os.Build
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import androidx.annotation.RequiresApi
import com.gigforce.app.R
import com.google.android.material.card.MaterialCardView
import kotlinx.android.synthetic.main.day_view_top_bar.view.*
import java.time.LocalDateTime
import kotlin.collections.ArrayList

@RequiresApi(Build.VERSION_CODES.O)
class RosterTopBar: MaterialCardView {
    constructor(context: Context): super(context)
    constructor(context: Context, attrs: AttributeSet): super(context, attrs)

    init {
        View.inflate(context, R.layout.day_view_top_bar, this)
        var months = ArrayList<String> (
            listOf("January", "February", "March", "April", "May",
                "June", "July", "August", "September", "October",
                "November", "December"))
        val datetime = LocalDateTime.now()
        for ((index, month) in months.withIndex()) {
            months[index] = month + " " + datetime.year
        }
        month_selector.adapter = ArrayAdapter(this.context, R.layout.simple_spinner_dropdown_item , months)
        month_selector.setSelection(months.map { it.toUpperCase() }.indexOf(datetime.month.toString() + " " + datetime.year))

        day_text.text = datetime.dayOfWeek.toString().toLowerCase()
        date_text.text = datetime.dayOfMonth.toString()
    }


}