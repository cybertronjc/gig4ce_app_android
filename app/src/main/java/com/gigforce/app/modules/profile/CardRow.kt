package com.gigforce.app.modules.profile

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.LinearLayout
import com.gigforce.app.R
import kotlinx.android.synthetic.main.card_row.view.*

class CardRow : LinearLayout {
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    init {
        View.inflate(context, R.layout.card_row, this)
    }

    private var callbacks: CardRowCallbacks? = null
    var rowTitle: String = ""
        set(value) {
            field = value
            row_title.text = value
        }

    var rowContent: String = ""
        set(value) {
            field = value
            row_content.text = value
        }

    var rowLocation: String = ""
        set(value) {
            field = value
        }

    var showIsWhatsappCb: Boolean = false
        set(value) {
            field = value
            cb_is_whatsapp_number_card_row.visibility = if (value) View.VISIBLE else View.GONE
            if(value){
                setCbCheckedListener()
            }

        }

    fun setCbCheckedListener() {
        cb_is_whatsapp_number_card_row.setOnCheckedChangeListener { _, isChecked -> callbacks?.checked(isChecked, rowContent) }
    }

    fun setCallbacks(callbacks: CardRowCallbacks) {
        this.callbacks = callbacks
    }
}