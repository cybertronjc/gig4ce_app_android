package com.gigforce.app.modules.profile

import android.content.Context
import android.util.AttributeSet
import android.view.View
import androidx.cardview.widget.CardView
import com.gigforce.app.R
import kotlinx.android.synthetic.main.profile_main_card_background.view.*

class ProfileMainCardBackground: CardView {
    constructor(context: Context): super(context)
    constructor(context: Context, attrs: AttributeSet): super(context, attrs)

    init {
        View.inflate(context, R.layout.profile_main_card_background, this)
    }

    // setters
    var cardTitle: String = ""
        set(value) {
            field=value
            card_title.text = value
        }

    var cardContent: String = ""
        set(value) {
            field=value
            card_content.text = value
        }
}