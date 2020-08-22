package com.gigforce.app.modules.profile

import android.content.Context
import android.os.Bundle
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.navigation.findNavController
import com.gigforce.app.R
import kotlinx.android.synthetic.main.card_row.view.*
import kotlinx.android.synthetic.main.profile_card_background.view.*

class ProfileCardBackground : CardView {
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    init {
        View.inflate(context, R.layout.profile_card_background, this)
    }

    private var callbacks: ProfileCardBgCallbacks? = null

    // setters
    var cardTitle: String = ""
        set(value) {
            field = value
            card_title.text = value
        }

    var hasContentTitles: Boolean = true
        set(value) {
            field = value
        }

    var cardContent: String = ""
        set(value) {
            field = value
            val viewgroup = card_content

            // TODO: Think if there is a better way such that only non
            //      duplicate elements can be loaded
            // re-initialize the content
            viewgroup.removeAllViews()

            for ((location, item) in value.split("\n\n").withIndex()) {
                if (item == this.context!!.getString(R.string.empty_about_me_text)) {
                    val widget = TextView(this.context!!)
                    widget.text = item
                    viewgroup.addView(widget)
                    break
                }
                if (item != "") {
                    val widget = CardRow(this.context!!)
                    if (showIsWhatsappCb) {
                        widget.showIsWhatsappCb = showIsWhatsappCb

                        widget.setCallbacks(object : CardRowCallbacks {
                            override fun checked(isChecked: Boolean, contactNumber: String) {
                                callbacks?.checked(isChecked, contactNumber)
                            }
                        })
                    }
                    if (hasContentTitles) {
                        widget.rowContent = ""
                        for ((idx, it) in item.split('\n').withIndex()) {
                            if (idx == 0)
                                widget.rowTitle = it
                            else
                                widget.rowContent += it + "\n"
                        }
                    } else {
                        widget.rowContent = item
                    }

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
    var showIsWhatsappCb: Boolean = false
        set(value) {
            field = value

        }

    var nextDestination: Int = 0
        set(value) {
            field = value
        }

    var isBottomRemoved: Boolean = false
        set(value) {
            field = value
            if (value) {
                card_bottom.visibility = View.GONE
                bottom_divider.visibility = View.GONE
            } else {
                card_bottom.visibility = View.VISIBLE
                bottom_divider.visibility = View.VISIBLE
            }
        }

    fun setCallbacks(callbacks: ProfileCardBgCallbacks) {
        this.callbacks = callbacks;
    }
}