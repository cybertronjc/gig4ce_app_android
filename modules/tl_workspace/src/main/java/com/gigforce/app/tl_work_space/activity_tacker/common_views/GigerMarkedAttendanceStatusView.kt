package com.gigforce.app.tl_work_space.activity_tacker.common_views

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.isVisible
import com.gigforce.app.tl_work_space.R
import com.gigforce.app.data.repositoriesImpl.gigs.AttendanceStatus
import java.time.LocalDateTime

class GigerMarkedAttendanceStatusView(
    context: Context,
    attrs: AttributeSet?
) : RelativeLayout(
    context,
    attrs
) {

    private lateinit var linearLayout: LinearLayout
    private lateinit var markingTimeTV : TextView
    private lateinit var statusTextView : TextView
    private lateinit var resolveButton: View
    private lateinit var progressBar : ProgressBar

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
        ).inflate(R.layout.layout_giger_attendance_status, this, true)

        linearLayout = view.findViewById(R.id.root_layout)
        markingTimeTV = view.findViewById(R.id.giger_attendance_time_status)
        statusTextView = view.findViewById(R.id.giger_attendance_status)
        resolveButton = view.findViewById(R.id.resolve_btn)
        progressBar = view.findViewById(R.id.progress_bar)
    }

    fun bind(
        status: String,
        markingTime: LocalDateTime? = null,
        showResolveButton: Boolean,
        showMarkAttendanceProgressBar : Boolean
    ) {
        statusTextView.text = status
        if(markingTime == null){
            markingTimeTV.text = null
        } else{
            markingTimeTV.text = "at -"
        }

        val textColor = when (status) {
            AttendanceStatus.PRESENT -> R.color.text_green
            AttendanceStatus.ABSENT -> R.color.lipstick_2
            else -> R.color.black
        }
        statusTextView.setTextColor(ResourcesCompat.getColor(resources,textColor,null))

        val backgroundColor = when (status) {
            AttendanceStatus.PRESENT -> R.color.text_green_10
            AttendanceStatus.ABSENT -> R.color.lipstick_10
            else -> R.color.lipstick_10
        }
        linearLayout.setBackgroundColor(
            ResourcesCompat.getColor(resources,backgroundColor,null)
        )

        resolveButton.isVisible = showResolveButton
        progressBar.isVisible = showMarkAttendanceProgressBar
    }

    fun setOnResolveButtonClickListener(
        listener : View.OnClickListener
    ){
        resolveButton.setOnClickListener(listener)
    }
}
