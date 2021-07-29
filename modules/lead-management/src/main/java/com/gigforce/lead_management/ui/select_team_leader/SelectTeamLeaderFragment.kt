package com.gigforce.lead_management.ui.select_team_leader

import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.gigforce.common_ui.components.atoms.models.ChipGroupModel
import com.gigforce.common_ui.datamodels.ShimmerDataModel
import com.gigforce.common_ui.ext.startShimmer
import com.gigforce.common_ui.ext.stopShimmer
import com.gigforce.common_ui.viewdatamodels.GigerProfileCardDVM
import com.gigforce.common_ui.viewdatamodels.leadManagement.AssignGigRequest
import com.gigforce.common_ui.viewdatamodels.leadManagement.JobProfileDetails
import com.gigforce.common_ui.viewdatamodels.leadManagement.JobTeamLeader
import com.gigforce.core.base.BaseFragment2
import com.gigforce.core.extensions.gone
import com.gigforce.core.extensions.visible
import com.gigforce.core.navigation.INavigation
import com.gigforce.core.utils.Lce
import com.gigforce.lead_management.LeadManagementConstants
import com.gigforce.lead_management.LeadManagementNavDestinations
import com.gigforce.lead_management.R
import com.gigforce.lead_management.databinding.SelectTeamLeaderFragmentBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

