package com.gigforce.giger_app.roster

import android.content.Context
import android.util.AttributeSet
import android.view.View
import com.gigforce.giger_app.R
import com.google.android.material.card.MaterialCardView
import kotlinx.android.synthetic.main.item_roster_day.view.*

class HourRow : MaterialCardView {
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    init {
        View.inflate(context, R.layout.item_roster_day, this)
    }

    var time: String = ""
        set(value) {
            field = value
            item_time.text = value
        }

    var hour: Int = 0
        set(value) {
            field = value
            this.tag = "hour_$value"
        }

    var clicked: Boolean = false

    var minute: Int = 0

    var isDisabled = false
        set(value) {
            field = value
            if (isDisabled) {
                item_time.setTextColor(resources.getColor(R.color.gray_color_calendar))
                top_half.isClickable = false
                bottom_half.isClickable = false
                this.isClickable = false
            } else {
                item_time.setTextColor(resources.getColor(R.color.black))
                top_half.isClickable = true
                bottom_half.isClickable = true
                this.isClickable = true
            }
        }

//    var isCurrentTime: Boolean = false
//        set(value) {
//            field = value
//            if (value) {
//                current_time_divider.visibility = View.VISIBLE
//                var p = current_time_divider.layoutParams as ViewGroup.MarginLayoutParams
//                if (minute < 30) {
//                    p.setMargins(85.px, (0.5*70 + (minute / 60.0) * 70).toInt().px, 0, 0)
//                }
//                else if (minute >= 30) {
//                    p.setMargins(85.px, ((minute / 60.0) * 70).toInt().px, 0, 0)
//                }
//                current_time_divider.requestLayout()
//            }
//        }
}