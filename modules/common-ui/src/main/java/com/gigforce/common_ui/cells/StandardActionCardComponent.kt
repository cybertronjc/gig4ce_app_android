package com.gigforce.common_ui.cells

import android.content.Context
import android.content.res.Resources
import android.util.AttributeSet
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import com.bumptech.glide.Glide
import com.gigforce.common_ui.R
import com.gigforce.common_ui.viewdatamodels.StandardActionCardDVM
import com.gigforce.core.IViewHolder
import com.gigforce.core.extensions.gone
import com.gigforce.core.extensions.visible
import com.google.android.material.button.MaterialButton
import kotlin.math.roundToInt


open class StandardActionCardComponent(context: Context, attrs: AttributeSet?) :
    FrameLayout(context, attrs),
    IViewHolder {
    private var cv_top: ConstraintLayout
    private var tv_title: TextView
    private var tv_subtitle: TextView
    private var tv_cta: StandardTextActionButtonComponent
    private var tv_cta1: MaterialButton
    private val img: ImageView
    private var buttonClickListener : OnClickListener? = null
    private var secondButtonClickListener : OnClickListener? = null

    init {
        this.layoutParams =
            LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        LayoutInflater.from(context).inflate(R.layout.cell_standard_action_card, this, true)
        cv_top = this.findViewById(R.id.cv_top)
        img = this.findViewById(R.id.iv_icon)
        tv_title = this.findViewById(R.id.tv_title)
        tv_subtitle = this.findViewById(R.id.tv_desc)
        tv_cta = this.findViewById(R.id.cta)
        tv_cta1 = this.findViewById(R.id.cta1)


        tv_cta.setOnClickListener{
            buttonClickListener?.onClick(it)
        }
        tv_cta1.setOnClickListener{
            secondButtonClickListener?.onClick(it)
        }
    }

    fun setButtonClick(buttonClickListener: OnClickListener){
        this.buttonClickListener = buttonClickListener
    }

    fun secondButtonClick(secondButtonClickListener: OnClickListener){
        this.secondButtonClickListener = secondButtonClickListener
    }

    var titleColor: Int
        get() = titleColor
        set(value) {
            tv_title.setTextColor(value)
        }

    var subtitleColor: Int
        get() = subtitleColor
        set(value) {
            tv_subtitle.setTextColor(value)
        }

    var applyMargin: Boolean
        get() = applyMargin
        set(value) {
            val params =
                LayoutParams(
                    LayoutParams.MATCH_PARENT,
                    LayoutParams.WRAP_CONTENT
                )
            val left: Int = getPixelValue(16)//context.resources.getDimension(R.dimen.size4))
            val top: Int = getPixelValue(16)
            val right: Int = getPixelValue(16)
            val bottom: Int = getPixelValue(16)
            params.setMargins(left, top, right, bottom)
            layoutParams = params
        }

    fun getPixelValue(value: Int): Int {
        return (value * Resources.getSystem().displayMetrics.density).toInt()
    }


    override fun bind(data: Any?) {
        if (data is StandardActionCardDVM) {
            if (data.image is String && (data.image as String).contains("http")) {
                Glide.with(context)
                    .load(data.image as String)
                    .into(img)
            } else if (data.image is Int) {
                img.setImageResource(data.image as Int)
            } else {
            }
            tv_title.text = data.title
            tv_subtitle.text = data.subtitle

            if (data.action.isNotBlank()) {
                tv_cta.text = data.action
            } else tv_cta.gone()

            if (data.secondAction.isNotBlank()) {
                tv_cta1.visible()
                tv_cta1.text = data.secondAction
            } else tv_cta1.gone()
        }
    }

}
