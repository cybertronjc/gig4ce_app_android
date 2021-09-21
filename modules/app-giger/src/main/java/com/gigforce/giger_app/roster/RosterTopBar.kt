package com.gigforce.giger_app.roster

import android.content.Context
import android.os.Build
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import com.gigforce.common_ui.adapter.DropdownAdapter
import com.gigforce.giger_app.R
import com.google.android.material.card.MaterialCardView
import kotlinx.android.synthetic.main.day_view_top_bar.view.*
import java.time.LocalDateTime

@RequiresApi(Build.VERSION_CODES.O)
class RosterTopBar : MaterialCardView {
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    val datetime = LocalDateTime.now()

    var monthTemplate = ArrayList<String>(
            listOf(context.getString(R.string.jan_app_giger), context.getString(R.string.feb_app_giger), context.getString(R.string.mar_app_giger), context.getString(
                    R.string.apr_app_giger), context.getString(R.string.may_app_giger),
                    context.getString(R.string.june_app_giger), context.getString(R.string.july_app_giger), context.getString(R.string.aug_app_giger), context.getString(
                    R.string.sep_app_giger), context.getString(R.string.oct_app_giger),
                    context.getString(R.string.nov_app_giger), context.getString(R.string.dec_app_giger))
    )

    var months = ArrayList<String>(
            listOf(context.getString(R.string.jan_app_giger), context.getString(R.string.feb_app_giger), context.getString(R.string.mar_app_giger), context.getString(
                    R.string.apr_app_giger), context.getString(R.string.may_app_giger),
                    context.getString(R.string.june_app_giger), context.getString(R.string.july_app_giger), context.getString(R.string.aug_app_giger), context.getString(
                    R.string.sep_app_giger), context.getString(R.string.oct_app_giger),
                    context.getString(R.string.nov_app_giger), context.getString(R.string.dec_app_giger)))

    var days = ArrayList<String>(
            listOf(context.getString(R.string.mon_app_giger), context.getString(R.string.tue_app_giger), context.getString(R.string.wed_app_giger), context.getString(
                    R.string.thur_app_giger), context.getString(R.string.fri_app_giger), context.getString(R.string.sat_app_giger), context.getString(
                    R.string.sun_app_giger))
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
            month_selector.adapter = DropdownAdapter(this.context, months)
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
        set(value) {
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
            if (value) {
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
//            available_toggle.isEnabled = !value
            available_toggle.alpha = if (value) 0.3f else 1f
        }

    var fullDayGigCard: MaterialCardView? = null
        set(value) {
            field = value
            full_day_gig.removeAllViews()
            value?.let {
                value.id = View.generateViewId()
                full_day_gig.addView(value)
                value.layoutParams =
                        ConstraintLayout.LayoutParams(LayoutParams.MATCH_PARENT, value.height)
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