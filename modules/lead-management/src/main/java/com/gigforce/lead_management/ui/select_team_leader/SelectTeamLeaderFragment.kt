package com.gigforce.lead_management.ui.select_team_leader

import android.os.Bundle
import android.widget.LinearLayout
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.gigforce.common_ui.components.cells.SearchTextChangeListener
import com.gigforce.common_ui.datamodels.ShimmerDataModel
import com.gigforce.common_ui.ext.startShimmer
import com.gigforce.common_ui.ext.stopShimmer
import com.gigforce.common_ui.viewdatamodels.leadManagement.TeamLeader
import com.gigforce.core.base.BaseFragment2
import com.gigforce.core.extensions.gone
import com.gigforce.core.extensions.visible
import com.gigforce.core.navigation.INavigation
import com.gigforce.core.utils.Lce
import com.gigforce.lead_management.R
import com.gigforce.lead_management.databinding.FragmentSelectTeamLeadersBinding
import com.gigforce.lead_management.ui.LeadManagementSharedViewModel
import dagger.hilt.android.AndroidEntryPoint
import java.util.*
import javax.inject.Inject

@AndroidEntryPoint
class SelectTeamLeaderFragment : BaseFragment2<FragmentSelectTeamLeadersBinding>(
    fragmentName = TAG,
    layoutId = R.layout.fragment_select_team_leaders,
    statusBarColor = R.color.lipstick_2
), TeamLeaderAdapter.OnTeamLeaderSelectedListener {

    companion object {
        private const val TAG = "SelectTeamLeadersFragment"

        const val INTENT_EXTRA_SHOW_ALL_TLS = "show_all_tls"
        const val INTENT_EXTRA_SELECTED_TL_ID = "selected_tl_id"
    }

    @Inject
    lateinit var navigation: INavigation

    private var shouldLoadAllTls : Boolean = false
    private var selectedTLId : String? = null

    private val viewModel: SelectTeamLeaderViewModel by viewModels()
    private val sharedViewModel: LeadManagementSharedViewModel by activityViewModels()
    private val adapter: TeamLeaderAdapter by lazy {

        TeamLeaderAdapter(requireContext()).apply {
            setOnTLSelectedListener(this@SelectTeamLeaderFragment)
        }
    }

    override fun shouldPreventViewRecreationOnNavigation(): Boolean = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.let {
            shouldLoadAllTls = it.getBoolean(INTENT_EXTRA_SHOW_ALL_TLS,false)
            selectedTLId = it.getString(INTENT_EXTRA_SELECTED_TL_ID)
        }

        savedInstanceState?.let {
            shouldLoadAllTls = it.getBoolean(INTENT_EXTRA_SHOW_ALL_TLS,false)
            selectedTLId = it.getString(INTENT_EXTRA_SELECTED_TL_ID)
        }

        viewModel.selectedTlID = selectedTLId
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean(INTENT_EXTRA_SHOW_ALL_TLS,shouldLoadAllTls)
        outState.putString(INTENT_EXTRA_SELECTED_TL_ID,selectedTLId)
    }

    override fun viewCreated(
        viewBinding: FragmentSelectTeamLeadersBinding,
        savedInstanceState: Bundle?
    ) {

        if (viewCreatedForTheFirstTime) {
            checkOrUnCheckInitialStateOfCheckBox()
            fetchTeamLeaders(shouldLoadAllTls)

            initListeners()
            initViewModel()
        }
    }

    private fun checkOrUnCheckInitialStateOfCheckBox() {
        viewBinding.mainForm.loadAllTlSwitch.isChecked = shouldLoadAllTls
    }

    private fun initViewModel() {

        viewModel
            .viewState
            .observe(viewLifecycleOwner, {

                when (it) {
                    Lce.Loading -> {

                        hideErrorOrInfoLayout()
                        setDataOnView(emptyList())
                        showLoadingElements()
                    }
                    is Lce.Content -> {
                        hideLoadingElements()

                        if(it.content.isEmpty()){
                            setDataOnView(emptyList())
                            showErrorOrInfoLayout("No team leader to show")
                        } else{
                            hideErrorOrInfoLayout()
                            setDataOnView(it.content)
                        }
                    }
                    is Lce.Error -> {

                        setDataOnView(emptyList())
                        hideLoadingElements()
                        showErrorOrInfoLayout(it.error)
                    }
                }
            })
    }

    private fun showLoadingElements() = viewBinding.apply{

        dataLoadingShimmerContainer.visible()
        startShimmer(
            this.dataLoadingShimmerContainer as LinearLayout,
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

    private fun hideLoadingElements() = viewBinding.apply{

        stopShimmer(
            dataLoadingShimmerContainer as LinearLayout,
            R.id.shimmer_controller
        )
        dataLoadingShimmerContainer.gone()
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

        viewBinding.mainForm.loadAllTlSwitch.setOnCheckedChangeListener { buttonView, isChecked ->
            viewModel.fetchTeamLeaders(isChecked)
        }

        viewBinding.mainForm.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        viewBinding.mainForm.recyclerView.adapter = adapter

        viewBinding.mainForm.okayButton.setOnClickListener {
            val selectedTL = adapter.getSelectedTL() ?: return@setOnClickListener

            sharedViewModel.reportingTLSelected(
                selectedTL,
                viewModel.fetchingAllTeamLeader
            )
            findNavController().navigateUp()
        }
    }

    private fun fetchTeamLeaders(shouldFetchAllTLs: Boolean) {
        viewModel.fetchTeamLeaders(shouldFetchAllTLs)
    }

    private fun setDataOnView(
        teamLeaders: List<TeamLeader>
    ) = viewBinding.apply {

        viewBinding.mainForm.root.visible()
        adapter.setData(teamLeaders)
        viewBinding.mainForm.okayButton.isEnabled = teamLeaders.find { it.selected } != null
    }

    private fun showErrorOrInfoLayout(
        textToShow: String
    ) = viewBinding.apply {

        this.formMainInfoLayout.root.visible()
        this.formMainInfoLayout.infoMessageTv.text = textToShow
        this.formMainInfoLayout.infoIv.loadImage(R.drawable.ic_no_selection)
    }

    private fun hideErrorOrInfoLayout() = viewBinding.apply {

        this.formMainInfoLayout.root.gone()
    }

    override fun onTeamLeaderFiltered(
        tlCountVisibleAfterFiltering: Int,
        selectedTLVisible: Boolean
    ) {
        if (tlCountVisibleAfterFiltering != 0) {
            viewBinding.formMainInfoLayout.root.gone()
            viewBinding.mainForm.okayButton.isEnabled = selectedTLVisible
        } else {
            viewBinding.mainForm.okayButton.isEnabled = false
            viewBinding.formMainInfoLayout.root.visible()
            viewBinding.formMainInfoLayout.infoMessageTv.text =
                "No Team leader to show"
            viewBinding.formMainInfoLayout.infoIv.loadImage(R.drawable.ic_no_selection)
        }
    }

    override fun onTeamLeaderSelected(selectedTL: TeamLeader) {
        viewBinding.mainForm.okayButton.isEnabled = true
    }
}