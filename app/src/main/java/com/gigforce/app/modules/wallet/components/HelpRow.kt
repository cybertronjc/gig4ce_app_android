package com.gigforce.app.modules.wallet.components

import android.content.Context
import android.util.AttributeSet
import android.view.View
import com.gigforce.app.R
import com.google.android.material.card.MaterialCardView
import kotlinx.android.synthetic.main.help_row.view.*

class HelpRow(context: Context, attributeSet: AttributeSet) : MaterialCardView(context, attributeSet) {

    init {
        View.inflate(context, R.layout.help_row, this)

        attributeSet?.let {
            context.theme.obtainStyledAttributes(
                it, R.styleable.HelpRow, 0, 0
            ).apply {
                content_help = getString(R.styleable.HelpRow_content_help).toString()
            }
        }
    }

    var content_help: String = ""
        set(value) {
            field = value
            text.text = content_help
        }
}