package com.gigforce.app.modules.roster

import android.content.Context
import android.util.AttributeSet
import android.view.View
import com.gigforce.app.R
import com.google.android.material.card.MaterialCardView
import kotlinx.android.synthetic.main.unavailable_card.view.*

class UnavailableCard: MaterialCardView {
    constructor(context: Context): super(context)
    constructor(context: Context, attrs: AttributeSet): super(context, attrs)

    init {
        View.inflate(context, R.layout.unavailable_card, this)
    }

    var unavailableStartHour: Int = 0
     set(value) {
         field = value
     }

    var unavailableStartMinute: Int = 0
        set(value) {
            field = value
        }

    var unavailableDuration: Float = 0.0F
        set(value) {
            field = value
        }

    var cardHeight: Int = 0
        set(value) {
            field = value
            unavailable_card.layoutParams.height = value
            unavailable_card.requestLayout()
        }
}