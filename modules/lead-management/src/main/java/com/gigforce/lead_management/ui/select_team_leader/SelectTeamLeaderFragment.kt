package com.gigforce.lead_management.ui.select_team_leader

import android.os.Bundle
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.gigforce.common_ui.components.cells.SearchTextChangeListener
import com.gigforce.common_ui.viewdatamodels.leadManagement.TeamLeader
import com.gigforce.core.base.BaseFragment2
import com.gigforce.core.extensions.gone
import com.gigforce.core.extensions.visible
import com.gigforce.core.navigation.INavigation
import com.gigforce.lead_management.R
import com.gigforce.lead_management.databinding.FragmentSelectBusinessBinding
import com.gigforce.lead_management.ui.LeadManagementSharedViewModel
import dagger.hilt.android.AndroidEntryPoint
import java.util.*
import javax.inject.Inject

@AndroidEntryPoint
class SelectTeamLeaderFragment : BaseFragment2<FragmentSelectBusinessBinding>(
    fragmentName = TAG,
    layoutId = R.layout.fragment_select_business, // reusing xml
    statusBarColor = R.color.lipstick_2
), TeamLeaderAdapter.OnTeamLeaderSelectedListener {

    companion object {
        private const val TAG = "SelectTeamLeadersFragment"

        const val INTENT_EXTRA_TEAM_LEADERS = "team_leaders"
    }

    @Inject
    lateinit var navigation: INavigation
    private lateinit var teamLeaders: ArrayList<TeamLeader>

    private val sharedViewModel: LeadManagementSharedViewModel by activityViewModels()
    private val adapter: TeamLeaderAdapter by lazy {

        TeamLeaderAdapter(requireContext()).apply {
            setOnTLSelectedListener(this@SelectTeamLeaderFragment)
        }
    }

    override fun shouldPreventViewRecreationOnNavigation(): Boolean = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        getDataFrom(
            arguments,
            savedInstanceState
        )
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putParcelableArrayList(INTENT_EXTRA_TEAM_LEADERS, teamLeaders)
    }

    override fun viewCreated(
        viewBinding: FragmentSelectBusinessBinding,
        savedInstanceState: Bundle?
    ) {

        if (viewCreatedForTheFirstTime) {
            initListeners()
            setTLDataOnView()
        }
    }


    private fun getDataFrom(
        arguments: Bundle?,
        savedInstanceState: Bundle?
    ) {

        arguments?.let {
            teamLeaders = it.getParcelableArrayList(INTENT_EXTRA_TEAM_LEADERS) ?: arrayListOf()
        }

        savedInstanceState?.let {
            teamLeaders = it.getParcelableArrayList(INTENT_EXTRA_TEAM_LEADERS) ?: arrayListOf()
        }
        logDataReceivedFromBundles()
    }

    private fun logDataReceivedFromBundles() {

        if (::teamLeaders.isInitialized) {
            logger.d(logTag, "received ${teamLeaders.size} team leaders from previous screen")
        } else {
            logger.e(
                logTag,
                "fetching tls from bundles from bundles",
                Exception("null team-leader received from bundles")
            )
        }
    }

    private fun setTLDataOnView() {
        setDataOnView()
    }

    private fun initListeners() {
        viewBinding.toolbar.apply {
            titleText.text = "Select Reporting TL"
            setBackButtonListener {
                navigation.navigateUp()
            }
            setBackButtonDrawable(R.drawable.ic_chevron)
            searchTextChangeListener = object : SearchTextChangeListener {
                override fun onSearchTextChanged(text: String) {
                    adapter.filter.filter(text)
                }
            }
        }

        viewBinding.businessRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        viewBinding.businessRecyclerView.adapter = adapter

        viewBinding.okayButton.setOnClickListener {
            val selectedTL = adapter.getSelectedTL() ?: return@setOnClickListener

            sharedViewModel.reportingTLSelected(
                selectedTL
            )
            findNavController().navigateUp()
        }
    }


    private fun setDataOnView() = viewBinding.apply {
        if (teamLeaders.isEmpty()) {
            this.businessInfoLayout.root.visible()
            this.businessInfoLayout.infoMessageTv.text = "No Teamleader to show"
            this.businessInfoLayout.infoIv.loadImage(R.drawable.ic_no_selection)
        } else {
            this.businessInfoLayout.root.gone()
            adapter.setData(teamLeaders)
        }

        okayButton.isEnabled = teamLeaders.find { it.selected } != null
    }

    override fun onTeamLeaderFiltered(
        tlCountVisibleAfterFiltering: Int,
        selectedTLVisible: Boolean
    ) {
        if (tlCountVisibleAfterFiltering != 0) {
            viewBinding.businessInfoLayout.root.gone()
            viewBinding.okayButton.isEnabled = selectedTLVisible
        } else {
            viewBinding.okayButton.isEnabled = false
            viewBinding.businessInfoLayout.root.visible()
            viewBinding.businessInfoLayout.infoMessageTv.text =
                "No Team leader to show"
            viewBinding.businessInfoLayout.infoIv.loadImage(R.drawable.ic_no_selection)
        }
    }

    override fun onTeamLeaderSelected(selectedTL: TeamLeader) {
        viewBinding.okayButton.isEnabled = true
    }
}