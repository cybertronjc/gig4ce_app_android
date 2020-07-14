package com.gigforce.app.modules.roster

import android.content.Context
import android.util.AttributeSet
import android.view.View
import com.gigforce.app.R
import com.google.android.material.card.MaterialCardView
import kotlinx.android.synthetic.main.upcoming_gig_card.view.*

class UpcomingGigCard: MaterialCardView {
    constructor(context: Context): super(context)
    constructor(context: Context, attrs: AttributeSet): super(context, attrs)

    init {
        View.inflate(context, R.layout.upcoming_gig_card, this)
    }

    var startHour: Int = 0
        set(value) {
            field = value
        }

    var startMinute: Int = 0
        set(value) {
            field = value
        }

    var duration: Float = 0.0F
        set(value) {
            field = value
        }

    var cardHeight: Int = 0
        set(value) {
            field = value
            upcoming_card.layoutParams.height = value
            upcoming_card.requestLayout()
        }

    fun setTimings() {
        var endHour = startHour + duration.toInt()
        var endMinute = ((duration - duration.toInt())*100).toInt()
        gig_timing.text = (
                String.format("%02d", startHour) + ":" + String.format("%02d", startMinute) +
                        "-" + String.format("%02d", endHour) + ":" + String.format("%02d", endMinute))
    }

    var isFullDay: Boolean = false
        set(value) {
            field = value
            gig_timing.text = ""
            cardHeight = 40.px
        }
}