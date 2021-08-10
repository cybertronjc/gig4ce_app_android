package com.gigforce.giger_gigs.tl_login_report.views

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import com.gigforce.core.IViewHolder
import com.gigforce.core.extensions.onTextChanged
import com.gigforce.giger_gigs.databinding.ItemViewTlLoginReportBinding
import com.gigforce.giger_gigs.databinding.LayoutBusinessNameRecyclerItemBinding
import com.gigforce.giger_gigs.models.BusinessDataItem
import com.gigforce.giger_gigs.models.BusinessListRecyclerItemData
import kotlinx.android.synthetic.main.gig_details_item.view.*

class DailyLoginReportItemView(
    context: Context,
    attrs: AttributeSet?
) : FrameLayout(
    context,
    attrs
) {

    private  var viewBinding: ItemViewTlLoginReportBinding = ItemViewTlLoginReportBinding.inflate(
        LayoutInflater.from(context),
        this,
        true
    )

    fun disableEdit(){
        viewBinding.absentEt.disbale()
    }

    fun showData(businessDataItem: BusinessDataItem) = viewBinding.apply{


        businessName.text = businessDataItem.businessName
        dailyReport.text = "Daily report of ${businessDataItem.businessName}"

        activeRidersEt.setText(businessDataItem.totalActive.toString())
        loginEt.setText(businessDataItem.loginToday.toString())
        absentEt.setText(businessDataItem.absentToday.toString())
        resignedEt.setText(businessDataItem.resignedToday.toString())
        newOnboardingEt.setText(businessDataItem.newOnboardingToday.toString())
        inTrainingEt.setText(businessDataItem.inTrainingToday.toString())
        newLoginEt.setText(businessDataItem.newLoginToday.toString())
        openPositionsEt.setText(businessDataItem.openPositions.toString())
        lineUpForTomorrowEt.setText(businessDataItem.totalLineupsForTomorrow.toString())
        expectedLoginsForTomorrowEt.setText(businessDataItem.expectedLoginsTomorrow.toString())
    }

    fun getDailyReportItem() : BusinessDataItem {
        val activeRiders = viewBinding.activeRidersEt.text.toString().toIntOrNull() ?: 0
        val login = viewBinding.loginEt.text.toString().toIntOrNull() ?: 0
        val absent = viewBinding.absentEt.text.toString().toIntOrNull() ?: 0
        val resigned = viewBinding.resignedEt.text.toString().toIntOrNull() ?: 0
        val newOnboarding = viewBinding.newOnboardingEt.text.toString().toIntOrNull() ?: 0
        val inTraining = viewBinding.inTrainingEt.text.toString().toIntOrNull() ?: 0
        val newLogin = viewBinding.newLoginEt.text.toString().toIntOrNull() ?: 0
        val openPositions = viewBinding.openPositionsEt.text.toString().toIntOrNull() ?: 0
        val lineUpsForTomorrow = viewBinding.lineUpForTomorrowEt.text.toString().toIntOrNull() ?: 0
        val expectedForTommorrow = viewBinding.expectedLoginsForTomorrowEt.text.toString().toIntOrNull() ?: 0

        return BusinessDataItem(
            newLoginToday = newLogin,
            openPositions = openPositions,
            expectedLoginsTomorrow = expectedForTommorrow,
            absentToday = absent,
            city = null,
            businessId = null,
            businessName = null,
            totalActive = activeRiders,
            newOnboardingToday = newOnboarding,
            loginToday = login,
            legalName = null,
            resignedToday = resigned,
            inTrainingToday = inTraining,
            totalLineupsForTomorrow = lineUpsForTomorrow
        )
    }
}