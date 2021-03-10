package com.gigforce.common_ui.utils

import android.content.Context
import android.util.AttributeSet
import android.view.View
import com.gigforce.common_ui.R
import com.google.android.material.card.MaterialCardView

class VerifiedButton: MaterialCardView {
    constructor(context: Context): super(context)
    constructor(context: Context, attrs: AttributeSet): super(context, attrs)

    init {
        View.inflate(context, R.layout.verified_button, this)
    }
}