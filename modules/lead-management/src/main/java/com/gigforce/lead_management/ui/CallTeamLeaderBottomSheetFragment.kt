package com.gigforce.lead_management.ui

import android.os.Bundle
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import com.gigforce.common_ui.viewdatamodels.leadManagement.TeamLeader
import com.gigforce.core.base.BaseBottomSheetDialogFragment
import com.gigforce.lead_management.R
import com.gigforce.lead_management.databinding.FragmentCallTeamLeaderBottomSheetBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CallTeamLeaderBottomSheetFragment : BaseBottomSheetDialogFragment<FragmentCallTeamLeaderBottomSheetBinding>(
    fragmentName = "CallTeamLeaderBottomSheetFragment",
    layoutId = R.layout.fragment_call_team_leader_bottom_sheet
) {

    companion object {
        const val TAG = "DropSelectionFragment2"
        const val INTENT_RECRUITING_TL = "recruiting_tl"
        const val INTENT_REPORTING_TL = "reporting_tl"

        fun launch(
            recruitingTl: TeamLeader,
            reportingTl: TeamLeader,
            childFragmentManager : FragmentManager
        ){
            CallTeamLeaderBottomSheetFragment().apply {
                arguments = bundleOf(
                    INTENT_RECRUITING_TL to recruitingTl,
                    INTENT_REPORTING_TL to reportingTl
                    )
            }.show(childFragmentManager,TAG)
        }
    }

    private lateinit var recruitingTl: TeamLeader
    private lateinit var reportingTl: TeamLeader

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(DialogFragment.STYLE_NORMAL, R.style.DialogStyle)
        arguments?.let {
            recruitingTl = it.getParcelable<TeamLeader>(
                INTENT_RECRUITING_TL
            ) ?: return@let
            reportingTl = it.getParcelable<TeamLeader>(
                INTENT_REPORTING_TL
            ) ?: return@let
        }

        savedInstanceState?.let {
            recruitingTl = it.getParcelable<TeamLeader>(
                INTENT_RECRUITING_TL
            ) ?: return@let
            reportingTl = it.getParcelable<TeamLeader>(
                INTENT_REPORTING_TL
            ) ?: return@let
        }
        logDataReceivedFromBundles()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putParcelable(
            INTENT_RECRUITING_TL,
            recruitingTl
        )

        outState.putParcelable(
            INTENT_REPORTING_TL,
            reportingTl
        )
    }

    private fun logDataReceivedFromBundles() {
        if (::recruitingTl.isInitialized) {
            logger.d(logTag, "Recruiting TL received from bundles : $recruitingTl")
        } else {
            logger.e(
                logTag,
                "no Recruiting TL received from bundles",
                Exception("no Recruiting TL received from bundles")
            )
        }

        if (::reportingTl.isInitialized) {
            logger.d(logTag, "Reporting TL received from bundles : $reportingTl")
        } else {
            logger.e(
                logTag,
                "no Reporting TL received from bundles",
                Exception("no Reporting TL received from bundles")
            )
        }
    }

    override fun viewCreated(
        viewBinding: FragmentCallTeamLeaderBottomSheetBinding,
        savedInstanceState: Bundle?
    ) {
        initViews()
        initListeners()
    }

    private fun initViews() = viewBinding.apply{
        //set data
        recruitingTl?.let {
            recruitingTlCard.txtTitle.text = it.name
            recruitingTlCard.txtSubtitle.text = it.mobileNumber
        }
    }

    private fun initListeners() {

        //call click

    }

}