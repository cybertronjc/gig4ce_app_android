package com.gigforce.common_ui.cells

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.gigforce.common_ui.R
import com.gigforce.common_ui.viewdatamodels.StandardActionCardDVM
import com.gigforce.core.IViewHolder
import com.gigforce.core.extensions.gone
import com.gigforce.core.extensions.visible
import kotlinx.android.synthetic.main.cell_standard_action_card.view.*

enum class ColorOptions(val value: Int) {

    Default(0),
    LightPink(201),
    LightBlue(202),
    Lipstick(203),
    GRAY(204);


    companion object {
        private val VALUES = values()
        fun getByValue(value: Int) = VALUES.first { it.value == value }
    }
}

open class StandardActionCardComponent(context: Context, attrs: AttributeSet?) :
        FrameLayout(context, attrs),
        IViewHolder {

    private var buttonClickListener: OnClickListener? = null
    private var secondButtonClickListener: OnClickListener? = null
    private var colorOption: ColorOptions = ColorOptions.Default

    init {
        this.layoutParams =
                LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        LayoutInflater.from(context).inflate(R.layout.cell_standard_action_card, this, true)

        attrs?.let {
            val styledAttributeSet = context.obtainStyledAttributes(it, R.styleable.StandardActionCardComponent, 0, 0)
            this.colorOption = ColorOptions.getByValue(styledAttributeSet.getInt(R.styleable.StandardActionCardComponent_colorOptions, 0))
//            var titleColor = styledAttributeSet.getColor(R.styleable.StandardActionCardComponent_titleTextColor, 0)
//            if(titleColor!=0){
//                tv_title.setTextColor(titleColor)
//            }
//            subtitle.setTextColor(styledAttributeSet.getColor(R.styleable.StandardActionCardComponent_subtitleTextColor, 0))
            backgroundColor = this.colorOption
        }

        primary_action.setOnClickListener {
            buttonClickListener?.onClick(it)
        }
        secondary_action.setOnClickListener {
            secondButtonClickListener?.onClick(it)
        }
    }

    var backgroundColor: ColorOptions
        get() = backgroundColor
        set(value) {
            val selectedColor = when (value) {
                ColorOptions.LightPink -> R.color.light_pink
                ColorOptions.LightBlue -> R.color.light_blue
                ColorOptions.Lipstick -> R.color.lipstick
                ColorOptions.GRAY -> R.color.grey
                else -> R.color.white
            }
            setBackgroundColor(ContextCompat.getColor(context, selectedColor))
        }

    fun setPrimaryActionClick(buttonClickListener: OnClickListener) {
        this.buttonClickListener = buttonClickListener
    }

    fun setSecondryActionClick(secondButtonClickListener: OnClickListener) {
        this.secondButtonClickListener = secondButtonClickListener
    }

    override fun bind(data: Any?) {
        if (data is StandardActionCardDVM) {
            /*if (data.image is String && (data.image as String).contains("http")) {
                Glide.with(context)
                        .load(data.image as String)
                        .into(image)
            } else */if (data.image is Int) {
                image.setImageResource(data.image as Int)
            } else {
            }
            tv_title.text = data.title
            tv_desc.text = data.subtitle

            if (data.action.isNotBlank()) {
                primary_action.text = data.action
            } else primary_action.gone()

            if (data.secondAction.isNotBlank()) {
                secondary_action.visible()
                secondary_action.text = data.secondAction
            } else secondary_action.gone()
        }
    }

}
