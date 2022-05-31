package com.gigforce.app.tl_work_space.activity_tacker.attendance_details.views

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.core.content.res.ResourcesCompat
import com.gigforce.app.tl_work_space.R
import com.gigforce.app.tl_work_space.databinding.LayoutAttendanceInfoBinding
import com.gigforce.common_ui.TextDrawable
import com.gigforce.app.data.repositoriesImpl.gigs.models.GigAttendanceData
import com.gigforce.core.extensions.capitalizeWords
import com.google.android.material.card.MaterialCardView

class AttendanceDetailsInfoLayout(
    context: Context,
    attrs: AttributeSet?
) : MaterialCardView(
    context,
    attrs
) {
    private lateinit var viewBinding: LayoutAttendanceInfoBinding

    init {
        setDefault()
        inflate()
        cardElevation = 0.0f
    }

    private fun setDefault() {
        val params = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
        this.layoutParams = params
    }

    private fun inflate() {
        viewBinding = LayoutAttendanceInfoBinding.inflate(
            LayoutInflater.from(context),
            this,
            true
        )
    }

    fun bind(
        data: GigAttendanceData
    ) = viewBinding.apply {

        this.businessNameTextview.text = data.businessName

        if (!data.businessLogo.isNullOrBlank()) {
            this.businessImageIv.loadImageIfUrlElseTryFirebaseStorage(
                data.businessLogo ?: ""
            )
        } else {
            val drawable = TextDrawable.builder().buildRound(
                data.businessName.capitalizeWords().get(0).toString(),
                ResourcesCompat.getColor(context.resources, R.color.lipstick, null)
            )
            this.businessImageIv.setImageDrawable(drawable)
        }

        this.jobProfileLayout.apply {
            imageView.loadImage(R.drawable.ic_breifcase_grey_gigs)
            titleTextView.text = "Job Profile"
            valueTextView.text = ": ${data.gigerDesignation}"
        }

        this.joiningDateLayout.apply {
            imageView.loadImage(R.drawable.ic_calendar_grey_gigs)
            titleTextView.text = "Joining Date"
            valueTextView.text = ": ${data.joiningDate}"
        }

        this.locationLayout.apply {
            imageView.loadImage(R.drawable.ic_location_grey_gigs)
            titleTextView.text = "Location"
            valueTextView.text = ": ${data.location}"
        }

        this.clientIdLayout.apply {
            imageView.loadImage(R.drawable.ic_manager_grey_gigs)
            titleTextView.text = "Client ID"
            valueTextView.text = ": ${data.clientId}"
        }

        this.scoutLayout.apply {
            imageView.loadImage(R.drawable.ic_user_grey_gigs)
            titleTextView.text = "Scout"
            valueTextView.text = ": ${data.scoutName}"
        }
    }
}