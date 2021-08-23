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
import kotlinx.android.synthetic.main.completed_gig_card.view.*
import kotlinx.android.synthetic.main.completed_gig_card.view.gig_timing
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

@AndroidEntryPoint
class CompletedGigCard(
    context: Context,
    var gigSuccess: Boolean = false,
    var paymentSuccess: Boolean = false,
    var startHour: Int = 0,
    var startMinute: Int = 0,
    var duration: Float = 0.0F,
    var cardHeight: Int = 0,
    var rating: Float = 0.0F,
    var amount: Double = 0.0,
    var title: String = "",
    var isFullDay: Boolean = false,
    var gigId: String = "",
    var isMonthlyGig: Boolean = false,
    var isNewgigPage : Boolean,
    var startDateTime : Timestamp,
    var endDateTime : Timestamp
): MaterialCardView(context) {
    //constructor(context: Context): super(context)
    //constructor(context: Context, attrs: AttributeSet): super(context, attrs)
    private val timeFormatter = SimpleDateFormat("hh.mm aa", Locale.getDefault())
    @Inject lateinit var navigation : INavigation
    init {
        View.inflate(context, R.layout.completed_gig_card, this)
        setGigSuccess()
        setPaymentSuccess()
        setHeightCard(cardHeight)
        setGigRating(rating)
        setGigAmount(amount)
        gig_title.text = title
        if (duration != 0.0F)
            setTimings()

        if (isFullDay) setFullDay()
    }



    fun setGigSuccess() {
        if (gigSuccess) {
            gig_success_icon.setImageResource(R.drawable.ic_gig_success_icon)
        } else {
            gig_success_icon.setImageResource(R.drawable.ic_gigpending)
        }
    }

    fun setPaymentSuccess() {
        if (paymentSuccess) {
            rupee_icon.setImageResource(R.drawable.ic_payment_success)
        } else {
            rupee_icon.setImageResource(R.drawable.ic_paymentpending)
        }
    }

    fun setHeightCard(value: Int) {
        cardHeight = value
        main_card.layoutParams.height = value
        main_card.requestLayout()
    }

    fun setGigRating(value: Float) {
        rating = value
        if (value > 0) {
            gig_rating.text = value.toString()
        }
    }

    fun setGigAmount(value: Double) {
        amount = value

        if (value == 0.0) {
            rupee_value.text = context.getString(R.string.as_per_contract)
        } else {
            rupee_value.text = if (isMonthlyGig) "Rs. $value /month" else "Rs. $value /hr"
        }
    }

    fun setTimings() {
        gig_timing.text = "${timeFormatter.format (startDateTime.toDate())} - ${timeFormatter.format(endDateTime.toDate())}"
    }

    fun setFullDay() {
        gig_timing.text = ""
        cardHeight = 70.px

        this.setOnClickListener {
            navigation.navigateTo("gig/attendance", bundleOf(
                    AppConstants.INTENT_EXTRA_GIG_ID to
                    gigId
            ))
//            GigNavigation.openGigMainPage(findNavController(), isNewgigPage, gigId)
        }
    }
}