package com.gigforce.app.modules.profile

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.View
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.cardview.widget.CardView
import com.gigforce.app.R
import kotlinx.android.synthetic.main.fragment_profile_main_expanded.view.*
import kotlinx.android.synthetic.main.profile_card_background.view.*

class ProfileCardBackground: CardView {
    constructor(context: Context): super(context)
    constructor(context: Context, attrs:AttributeSet): super(context, attrs)

    init {
        View.inflate(context, R.layout.profile_card_background, this)
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
            var location = 0
            val viewgroup = card_content
            for (item in value.split("\n\n")) {
                if (item.toString() != "") {

                    val widget = CardRow(this.context!!)
                    widget.rowContent = item
                    widget.rowLocation = location.toString()

                    viewgroup.addView(widget)
                }
            }
        }

    var cardBottom: String = ""
        set(value) {
            field = value
            card_bottom.text = value
        }
}