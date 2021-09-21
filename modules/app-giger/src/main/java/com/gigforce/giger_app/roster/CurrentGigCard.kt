package com.gigforce.giger_app.roster

import android.content.Context
import android.view.View
import androidx.core.os.bundleOf
import com.gigforce.core.AppConstants
import com.gigforce.core.extensions.px
import com.gigforce.core.navigation.INavigation
import com.gigforce.giger_app.R
import com.google.android.material.card.MaterialCardView
import com.google.firebase.Timestamp
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.upcoming_gig_card.view.*
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

@AndroidEntryPoint
class CurrentGigCard(
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
        var isNewGigPage: Boolean,
        var startDateTime: Timestamp,
        var endDateTime: Timestamp
) : MaterialCardView(context) {

    private val timeFormatter = SimpleDateFormat("hh.mm aa", Locale.getDefault())

    @Inject
    lateinit var navigation: INavigation

    init {
        View.inflate(context, R.layout.current_gig_card, this)
        setCardHeight()
        if (duration != 0.0F)
            setTimings()
        setTitle()
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
        gig_timing.text = "${timeFormatter.format(startDateTime.toDate())} - ${timeFormatter.format(endDateTime.toDate())}"
    }

    fun setFullDay() {
        gig_timing.text = ""
        cardHeight = 40.px

        this.setOnClickListener {
            navigation.navigateTo("gig/attendance", bundleOf(AppConstants.INTENT_EXTRA_GIG_ID to gigId))
//            GigNavigation.openGigAttendancePage(findNavController(),isNewGigPage, Bundle().apply {
//                this.putString(com.gigforce.giger_gigs.GigPage2Fragment.INTENT_EXTRA_GIG_ID, gigId)
//            })
        }
    }
}