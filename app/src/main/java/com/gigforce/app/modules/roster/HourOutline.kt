package com.gigforce.app.modules.roster

import android.content.Context
import android.util.AttributeSet
import android.view.View
import com.gigforce.app.R
import com.google.android.material.card.MaterialCardView

class HourOutline: MaterialCardView {
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    init {
        View.inflate(context, R.layout.hour_selected_outline, this)

    }
}
