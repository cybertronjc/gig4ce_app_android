package com.gigforce.app.tl_work_space.activity_tacker.common_views

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.ProgressBar
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.core.graphics.drawable.DrawableCompat
import androidx.core.view.isVisible
import com.gigforce.app.tl_work_space.R

class AttendanceStatusView(
    context: Context,
    attrs: AttributeSet?
) : RelativeLayout(
    context,
    attrs
) {

    private lateinit var textView: TextView
    private lateinit var progressBar: ProgressBar

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
        progressBar = view.findViewById(R.id.progress_bar)
    }

    fun bind(
        status: String,
        statusBackGroundColorCode: String,
        statusTextColorCode: String,
        showMarkAttendanceProgressBar: Boolean
    ) {


        var background = textView.background
        background = DrawableCompat.wrap(background)

        DrawableCompat.setTint(background, Color.parseColor(statusBackGroundColorCode))
        textView.background = background
        textView.setTextColor(Color.parseColor(statusTextColorCode))

        if (showMarkAttendanceProgressBar) {
            progressBar.isVisible = true
            textView.text = ""
        } else {
            progressBar.isVisible = false
            textView.text = status
        }
    }
}
