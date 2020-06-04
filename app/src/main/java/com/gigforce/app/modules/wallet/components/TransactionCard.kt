package com.gigforce.app.modules.wallet.components

import android.content.Context
import android.util.AttributeSet
import android.view.View
import com.gigforce.app.R
import com.google.android.material.card.MaterialCardView

class TransactionCard: MaterialCardView {
    constructor(context: Context): super(context)
    constructor(context: Context, attrs:AttributeSet): super(context, attrs)

    init {
        View.inflate(context, R.layout.transaction_card, this)
    }
}