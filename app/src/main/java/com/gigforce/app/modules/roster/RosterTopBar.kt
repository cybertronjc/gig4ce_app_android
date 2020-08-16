package com.gigforce.app.modules.roster

import android.content.Context
import android.content.res.ColorStateList
import android.os.Build
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.annotation.RequiresApi
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
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
        listOf(context.getString(R.string.jan), context.getString(R.string.feb), context.getString(R.string.mar), context.getString(
                    R.string.apr), context.getString(R.string.may),
            context.getString(R.string.june), context.getString(R.string.july), context.getString(R.string.aug), context.getString(
                            R.string.sep), context.getString(R.string.oct),
            context.getString(R.string.nov), context.getString(R.string.dec))
    )

    var months = ArrayList<String> (
        listOf(context.getString(R.string.jan), context.getString(R.string.feb), context.getString(R.string.mar), context.getString(
            R.string.apr), context.getString(R.string.may),
            context.getString(R.string.june), context.getString(R.string.july), context.getString(R.string.aug), context.getString(
                R.string.sep), context.getString(R.string.oct),
            context.getString(R.string.nov), context.getString(R.string.dec)))

    var days = ArrayList<String> (
        listOf(context.getString(R.string.mon), context.getString(R.string.tue), context.getString(R.string.wed), context.getString(
                    R.string.thur), context.getString(R.string.fri), context.getString(R.string.sat), context.getString(
                                R.string.sun))
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
            month_selector.setAdapter(DropdownAdapter(this.context, months))
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

    var isFutureDate: Boolean = false
        set(value) {
            field = value
            if(value) {
                date_text.setTextColor(resources.getColor(R.color.black))
                day_text.setTextColor(resources.getColor(R.color.gray_color_calendar))
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
            available_toggle.isChecked = isAvailable
        }

    var toggleInactive: Boolean = false
        set(value) {
            field = value
            available_toggle.isClickable = !value
        }

    var fullDayGigCard: MaterialCardView? = null
        set(value) {
            field = value
            full_day_gig.removeAllViews()
            value?.let {
                value.id = View.generateViewId()
                full_day_gig.addView(value)
                value.layoutParams = ConstraintLayout.LayoutParams(LayoutParams.MATCH_PARENT, value.height)
                val constraintSet = ConstraintSet()
                constraintSet.clone(full_day_gig)
                constraintSet.connect(
                    value.id,
                    ConstraintSet.START,
                    ConstraintSet.PARENT_ID,
                    ConstraintSet.START
                )
                constraintSet.connect(
                    value.id,
                    ConstraintSet.END,
                    ConstraintSet.PARENT_ID,
                    ConstraintSet.END
                )
                constraintSet.connect(
                    value.id,
                    ConstraintSet.TOP,
                    ConstraintSet.PARENT_ID,
                    ConstraintSet.TOP
                )
                constraintSet.connect(
                    value.id,
                    ConstraintSet.BOTTOM,
                    ConstraintSet.PARENT_ID,
                    ConstraintSet.BOTTOM
                )

                constraintSet.applyTo(full_day_gig)

                full_day_gig.layoutParams.width = ViewGroup.LayoutParams.WRAP_CONTENT
            }
        }
}