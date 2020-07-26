package com.gigforce.app.modules.roster

import android.content.Context
import android.util.AttributeSet
import android.view.View
import com.gigforce.app.R
import com.google.android.material.card.MaterialCardView
import kotlinx.android.synthetic.main.completed_gig_card.view.*
import kotlinx.android.synthetic.main.completed_gig_card.view.gig_timing

class CompletedGigCard(
    context: Context,
    var gigSuccess: Boolean = false,
    var paymentSuccess: Boolean = false,
    var startHour: Int = 0,
    var startMinute: Int = 0,
    var duration: Float = 0.0F,
    var cardHeight: Int = 0,
    var rating: Float = 0.0F,
    var amount: Int = 0,
    var title: String = ""
): MaterialCardView(context) {
    //constructor(context: Context): super(context)
    //constructor(context: Context, attrs: AttributeSet): super(context, attrs)

    init {
        View.inflate(context, R.layout.completed_gig_card, this)
        if (gigSuccess) setGigSuccess()
        if (paymentSuccess) setPaymentSuccess()
        setHeightCard(cardHeight)
        setGigRating(rating)
        setGigAmount(amount)
        gig_title.text = title
    }

    fun setGigSuccess() {
        gigSuccess = true
        if (gigSuccess) {
            gig_success_icon.setImageResource(R.drawable.ic_gig_success_icon)
        } else {
            gig_success_icon.setImageResource(R.drawable.ic_gigpending)
        }
    }

    fun setPaymentSuccess() {
        paymentSuccess = true
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
        if(value > 0) {
            gig_rating.text = value.toString()
        }
    }

    fun setGigAmount(value: Int) {
        amount = value
        rupee_value.text = value.toString()
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
            cardHeight = 70.px
        }
}