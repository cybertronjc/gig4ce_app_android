package com.gigforce.app.modules.roster

import android.content.Context
import android.util.AttributeSet
import android.view.View
import com.gigforce.app.R
import com.google.android.material.card.MaterialCardView
import kotlinx.android.synthetic.main.completed_gig_card.view.*
import kotlinx.android.synthetic.main.completed_gig_card.view.gig_timing
import kotlinx.android.synthetic.main.upcoming_gig_card.view.*

class CompletedGigCard: MaterialCardView {
    constructor(context: Context): super(context)
    constructor(context: Context, attrs: AttributeSet): super(context, attrs)

    init {
        View.inflate(context, R.layout.completed_gig_card, this)
    }

    var gigSuccess: Boolean = true
        set(value) {
            field = value
            if (value) {
                gig_success_icon.setImageResource(R.drawable.ic_gig_success_icon)
            } else {
                gig_success_icon.setImageResource(R.drawable.ic_gigpending)
            }
        }

    var paymentSuccess: Boolean = false
        set(value) {
            field = value
            if (value) {
                rupee_icon.setImageResource(R.drawable.ic_payment_success)
            } else {
                rupee_icon.setImageResource(R.drawable.ic_paymentpending)
            }
        }

    var gigStartHour: Int = 0
        set(value) {
            field = value
        }

    var gigStartMinute: Int = 0
        set(value) {
            field = value
        }

    var gigDuration: Float = 0.0F
        set(value) {
            field = value
        }

    var cardHeight: Int = 0
        set(value) {
            field = value
            main_card.layoutParams.height = value
            main_card.requestLayout()
        }

    var gigRating: Float = 0F
        set(value) {
            field = value
            if(value > 0) {
                gig_rating.text = value.toString()
            }
        }

    var gigAmount: Int = 0
        set(value) {
            field = value
            rupee_value.text = value.toString()
        }

    fun setTimings() {
        var endHour = gigStartHour + gigDuration.toInt()
        var endMinute = ((gigDuration - gigDuration.toInt())*100).toInt()
        gig_timing.text = (
                String.format("%02d", gigStartHour) + ":" + String.format("%02d", gigStartMinute) +
                        "-" + String.format("%02d", endHour) + ":" + String.format("%02d", endMinute))
    }

    var isFullDay: Boolean = false
        set(value) {
            field = value
            gig_timing.text = ""
            cardHeight = 70.px
        }
}