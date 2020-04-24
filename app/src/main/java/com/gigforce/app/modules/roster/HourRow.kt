package com.gigforce.app.modules.roster

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import com.gigforce.app.R
import com.google.android.material.card.MaterialCardView
import kotlinx.android.synthetic.main.item_roster_day.view.*

class HourRow: MaterialCardView {
    constructor(context: Context): super(context)
    constructor(context: Context, attrs: AttributeSet): super(context, attrs)

    init {
        View.inflate(context, R.layout.item_roster_day, this)
    }

    var time: String = ""
        set(value) {
            field = value
            item_time.text = value
        }

    var hour: Int = 0
        set (value) {
            field = value
            this.id = value
        }

    var minute: Int = 0
        set(value) {
            field = value
        }

    var isCurrentTime: Boolean = false
        set(value) {
            field = value
            if (value) {
                current_time_divider.visibility = View.VISIBLE
                var p = current_time_divider.layoutParams as ViewGroup.MarginLayoutParams
                p.setMargins(85.px, ((minute/60.0) * 48).toInt().px, 0, 0)
                current_time_divider.requestLayout()
            }
        }
}