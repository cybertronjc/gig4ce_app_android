package com.gigforce.app.android_common_ui.molecules

import android.content.Context
import android.util.AttributeSet
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextSwitcher
import android.widget.TextView
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.ViewCompat
import com.gigforce.app.android_common_ui.R
import com.gigforce.app.android_common_utils.extensions.dpToPx
import com.google.android.material.shape.CornerFamily
import com.google.android.material.shape.MaterialShapeDrawable
import com.google.android.material.shape.ShapeAppearanceModel

class TextViewWithRiseAndDipIndicator(
    context: Context,
    attrs: AttributeSet
) : LinearLayout(
    context,
    attrs
) {
    private lateinit var textSwitcher: TextSwitcher
    private lateinit var riseDipIndicatorImageView: ImageView

    init {
        val inflatedView = LayoutInflater.from(context).inflate(
            R.layout.layout_text_with_rise_dip_indicator,
            this,
            true
        )


        findViews(inflatedView)
        setupTextSwitcher(textSwitcher)
    }

    private fun setupTextSwitcher(textSwitcher: TextSwitcher) {

        textSwitcher.setFactory {
            inflate(
                context,
                R.layout.layout_text_with_rise_dip_textswitcher_textview,
                null
            ) as TextView
        }
    }


    private fun findViews(inflatedView: View?) = inflatedView?.let {
        textSwitcher = it.findViewById(R.id.textSwitcher)
        riseDipIndicatorImageView = it.findViewById(R.id.indicator_image_view)
    }


    fun setText(
        text: String,
        @ColorRes textColor: Int,
        @ColorRes backgroundColor: Int,
        @DrawableRes indicatorDrawable: Int,
        @ColorRes indicatorDrawableTint: Int,
    ) {

        val textView = getTextViewFromTextSwitcher()
        val animate = shouldAnimateTextChange(
            textView,
            text
        )
        setUpTextView(
            textView,
            textColor,
            backgroundColor,
            indicatorDrawable,
            indicatorDrawableTint
        )

        if (animate) {
            textSwitcher.setText(text)
        } else {
            textSwitcher.setCurrentText(text)
        }
    }

    private fun setUpTextView(
        textview: TextView,
        @ColorRes textColor: Int,
        @ColorRes backgroundColor: Int,
        @DrawableRes indicatorDrawable: Int,
        @ColorRes indicatorDrawableTint: Int
    ) {
        setTextColor(textview, textColor)
        setBackgroundShapeColor(backgroundColor)
        setIndicatorDrawable(indicatorDrawable, indicatorDrawableTint)
    }

    private fun setBackgroundShapeColor(
        @ColorRes color: Int
    ) {
        val radius = resources.getDimension(R.dimen.layout_rise_dip_corner_size)
        val shapeAppearanceModel = ShapeAppearanceModel().toBuilder()
            .setAllCorners(CornerFamily.ROUNDED, radius)
            .build()
        val materialShapeDrawable = MaterialShapeDrawable(shapeAppearanceModel).apply {
            fillColor = ContextCompat.getColorStateList(
                context,
                color
            )
        }
        ViewCompat.setBackground(this, materialShapeDrawable)
    }

    fun setTextColor(
        textview: TextView,
        @ColorRes textColor: Int,
    ) {
        textview.setTextColor(
            ResourcesCompat.getColor(resources, textColor, null)
        )
    }

    fun setIndicatorDrawable(
        @DrawableRes drawable: Int,
        @ColorRes indicatorDrawableTint: Int
    ) {
        riseDipIndicatorImageView.setImageResource(drawable)
        //  riseDipIndicatorImageView.setColorFilter(indicatorDrawableTint)
    }

    private fun shouldAnimateTextChange(
        textView: TextView,
        newText: String
    ): Boolean {
        return if (textView.text.isNullOrBlank())
            false
        else
            textView.text != newText
    }

    private fun getTextViewFromTextSwitcher(): TextView {
        return textSwitcher.findViewById(
            R.id.textView
        )
    }

    fun showTextWithRiseIndicator(
        riseString: String
    ) {
        setText(
            text = riseString,
            textColor = R.color.green_1,
            backgroundColor = R.color.green_8,
            indicatorDrawable = R.drawable.ic_rise_green,
            indicatorDrawableTint = 0
        )
    }

    fun showTextWithDipIndicator(
        dipString: String
    ) {
        setText(
            text = dipString,
            textColor = R.color.red_1,
            backgroundColor = R.color.red_8,
            indicatorDrawable = R.drawable.ic_dip_red,
            indicatorDrawableTint = 0
        )
    }
}