package com.gigforce.common_ui.atoms

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import androidx.appcompat.widget.AppCompatImageButton
import com.gigforce.common_ui.R

enum class IconType(val value: Int){

    Default(0),
    Chat(101),
    Home(102),
    Pin(103),
    Profile(104);

    companion object {
        private val VALUES = values();
        fun getByValue(value: Int) = VALUES.first { it.value == value }
    }
}

open class IconButton : AppCompatImageButton {

    private var _iconType:IconType = IconType.Default

    constructor(context: Context, attrs: AttributeSet?):super(context, attrs, R.attr.iconButtonStyle){
        // extract attrs
        attrs ?. let {
            val styledAttributeSet = context.obtainStyledAttributes(it, R.styleable.IconButton, R.attr.iconButtonStyle, R.style.IconButtonStyle)
            this.iconType = IconType.getByValue(styledAttributeSet.getInt(R.styleable.IconButton_iconType,0))
            styledAttributeSet.recycle()
        }
    }

    constructor(_iconType: IconType, context: Context, attrs: AttributeSet? = null):this(context, attrs){
        this.iconType = _iconType
    }

    var iconType:IconType
        get() = _iconType
        set(value) {
            Log.i("iconbutton", "Setting Value as ${value}")
            this._iconType = value
            val iconRes = when(value){
                IconType.Chat -> R.drawable.ic_homescreen_chat
                IconType.Profile -> R.drawable.profile_avatar
                else -> R.drawable.ic_tip
            }

            this.setImageResource(iconRes)
        }
}