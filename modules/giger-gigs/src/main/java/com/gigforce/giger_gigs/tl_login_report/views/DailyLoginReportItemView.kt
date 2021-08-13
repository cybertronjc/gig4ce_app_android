package com.gigforce.giger_gigs.tl_login_report.views

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
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

            this.activeRidersLayout.titleTextview.text = "Active riders/gigers"
            this.loginLayout.titleTextview.text = "Login"
            this.absentLayout.titleTextview.text = "Absent"
            this.resignedLayout.titleTextview.text = "Resigned"
            this.newOnboardingLayout.titleTextview.text = "New onboarding"
            this.inTrainingLayout.titleTextview.text = "In-training"
            this.newLoginLayout.titleTextview.text = "New login"
            this.openPositionsLayout.titleTextview.text = "Open positions"
            this.tomorrowLineupLayout.titleTextview.text = "Lineup for tomorrow/ Next day"
            this.expectedLoginTommorrowLayout.titleTextview.text =
                "Expected logins tomorrow/ Next day"
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