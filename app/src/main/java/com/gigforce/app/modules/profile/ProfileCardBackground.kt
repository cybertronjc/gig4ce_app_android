package com.gigforce.app.modules.profile

import android.content.Context
import android.os.Bundle
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.navigation.findNavController
import com.gigforce.app.R
import kotlinx.android.synthetic.main.card_row.view.*
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
            val viewgroup = card_content
            for ((location, item) in value.split("\n\n").withIndex()) {
                if (item.toString() == this.context!!.getString(R.string.empty_about_me_text)) {
                    val widget = TextView(this.context!!)
                    widget.text = item
                    viewgroup.addView(widget)
                    break
                }
                if (item.toString() != "") {
                    val widget = CardRow(this.context!!)
                    widget.rowContent = item
                    widget.rowLocation = location.toString()

                    var bundle = Bundle()
                    bundle.putString("array_location", location.toString())
                    Log.d("LOCATION", location.toString())
                    widget.edit_button.setOnClickListener {
                        findNavController().navigate(nextDestination, bundle)
                    }
                    viewgroup.addView(widget)
                }
            }
        }

    var cardBottom: String = ""
        set(value) {
            field = value
            card_bottom.text = value
        }

    var nextDestination: Int = 0
        set(value) {
            field = value
        }

    var isBottomRemoved: Boolean = false
        set(value) {
            field = value
            if (value) {
                (card_bottom.parent as ViewGroup).removeView(card_bottom)
                (bottom_divider.parent as ViewGroup).removeView(bottom_divider)
            }
        }
}