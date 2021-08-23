package com.gigforce.giger_gigs.tl_login_report.views

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import com.gigforce.giger_gigs.R
import com.gigforce.giger_gigs.databinding.ItemViewTlLoginReportViewBinding
import com.gigforce.giger_gigs.models.BusinessData
import com.gigforce.giger_gigs.models.BusinessDataItem

class DailyLoginReportItemView(
    context: Context,
    attrs: AttributeSet?
) : FrameLayout(
    context,
    attrs
) {
    private var viewBinding: ItemViewTlLoginReportViewBinding =
        ItemViewTlLoginReportViewBinding.inflate(
            LayoutInflater.from(context),
            this,
            true
        )

    init {
        viewBinding.apply {

            this.activeRidersLayout.titleTextview.text = context.getString(R.string.active_riders)
            this.loginLayout.titleTextview.text = context.getString(R.string.login)
            this.absentLayout.titleTextview.text = context.getString(R.string.absent)
            this.resignedLayout.titleTextview.text = context.getString(R.string.resigned)
            this.newOnboardingLayout.titleTextview.text = context.getString(R.string.new_onboarding)
            this.inTrainingLayout.titleTextview.text = context.getString(R.string.in_training)
            this.newLoginLayout.titleTextview.text = context.getString(R.string.new_login)
            this.openPositionsLayout.titleTextview.text = context.getString(R.string.open_positions)
            this.tomorrowLineupLayout.titleTextview.text = context.getString(R.string.lineup_for_tomorrow)
            this.expectedLoginTommorrowLayout.titleTextview.text =
                context.getString(R.string.expencted_logins)
        }
    }


    fun showData(businessDataItem: BusinessData) = viewBinding.apply {


        this.activeRidersLayout.countEt.setText(
            formatData(businessDataItem.totalActive)
        )
        this.loginLayout.countEt.setText(
            formatData(businessDataItem.loginToday)
        )
        this.absentLayout.countEt.setText(
            formatData(businessDataItem.absentToday)
        )
        this.resignedLayout.countEt.setText(
            formatData(businessDataItem.resignedToday)
        )
        this.newOnboardingLayout.countEt.setText(
            formatData(businessDataItem.newOnboardingToday)
        )
        this.inTrainingLayout.countEt.setText(
            formatData(businessDataItem.inTrainingToday)
        )
        this.newLoginLayout.countEt.setText(
            formatData(businessDataItem.newLoginToday)
        )
        this.openPositionsLayout.countEt.setText(
            formatData(businessDataItem.openPositions)
        )
        this.tomorrowLineupLayout.countEt.setText(
            formatData(businessDataItem.totalLineupsForTomorrow)
        )
        this.expectedLoginTommorrowLayout.countEt.setText(
            formatData(businessDataItem.expectedLoginsTomorrow)
        )
    }

    private fun formatData(value : Int?) : String{
        val finalValue = value ?: return "-"
        return finalValue.toString()
    }
}