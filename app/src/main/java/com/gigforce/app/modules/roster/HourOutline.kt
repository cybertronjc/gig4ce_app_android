package com.gigforce.app.modules.roster

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.View
import com.gigforce.app.R
import com.gigforce.core.extensions.px
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

    fun resetHeightAndTopMargin(itemHeight: Int) {
        Log.d("HourOutline", "Entered with $startHour $startMinute $endHour $endMinute")
        main_card.layoutParams.height = ((endHour - startHour) * itemHeight -
                (startMinute/60.0F)*itemHeight +
                (endMinute/60.0F)*itemHeight)
            .toInt().px
        main_card.requestLayout()

        marginTop = (itemHeight * startHour + (startMinute/60.0F) * itemHeight).toInt().px
//        (main_card.layoutParams as MarginLayoutParams).setMargins(0, marginTop, 0, 0)
//
//        main_card.requestLayout()

        Log.d("HourOutline", "height margin changed")
    }

    var marginTop: Int = 0
        set(value) {
            field = value
        }
}
