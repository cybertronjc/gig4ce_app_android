package com.app.user_profile.components

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.util.AttributeSet
import android.view.View
import android.widget.ArrayAdapter
import android.widget.FrameLayout
import com.app.user_profile.R
import com.tokenautocomplete.TokenCompleteTextView

class ChipsEditText : FrameLayout {
    init {
        View.inflate(context, R.layout.chips_et_layout, this)
    }

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    constructor(
        context: Context,
        attrs: AttributeSet?,
        defStyleAttr: Int,
        defStyleRes: Int
    ) : super(context, attrs, defStyleAttr, defStyleRes)

    fun setAdapter(items: List<String>) {
        findViewById<com.app.user_profile.components.ChipCollectionView>(R.id.cmp_chip_et).setAdapter(
            ArrayAdapter<String>(
                context,
                android.R.layout.simple_list_item_1,
                items
            )
        )
        findViewById<com.app.user_profile.components.ChipCollectionView>(R.id.cmp_chip_et).threshold = 1
        findViewById<com.app.user_profile.components.ChipCollectionView>(R.id.cmp_chip_et).setTokenClickStyle(TokenCompleteTextView.TokenClickStyle.Select)
    }

    fun setTextColor(color: String) {
        findViewById<com.app.user_profile.components.ChipCollectionView>(R.id.cmp_chip_et).setTextColor(Color.parseColor(color))
    }

    fun setHint(hint: String) {
        findViewById<com.app.user_profile.components.ChipCollectionView>(R.id.cmp_chip_et).hint = hint
    }

    fun setHintTextColor(color: String) {
        findViewById<com.app.user_profile.components.ChipCollectionView>(R.id.cmp_chip_et).setHintTextColor(Color.parseColor(color))
    }

    fun setBackgroundTint(color: String) {
        findViewById<com.app.user_profile.components.ChipCollectionView>(R.id.cmp_chip_et).backgroundTintList =
            ColorStateList.valueOf(Color.parseColor(color));

    }

    fun setTextSize(pixelSize: Float) {
        findViewById<com.app.user_profile.components.ChipCollectionView>(R.id.cmp_chip_et).textSize = pixelSize
    }

    fun setContentPadding(
        paddingLeft: Int = 0,
        paddingRight: Int = 0,
        paddingTop: Int = 0,
        paddingBottom: Int = 0
    ) {
        findViewById<com.app.user_profile.components.ChipCollectionView>(R.id.cmp_chip_et).setPadding(
            paddingLeft,
            paddingTop,
            paddingRight,
            paddingBottom
        )
    }

}