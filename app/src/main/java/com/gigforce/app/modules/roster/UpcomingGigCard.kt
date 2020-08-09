package com.gigforce.app.modules.roster

import android.content.Context
import android.os.Bundle
import android.util.AttributeSet
import android.view.View
import androidx.navigation.findNavController
import com.gigforce.app.R
import com.gigforce.app.modules.gigPage.GigPageFragment
import com.google.android.material.card.MaterialCardView
import kotlinx.android.synthetic.main.upcoming_gig_card.view.*

class UpcomingGigCard(
    context: Context,
    var startHour: Int = 0,
    var startMinute: Int = 0,
    var endHour: Int = 0,
    var endMinute: Int = 0,
    var duration: Float = 0.0F,
    var title: String = "",
    var cardHeight: Int = 0,
    var isFullDay: Boolean = false,
    var gigId: String = ""
): MaterialCardView(context) {
    init {
        View.inflate(context, R.layout.upcoming_gig_card, this)
        setCardHeight()
        setTitle()
        if (duration != 0.0F)
            setTimings()
        if (isFullDay) setFullDay()
    }

    fun setTitle() {
        gig_title.text = title
    }

    fun setCardHeight() {
        upcoming_card.layoutParams.height = cardHeight
        upcoming_card.requestLayout()
    }

    fun setTimings() {
        var endHour = startHour + duration.toInt()
        var endMinute = ((duration - duration.toInt())*100).toInt()
        gig_timing.text = (
                String.format("%02d", startHour) + ":" + String.format("%02d", startMinute) +
                        "-" + String.format("%02d", endHour) + ":" + String.format("%02d", endMinute))
    }

    fun setFullDay() {
            gig_timing.text = ""
            cardHeight = 40.px

        this.setOnClickListener {
            findNavController().navigate(R.id.presentGigPageFragment, Bundle().apply {
                this.putString(GigPageFragment.INTENT_EXTRA_GIG_ID, gigId)
            })
        }
    }
}