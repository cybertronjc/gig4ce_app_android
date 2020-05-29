package com.gigforce.app.modules.roster

import android.content.Context
import android.util.AttributeSet
import android.view.View
import com.gigforce.app.R
import com.google.android.material.card.MaterialCardView
import kotlinx.android.synthetic.main.hour_selected_outline.view.*

class HourOutline: MaterialCardView {
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    init {
        View.inflate(context, R.layout.hour_selected_outline, this)

    }

    var startHour: Int = 0
        set(value) {
            field = value
        }

    var startMinute: Int = 0
        set (value) {
            field = value
        }

    var endHour: Int = 0
        set(value) {
            field = value
        }

    var endMinute: Int = 0
        set(value) {
            field = value
        }

    fun resetHeight() {
        main_card.layoutParams.height = (
                (endHour - startHour) * 70 - (startMinute/60)*70 + (endMinute/60)*70).px
        main_card.requestLayout()
    }
}
