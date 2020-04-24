package com.gigforce.app.modules.roster

import android.content.Context
import android.util.AttributeSet
import android.view.View
import com.gigforce.app.R
import com.google.android.material.card.MaterialCardView
import kotlinx.android.synthetic.main.completed_gig_card.view.*

class CompletedGigCard: MaterialCardView {
    constructor(context: Context): super(context)
    constructor(context: Context, attrs: AttributeSet): super(context, attrs)

    init {
        View.inflate(context, R.layout.completed_gig_card, this)
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
}