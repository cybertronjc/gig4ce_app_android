package com.gigforce.app.modules.roster

import android.content.Context
import android.view.View
import androidx.navigation.findNavController
import com.gigforce.app.R
import com.gigforce.app.utils.GigNavigation
import com.google.android.material.card.MaterialCardView
import com.google.firebase.Timestamp
import kotlinx.android.synthetic.main.upcoming_gig_card.view.*
import java.text.SimpleDateFormat
import java.util.*

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
        var gigId: String = "",
        var isNewGigPage : Boolean,
        var startDateTime : Timestamp,
        var endDateTime : Timestamp
): MaterialCardView(context) {

    private val timeFormatter = SimpleDateFormat("hh.mm aa", Locale.getDefault())

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
        gig_timing.text = "${timeFormatter.format (startDateTime.toDate())} - ${timeFormatter.format(endDateTime.toDate())}"
    }

    fun setFullDay() {
        gig_timing.text = ""
        cardHeight = 40.px

        this.setOnClickListener {
            GigNavigation.openGigMainPage(findNavController(), isNewGigPage, gigId)
        }
    }
}