@AndroidEntryPoint
class SelectTeamLeaderFragment : BaseFragment2<SelectTeamLeaderFragmentBinding>(
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

    private lateinit var userUid: String
    private lateinit var assignGigRequest: AssignGigRequest
    private var currentGigerInfo: GigerProfileCardDVM? = null

    val selectedGigforceTLs = arrayListOf<JobTeamLeader>()
    val selectedBusinessTLs = arrayListOf<JobTeamLeader>()
    var gigforceTeamLeaders = listOf<JobTeamLeader>()
    var businessTeamLeaders = listOf<JobTeamLeader>()
    val gigforceTeamLeaderChips = arrayListOf<ChipGroupModel>()
    val businessTeamLeaderChips = arrayListOf<ChipGroupModel>()

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
            userUid = it.getString(LeadManagementConstants.INTENT_EXTRA_USER_ID) ?: return@let
            assignGigRequest =
                it.getParcelable(LeadManagementConstants.INTENT_EXTRA_ASSIGN_GIG_REQUEST_MODEL)
                    ?: return@let
            currentGigerInfo =
                it.getParcelable(LeadManagementConstants.INTENT_EXTRA_CURRENT_JOINING_USER_INFO)
                    ?: return@let
        }

        savedInstanceState?.let {
            userUid = it.getString(LeadManagementConstants.INTENT_EXTRA_USER_ID) ?: return@let
            assignGigRequest =
                it.getParcelable(LeadManagementConstants.INTENT_EXTRA_ASSIGN_GIG_REQUEST_MODEL)
                    ?: return@let
            currentGigerInfo =
                it.getParcelable(LeadManagementConstants.INTENT_EXTRA_CURRENT_JOINING_USER_INFO)
                    ?: return@let
        }
        logDataReceivedFromBundles()
    }

    private fun logDataReceivedFromBundles() {
        if (::userUid.isInitialized) {
            logger.d(logTag, "User-id received from bundles : $userUid")
        } else {
            logger.e(
                logTag,
                "no User-id received from bundles",
                Exception("no User-id received from bundles")
            )
        }

        if (::assignGigRequest.isInitialized.not()) {
            logger.e(
                logTag,
                "null assignGigRequest received from bundles",
                Exception("null assignGigRequest received from bundles")
            )
        } else {
            logger.d(logTag, "AssignGigRequest received from bundles : $assignGigRequest")
        }
    }

    override fun onSaveInstanceState(
        outState: Bundle
    ) {
        super.onSaveInstanceState(outState)
        outState.putString(LeadManagementConstants.INTENT_EXTRA_USER_ID, userUid)
        outState.putParcelable(
            LeadManagementConstants.INTENT_EXTRA_ASSIGN_GIG_REQUEST_MODEL,
            assignGigRequest
        )
        outState.putParcelable(
            LeadManagementConstants.INTENT_EXTRA_CURRENT_JOINING_USER_INFO,
            currentGigerInfo
        )
    }

    private fun initViewModel() {
        viewModel.getJobProfileDetails(assignGigRequest.jobProfileId, userUid)
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
            selectedGigforceTLs.clear()
            selectedBusinessTLs.clear()
            gigforceTeamLeaderChips.forEachIndexed { index, chipGroupModel ->
                if (chipGroupModel.isSelected) {
                    selectedGigforceTLs.add(gigforceTeamLeaders.get(index))
                }
            }
            businessTeamLeaderChips.forEachIndexed { index, chipGroupModel ->
                if (chipGroupModel.isSelected) {
                    selectedBusinessTLs.add(businessTeamLeaders.get(index))
                }
            }
            if (selectedGigforceTLs.isNotEmpty()) {
                assignGigRequest.gigForceTeamLeaders = selectedGigforceTLs
                assignGigRequest.businessTeamLeaders = selectedBusinessTLs
                logger.d(
                    TAG,
                    "Selected Business TLs $selectedBusinessTLs Selected Gigforce TLs $selectedGigforceTLs"
                )
                logger.d(TAG, "AssignGigReuest $assignGigRequest")

                navigation.navigateTo(
                    LeadManagementNavDestinations.FRAGMENT_REFERENCE_CHECK, bundleOf(
                        LeadManagementConstants.INTENT_EXTRA_USER_ID to userUid,
                        LeadManagementConstants.INTENT_EXTRA_ASSIGN_GIG_REQUEST_MODEL to assignGigRequest,
                        LeadManagementConstants.INTENT_EXTRA_CURRENT_JOINING_USER_INFO to currentGigerInfo,
                    )
                )
            } else {
                MaterialAlertDialogBuilder(requireContext())
                    .setMessage("Select Gigforce Team Leader to continue")
                    .setPositiveButton("Okay") { _, _ -> }
                    .show()
            }
        }

        if (currentGigerInfo != null) {
            viewBinding.gigerProfileCard.setProfileCard(currentGigerInfo!!)
        } else {
            viewLifecycleOwner.lifecycleScope.launch {
                viewBinding.gigerProfileCard.setGigerProfileData(userUid)
            }
        }
    }

    private fun showGigTeamLeaders(jobProfile: JobProfileDetails) = viewBinding.apply {
        stopShimmer(
            this.teamLeaderShimmerContainer,
            R.id.shimmer_controller
        )
        teamLeadersLayout.visible()
        teamLeaderShimmerContainer.gone()
        teamLeadersInfoLayout.root.gone()

        val gfTeamLeaders = jobProfile.gigforceTeamLeaders
        val currentUserUid = FirebaseAuth.getInstance().currentUser?.uid
        gigforceTeamLeaders = gfTeamLeaders.filter { it.id.equals(currentUserUid) } + gfTeamLeaders.filter { !it.id.equals(currentUserUid) }
        gigforceTeamLeaders = jobProfile.gigforceTeamLeaders.sortedBy { it.id.equals(FirebaseAuth.getInstance().currentUser?.uid) }
        businessTeamLeaders = jobProfile.businessTeamLeaders
        processTeamLeaders(gigforceTeamLeaders, businessTeamLeaders)
    }

    private fun processTeamLeaders(
        gigforceTLs: List<JobTeamLeader>,
        businessTLs: List<JobTeamLeader>
    ) {
        gigforceTeamLeaderChips.clear()
        gigforceTLs.forEachIndexed { index, teamLeader ->
            teamLeader.let {
                gigforceTeamLeaderChips.add(ChipGroupModel(it.name.toString(), -1, index))
            }
        }
        viewBinding.gigforceTLChipGroup.removeAllViews()
        viewBinding.gigforceTLChipGroup.addChips(
            gigforceTeamLeaderChips,
            isSingleSelection = true,
            true
        )
        logger.d(TAG, "Gigforce team leaders ${gigforceTeamLeaderChips.toArray()}")

        businessTeamLeaderChips.clear()
        businessTLs.forEachIndexed { index, teamLeader ->
            teamLeader.let {
                businessTeamLeaderChips.add(ChipGroupModel(it.name.toString(), -1, index))
            }
        }
        viewBinding.businessTLChipGroup.removeAllViews()
        viewBinding.businessTLChipGroup.addChips(
            businessTeamLeaderChips,
            isSingleSelection = false,
            false
        )
        logger.d(TAG, "Business team leaders ${businessTeamLeaderChips.toArray()}")

    }


    private fun showErrorInLoadingGigTeamLeaders(error: String) = viewBinding.apply {
        stopShimmer(
            teamLeaderShimmerContainer,
            R.id.shimmer_controller
        )
        teamLeaderShimmerContainer.gone()
        teamLeadersInfoLayout.root.visible()
        teamLeadersLayout.gone()
        teamLeadersInfoLayout.infoIv.loadImage(R.drawable.ic_no_joining_found)
        teamLeadersInfoLayout.infoMessageTv.text = error

    }

    private fun showNoTeamLeadersFound() = viewBinding.apply {
        stopShimmer(
            teamLeaderShimmerContainer,
            R.id.shimmer_controller
        )
        teamLeaderShimmerContainer.gone()
        teamLeadersInfoLayout.root.visible()
        teamLeadersLayout.gone()
        teamLeadersInfoLayout.infoIv.loadImage(R.drawable.ic_no_joining_found)
        teamLeadersInfoLayout.infoMessageTv.text = "No Team Leaders Found"
    }

    private fun showGigTeamLeadersAsLoading() = viewBinding.apply {
        teamLeadersLayout.gone()
        teamLeadersInfoLayout.root.gone()
        teamLeaderShimmerContainer.visible()

        startShimmer(
            this.teamLeaderShimmerContainer,
            ShimmerDataModel(
                minHeight = R.dimen.size_120,
                minWidth = LinearLayout.LayoutParams.MATCH_PARENT,
                marginRight = R.dimen.size_16,
                marginTop = R.dimen.size_1,
                orientation = LinearLayout.VERTICAL
            ),
            R.id.shimmer_controller
        )
    }
}