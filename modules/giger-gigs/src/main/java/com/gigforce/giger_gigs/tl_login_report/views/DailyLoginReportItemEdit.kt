package com.gigforce.giger_gigs.tl_login_report.views

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import com.gigforce.core.extensions.gone
import com.gigforce.core.extensions.visible
import com.gigforce.giger_gigs.R
import com.gigforce.giger_gigs.databinding.ItemViewTlLoginReportEditBinding
import com.gigforce.giger_gigs.models.BusinessData
import com.gigforce.giger_gigs.models.BusinessDataItem

class DailyLoginReportItemEdit(
    context: Context,
    attrs: AttributeSet?
) : FrameLayout(
    context,
    attrs
) {
    private var viewBinding: ItemViewTlLoginReportEditBinding =
        ItemViewTlLoginReportEditBinding.inflate(
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

            this.activeRidersLayout.enterQuantity.setOnClickListener{
                showEditTextAndEnterBtn(it)
            }
            this.loginLayout.enterQuantity.setOnClickListener{
                showEditTextAndEnterBtn(it)
            }
            this.absentLayout.enterQuantity.setOnClickListener{
                showEditTextAndEnterBtn(it)
            }
            this.resignedLayout.enterQuantity.setOnClickListener{
                showEditTextAndEnterBtn(it)
            }
            this.newOnboardingLayout.enterQuantity.setOnClickListener{
                showEditTextAndEnterBtn(it)
            }
            this.inTrainingLayout.enterQuantity.setOnClickListener{
                showEditTextAndEnterBtn(it)
            }
            this.newLoginLayout.enterQuantity.setOnClickListener{
                showEditTextAndEnterBtn(it)
            }
            this.openPositionsLayout.enterQuantity.setOnClickListener{
                showEditTextAndEnterBtn(it)
            }
            this.tomorrowLineupLayout.enterQuantity.setOnClickListener{
                showEditTextAndEnterBtn(it)
            }
            this.expectedLoginTommorrowLayout.enterQuantity.setOnClickListener{
                showEditTextAndEnterBtn(it)
            }
        }
    }

    private fun showEditTextAndEnterBtn(it: View?) {
        val view = it ?: return
        view.gone()
        (view.parent as View).findViewById<View>(R.id.count_et).visible()
    }


    fun showData(businessDataItem: BusinessData) = viewBinding.apply {

        if(businessDataItem.totalActive != null ){
            this.activeRidersLayout.enterQuantity.gone()
            this.activeRidersLayout.countEt.visible()
            this.activeRidersLayout.countEt.setText(businessDataItem.totalActive.toString())
        } else {
            this.activeRidersLayout.countEt.setText("")
            this.activeRidersLayout.countEt.gone()
            this.activeRidersLayout.enterQuantity.visible()
        }

        if(businessDataItem.loginToday != null ){
            this.loginLayout.enterQuantity.gone()
            this.loginLayout.countEt.visible()
            this.loginLayout.countEt.setText(businessDataItem.loginToday.toString())
        } else {
            this.loginLayout.countEt.setText("")
            this.loginLayout.countEt.gone()
            this.loginLayout.enterQuantity.visible()
        }

        if(businessDataItem.absentToday != null ){
            this.absentLayout.enterQuantity.gone()
            this.absentLayout.countEt.visible()
            this.absentLayout.countEt.setText(businessDataItem.absentToday.toString())
        } else {
            this.absentLayout.countEt.setText("")
            this.absentLayout.countEt.gone()
            this.absentLayout.enterQuantity.visible()
        }

        if(businessDataItem.resignedToday != null ){
            this.resignedLayout.enterQuantity.gone()
            this.resignedLayout.countEt.visible()
            this.resignedLayout.countEt.setText(businessDataItem.resignedToday.toString())
        } else {
            this.resignedLayout.countEt.setText("")
            this.resignedLayout.countEt.gone()
            this.resignedLayout.enterQuantity.visible()
        }

        if(businessDataItem.newOnboardingToday != null ){
            this.newOnboardingLayout.enterQuantity.gone()
            this.newOnboardingLayout.countEt.visible()
            this.newOnboardingLayout.countEt.setText(businessDataItem.newOnboardingToday.toString())
        } else {
            this.newOnboardingLayout.countEt.setText("")
            this.newOnboardingLayout.countEt.gone()
            this.newOnboardingLayout.enterQuantity.visible()
        }

        if(businessDataItem.inTrainingToday != null ){
            this.inTrainingLayout.enterQuantity.gone()
            this.inTrainingLayout.countEt.visible()
            this.inTrainingLayout.countEt.setText(businessDataItem.inTrainingToday.toString())
        } else {
            this.inTrainingLayout.countEt.setText("")
            this.inTrainingLayout.countEt.gone()
            this.inTrainingLayout.enterQuantity.visible()
        }

        if(businessDataItem.newLoginToday != null ){
            this.newLoginLayout.enterQuantity.gone()
            this.newLoginLayout.countEt.visible()
            this.newLoginLayout.countEt.setText(businessDataItem.newLoginToday.toString())
        } else {
            this.newLoginLayout.countEt.setText("")
            this.newLoginLayout.countEt.gone()
            this.newLoginLayout.enterQuantity.visible()
        }

        if(businessDataItem.openPositions != null ){
            this.openPositionsLayout.enterQuantity.gone()
            this.openPositionsLayout.countEt.visible()
            this.openPositionsLayout.countEt.setText(businessDataItem.openPositions.toString())
        } else {
            this.openPositionsLayout.countEt.setText("")
            this.openPositionsLayout.countEt.gone()
            this.openPositionsLayout.enterQuantity.visible()
        }

        if(businessDataItem.totalLineupsForTomorrow != null ){
            this.tomorrowLineupLayout.enterQuantity.gone()
            this.tomorrowLineupLayout.countEt.visible()
            this.tomorrowLineupLayout.countEt.setText(businessDataItem.totalLineupsForTomorrow.toString())
        } else {
            this.tomorrowLineupLayout.countEt.setText("")
            this.tomorrowLineupLayout.countEt.gone()
            this.tomorrowLineupLayout.enterQuantity.visible()
        }

        if(businessDataItem.expectedLoginsTomorrow != null ){
            this.expectedLoginTommorrowLayout.enterQuantity.gone()
            this.expectedLoginTommorrowLayout.countEt.visible()
            this.expectedLoginTommorrowLayout.countEt.setText(businessDataItem.expectedLoginsTomorrow.toString())
        } else {
            this.expectedLoginTommorrowLayout.countEt.setText("")
            this.expectedLoginTommorrowLayout.countEt.gone()
            this.expectedLoginTommorrowLayout.enterQuantity.visible()
        }
    }

    fun getDailyReportItem(): BusinessDataItem {
        val activeRiders = viewBinding.activeRidersLayout.countEt.getTextAsIntOrNull()
        val login = viewBinding.loginLayout.countEt.getTextAsIntOrNull()
        val absent = viewBinding.absentLayout.countEt.getTextAsIntOrNull()
        val resigned = viewBinding.resignedLayout.countEt.getTextAsIntOrNull()
        val newOnboarding = viewBinding.newOnboardingLayout.countEt.getTextAsIntOrNull()
        val inTraining = viewBinding.inTrainingLayout.countEt.getTextAsIntOrNull()
        val newLogin = viewBinding.newLoginLayout.countEt.getTextAsIntOrNull()
        val openPositions = viewBinding.openPositionsLayout.countEt.getTextAsIntOrNull()
        val lineUpsForTomorrow = viewBinding.tomorrowLineupLayout.countEt.getTextAsIntOrNull()
        val expectedForTommorrow = viewBinding.openPositionsLayout.countEt.getTextAsIntOrNull()

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