package com.gigforce.giger_gigs.attendance_tl.attendance_details.views

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.core.content.res.ResourcesCompat
import com.gigforce.common_ui.ext.formatToCurrency
import com.gigforce.common_ui.viewmodels.payouts.Payout
import com.gigforce.giger_gigs.R
import com.gigforce.giger_gigs.databinding.LayoutAttendanceInfoBinding
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
        data: Payout
    ) = viewBinding.apply {
        this.jobProfileLayout.apply {
            imageView.loadImage(R.drawable.ic_breifcase_grey_gigs)
            titleTextView.text = "Job Profile"
            valueTextView.text = ": ${data.amount.formatToCurrency()}"
        }

        this.joiningDateLayout.apply {
            imageView.loadImage(R.drawable.ic_calendar_grey_gigs)
            titleTextView.text = "Joining Date"
            valueTextView.text = ": ${data.getPaidOnDateString()}"
        }

        this.locationLayout.apply {
            imageView.loadImage(R.drawable.ic_location_grey_gigs)
            titleTextView.text = "Location"
            valueTextView.text = ": ${data.utrNo ?: "-"}"
        }

        this.clientIdLayout.apply {
            imageView.loadImage(R.drawable.ic_manager_grey_gigs)
            titleTextView.text = "Client ID"
            valueTextView.text = ": ${data.category?.capitalize() ?: "-"}"
        }

        this.scoutLayout.apply {
            imageView.loadImage(R.drawable.ic_user_grey_gigs)
            titleTextView.text = "Scout"
            valueTextView.text = ": ${data.payOutCycle ?: "-"}"
        }
    }
}