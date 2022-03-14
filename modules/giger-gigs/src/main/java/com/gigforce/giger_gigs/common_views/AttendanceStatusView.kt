package com.gigforce.giger_gigs.common_views

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.drawable.DrawableCompat
import com.gigforce.giger_gigs.R

class AttendanceStatusView(
    context: Context,
    attrs: AttributeSet?
) : RelativeLayout(
    context,
    attrs
) {

    private lateinit var textView: TextView

    init {
        setDefault()
        inflate()
    }

    private fun setDefault() {
        val params = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
        this.layoutParams = params
    }

    private fun inflate() {
        val view = LayoutInflater.from(
            context
        ).inflate(R.layout.layout_attendance_status, this, true)

        textView = view.findViewById(R.id.statusTextView)
    }

    fun bind(
        status: String,
        statusBackGroundColorCode: String,
        statusTextColorCode : String
    ) {
        textView.text = status

        var background = textView.background
        background = DrawableCompat.wrap(background)

        DrawableCompat.setTint(background, Color.parseColor(statusBackGroundColorCode))
        textView.background = background
        textView.setTextColor(Color.parseColor(statusTextColorCode))
    }
}
