package com.app.user_profile.components

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.DimenRes
import androidx.annotation.StringRes
import androidx.constraintlayout.widget.ConstraintLayout
import com.app.user_profile.R

typealias ClickHandler = (AddContentCard) -> Unit

class AddContentCard @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {

    @get:DimenRes
    var topIcon: Int? = null
        set(value) {
            field = value
            findViewById<ImageView>(R.id.iv_content_top_icon_profile_v2).setImageResource(value!!)
        }

    @get:DimenRes
    var contentIllustration: Int? = null
        set(value) {
            field = value
            findViewById<ImageView>(R.id.iv_content_illustration_profile_v2).setImageResource(value!!)
        }

    @get:StringRes
    var topLabel: Int? = null
        set(value) {
            field = value
            findViewById<TextView>(R.id.tv_content_top_profile_v2).text = resources.getString(value!!)
        }

    @get:StringRes
    var contentHeading: Int? = null
        set(value) {
            field = value
            findViewById<TextView>(R.id.tv_content_heading_profile_v2).text = resources.getString(value!!)
        }

    @get:StringRes
    var contentText: Int? = null
        set(value) {
            field = value
            findViewById<TextView>(R.id.tv_content_text_profile_v2).text = resources.getString(value!!)
        }

    @get:StringRes
    var rightActionText: Int? = null
        set(value) {
            field = value
            findViewById<TextView>(R.id.tv_add_now_profile_v2).text = resources.getString(value!!)
        }

    fun setRightClickAction(clickHandler: ClickHandler?) {
        findViewById<TextView>(R.id.tv_add_now_profile_v2).setOnClickListener {
            clickHandler?.let {
                clickHandler(this)
            }
        }
    }


    init {
        View.inflate(context, R.layout.layout_add_content, this)
    }

}