package com.gigforce.lead_management.ui.select_team_leader

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.gigforce.common_ui.components.atoms.ChipGroupComponent
import com.gigforce.common_ui.components.atoms.models.ChipGroupModel
import com.gigforce.common_ui.viewdatamodels.leadManagement.JobProfileDetails
import com.gigforce.core.base.BaseFragment2
import com.gigforce.core.navigation.INavigation
import com.gigforce.core.utils.Lce
import com.gigforce.lead_management.LeadManagementConstants
import com.gigforce.lead_management.R
import com.gigforce.lead_management.databinding.SelectTeamLeaderFragmentBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

@AndroidEntryPoint
class SelectTeamLeaderFragment: BaseFragment2<SelectTeamLeaderFragmentBinding>(
    fragmentName = "SelectTeamLeadersFragment",
    layoutId = R.layout.select_team_leader_fragment,
    statusBarColor = R.color.lipstick_2
    ) {

    companion object {
        fun newInstance() = SelectTeamLeaderFragment()
        private const val TAG = "SelectTeamLeadersFragment"
    }

    @Inject
    lateinit var navigation: INavigation

    private val viewModel: SelectTeamLeaderViewModel by viewModels()
    private  var userUid: String = ""
    private  var jobProfileId: String = ""

    override fun viewCreated(
        viewBinding: SelectTeamLeaderFragmentBinding,
        savedInstanceState: Bundle?
    ) {
        getDataFrom(
            arguments,
            savedInstanceState
        )
        initListeners()
        initViewModel()
    }

    private fun getDataFrom(
        arguments: Bundle?,
        savedInstanceState: Bundle?
    ) {

        arguments?.let {
            userUid = it.getString(LeadManagementConstants.INTENT_EXTRA_USER_UID) ?: return@let
            jobProfileId = it.getString(LeadManagementConstants.INTENT_EXTRA_JOB_PROFILE) ?: return@let
        }

        savedInstanceState?.let {
            userUid = it.getString(LeadManagementConstants.INTENT_EXTRA_USER_UID) ?: return@let
            jobProfileId = it.getString(LeadManagementConstants.INTENT_EXTRA_JOB_PROFILE) ?: return@let
        }
    }

    override fun onSaveInstanceState(
        outState: Bundle
    ) {
        super.onSaveInstanceState(outState)
        outState.putString(LeadManagementConstants.INTENT_EXTRA_USER_UID, userUid)
        outState.putString(LeadManagementConstants.INTENT_EXTRA_JOB_PROFILE, jobProfileId)
    }

    private fun initViewModel() {
        viewModel.getJobProfileDetails(jobProfileId, userUid)
        viewModel.viewState.observe(viewLifecycleOwner, androidx.lifecycle.Observer {
            val jobProfileDetails = it

            when (jobProfileDetails) {
                is Lce.Content -> showGigTeamLeaders(jobProfileDetails.content)
                is Lce.Error -> showErrorInLoadingGigTeamLeaders(jobProfileDetails.error)
                Lce.Loading -> {
                    showGigTeamLeadersAsLoading()
                }
            }
        })
    }

    private fun initListeners() {
        viewBinding.toolbar.apply {
            hideActionMenu()
            showTitle("Team Leaders")
            setBackButtonListener(View.OnClickListener {
                navigation.popBackStack()
            })
        }

        viewBinding.submitBtn.setOnClickListener {
            navigation.navigateTo("LeadMgmt/selectTeamLeaders", bundleOf(
                LeadManagementConstants.INTENT_EXTRA_USER_UID to userUid,
                LeadManagementConstants.INTENT_EXTRA_JOB_PROFILE to jobProfileId
            )
            )
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewBinding.gigerProfileCard.setGigerProfileData("d5ToQmOn6sdAcPWvjsBuhYWm9kF3")
        }
    }

    private fun showGigTeamLeaders(jobProfile: JobProfileDetails){
        //set chips for gig team leaders
        val gigforceTeamLeaderChips = arrayListOf<ChipGroupModel>()
        val gigforceTeamLeaders = jobProfile.gigforceTeamLeaders
        val businessTeamLeaderChips = arrayListOf<ChipGroupModel>()
        val businessTeamLeaders = jobProfile.businessTeamLeaders
        gigforceTeamLeaders.forEachIndexed { index, teamLeader ->
            teamLeader.let {
                gigforceTeamLeaderChips.add(ChipGroupModel(it.name.toString(), -1, index))
            }
        }
        viewBinding.gigforceTLChipGroup.addChips(gigforceTeamLeaderChips)
        logger.d(TAG, "Gigforce team leaders ${gigforceTeamLeaderChips.toArray()}")
        viewBinding.gigforceTLChipGroup.setOnCheckedChangeListener(object : ChipGroupComponent.OnCustomCheckedChangeListener{
            override fun onCheckedChangeListener(model: ChipGroupModel) {

            }
        })
        businessTeamLeaders.forEachIndexed { index, teamLeader ->
            teamLeader.let {
                businessTeamLeaderChips.add(ChipGroupModel(it.name.toString(), -1, index))
            }
        }
        viewBinding.businessTLChipGroup.addChips(businessTeamLeaderChips)
        logger.d(TAG, "Business team leaders ${businessTeamLeaderChips.toArray()}")
        viewBinding.gigforceTLChipGroup.setOnCheckedChangeListener(object : ChipGroupComponent.OnCustomCheckedChangeListener{
            override fun onCheckedChangeListener(model: ChipGroupModel) {

            }
        })

    }

    private fun showErrorInLoadingGigTeamLeaders(error: String){

    }

    private fun showGigTeamLeadersAsLoading(){

    }
}