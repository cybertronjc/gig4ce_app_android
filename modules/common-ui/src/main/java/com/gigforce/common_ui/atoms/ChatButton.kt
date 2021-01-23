package com.gigforce.common_ui.atoms

import android.content.Context
import android.util.AttributeSet
import android.widget.ImageButton
import androidx.appcompat.widget.AppCompatImageButton
import com.gigforce.common_ui.R

class ChatButton(context: Context, attrs: AttributeSet?):
    AppCompatImageButton(context, attrs, R.attr.testButtonStyle){

    init {
        this.setOnClickListener {
            // setup Navigation

        }
    }
}

class IconButton(context: Context, attrs: AttributeSet?) : AppCompatImageButton(context, attrs) {

    companion object{
        fun d
    }

}