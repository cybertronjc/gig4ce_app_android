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


        this.activeRidersLayout.countEt.setText((businessDataItem.totalActive ?: 0).toString())
        this.loginLayout.countEt.setText((businessDataItem.loginToday ?: 0).toString())
        this.absentLayout.countEt.setText((businessDataItem.absentToday ?: 0).toString())
        this.resignedLayout.countEt.setText((businessDataItem.resignedToday ?: 0).toString())
        this.newOnboardingLayout.countEt.setText(
            (businessDataItem.newOnboardingToday ?: 0).toString()
        )
        this.inTrainingLayout.countEt.setText((businessDataItem.inTrainingToday ?: 0).toString())
        this.newLoginLayout.countEt.setText((businessDataItem.newLoginToday ?: 0).toString())
        this.openPositionsLayout.countEt.setText((businessDataItem.openPositions ?: 0).toString())
        this.tomorrowLineupLayout.countEt.setText(
            (businessDataItem.totalLineupsForTomorrow ?: 0).toString()
        )
        this.expectedLoginTommorrowLayout.countEt.setText(
            (businessDataItem.expectedLoginsTomorrow ?: 0).toString()
        )
    }
}