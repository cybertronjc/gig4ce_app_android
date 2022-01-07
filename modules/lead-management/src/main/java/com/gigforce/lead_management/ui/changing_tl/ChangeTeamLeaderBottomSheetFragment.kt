package com.gigforce.lead_management.ui.changing_tl

import android.graphics.Color
import android.os.Bundle
import android.widget.LinearLayout
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.gigforce.common_ui.datamodels.ShimmerDataModel
import com.gigforce.common_ui.ext.startShimmer
import com.gigforce.common_ui.ext.stopShimmer
import com.gigforce.common_ui.viewdatamodels.leadManagement.ChangeTeamLeaderRequestItem
import com.gigforce.common_ui.viewdatamodels.leadManagement.ResultItem
import com.gigforce.common_ui.viewdatamodels.leadManagement.TeamLeader
import com.gigforce.core.base.BaseBottomSheetDialogFragment
import com.gigforce.core.extensions.getTextChangeAsStateFlow
import com.gigforce.core.extensions.gone
import com.gigforce.core.extensions.visible
import com.gigforce.lead_management.R
import com.gigforce.lead_management.databinding.ChangeTeamLeaderBottomSheetFragmentBinding
import com.gigforce.lead_management.ui.LeadManagementSharedViewModel
import com.github.razir.progressbutton.attachTextChangeAnimator
import com.github.razir.progressbutton.bindProgressButton
import com.github.razir.progressbutton.hideProgress
import com.github.razir.progressbutton.showProgress
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect

@AndroidEntryPoint
class ChangeTeamLeaderBottomSheetFragment :
    BaseBottomSheetDialogFragment<ChangeTeamLeaderBottomSheetFragmentBinding>(
        fragmentName = "ChangeTeamLeaderBottomSheetFragment",
        layoutId = R.layout.change_team_leader_bottom_sheet_fragment
    ), TeamLeaderAdapter2.OnTeamLeaderSelectedListener {

    companion object {
        fun newInstance() = ChangeTeamLeaderBottomSheetFragment()
        const val TAG = "ChangeTeamLeaderBottomSheetFragment"
        const val INTENT_EXTRA_GIGER_LIST = "gigers_list"

        fun launch(
            gigers: ArrayList<ChangeTeamLeaderRequestItem>,
            childFragmentManager: FragmentManager
        ) {
            ChangeTeamLeaderBottomSheetFragment().apply {
                arguments = bundleOf(INTENT_EXTRA_GIGER_LIST to gigers)
            }.show(childFragmentManager, TAG)
        }
    }

    private val sharedViewModel: LeadManagementSharedViewModel by activityViewModels()
    private val viewModel: ChangeTeamLeaderBottomSheetViewModel by viewModels()
    private var gigerForChangeTL: ArrayList<ChangeTeamLeaderRequestItem>? = null

    private val adapter: TeamLeaderAdapter2 by lazy {

        TeamLeaderAdapter2(requireContext()).apply {
            setOnTLSelectedListener(this@ChangeTeamLeaderBottomSheetFragment)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(DialogFragment.STYLE_NORMAL, R.style.DialogStyle)


        arguments?.let {
            gigerForChangeTL = it.getParcelableArrayList(INTENT_EXTRA_GIGER_LIST) ?: return@let
        }

        savedInstanceState?.let {
            gigerForChangeTL = it.getParcelableArrayList(INTENT_EXTRA_GIGER_LIST) ?: return@let
        }

        logDataReceivedFromBundles(
            gigerForChangeTL
        )

        viewModel.gigerForChangeTL = gigerForChangeTL?.toList() ?: emptyList()
        viewModel.sharedViewModel = sharedViewModel
    }

    private fun logDataReceivedFromBundles(
        gigerForChangeTL: ArrayList<ChangeTeamLeaderRequestItem>?
    ) {
        logger.d(TAG, "giger got change tl from previous screen : $gigerForChangeTL")
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putParcelableArrayList(
            INTENT_EXTRA_GIGER_LIST,
            gigerForChangeTL
        )
    }

    override fun viewCreated(
        viewBinding: ChangeTeamLeaderBottomSheetFragmentBinding,
        savedInstanceState: Bundle?
    ) {
        initView()
        initViewModel()
        initListeners()
    }

    private fun initView() = viewBinding.changeTeamLeaderMainLayout.apply {
        this.teamLeaderRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        this.teamLeaderRecyclerView.adapter = adapter
    }

    private fun initListeners() {
        viewBinding.changeTeamLeaderSuccessLayout.okayButton.setOnClickListener {
            dismiss()
        }

        viewBinding.changeTeamLeaderMainLayout.apply{
            bindProgressButton(confirmChangeTl)
            confirmChangeTl.attachTextChangeAnimator()
            confirmChangeTl.setOnClickListener {

                val selectedTL = adapter.getSelectedTL() ?: return@setOnClickListener
                viewModel.changeTeamLeadersOfGigersTo(selectedTL)
            }

            lifecycleScope.launchWhenCreated {

                searchItem.getTextChangeAsStateFlow()
                    .collect { searchString ->
                        adapter.filter.filter(searchString)
                    }
            }
        }
    }

    private fun initViewModel() {

        viewModel
            .viewState
            .observe(viewLifecycleOwner, {

                when (it) {
                    ChangeTeamLeaderBottomSheetState.LoadingTeamLeaders -> showTeamLeadersLoading()
                    is ChangeTeamLeaderBottomSheetState.TeamLeaderListLoaded -> showTeamLeaders(it.teamLeaders)
                    is ChangeTeamLeaderBottomSheetState.ErrorLoadingTeamLeaders -> showErrorLoadingTeamLeaders(
                        it.error
                    )

                    ChangeTeamLeaderBottomSheetState.ChangingTl -> showChangingTLLayout()
                    is ChangeTeamLeaderBottomSheetState.ErrorWhileChangingTeamLeaders -> errorWhileTLChangingTeamLeaders(
                        it.error
                    )

                    is ChangeTeamLeaderBottomSheetState.SomeTeamLeaderChangeFailed -> someTLChangeFailed(
                        it.failedList
                    )
                    ChangeTeamLeaderBottomSheetState.TeamLeaderChangedForAllGigers -> {
                        teamLeaderChangedForAllGigers()
                    }
                }
            })
    }

    private fun showErrorLoadingTeamLeaders(
        error: String
    ) = viewBinding.apply {
        stopShimmer(
            this.changeTlShimmerContainer as LinearLayout,
            R.id.shimmer_controller
        )

        this.changeTlShimmerContainer.gone()
        this.changeTeamLeaderMainLayout.root.gone()
        this.formMainInfoLayout.root.visible()

        this.formMainInfoLayout.infoMessageTv.text = error
    }

    private fun showTeamLeaders(
        teamLeaders: List<TeamLeader>
    ) = viewBinding.apply {

        stopShimmer(
            this.changeTlShimmerContainer as LinearLayout,
            R.id.shimmer_controller
        )

        this.changeTlShimmerContainer.gone()

        if (teamLeaders.isEmpty()) {

            this.changeTeamLeaderMainLayout.root.gone()
            adapter.setData(teamLeaders)

            viewBinding.formMainInfoLayout.root.visible()
            viewBinding.formMainInfoLayout.infoMessageTv.text =
                "No Team leader to show"
            viewBinding.formMainInfoLayout.infoIv.loadImage(R.drawable.ic_no_selection)
        } else {

            viewBinding.formMainInfoLayout.root.gone()
            this.changeTeamLeaderMainLayout.root.visible()
            adapter.setData(teamLeaders)
        }

    }

    private fun showTeamLeadersLoading() = viewBinding.apply {
        this.changeTeamLeaderMainLayout.root.gone()
        this.formMainInfoLayout.root.gone()

        this.changeTlShimmerContainer.visible()
        startShimmer(
            this.changeTlShimmerContainer as LinearLayout,
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

    private fun showChangingTLLayout() = viewBinding.changeTeamLeaderMainLayout.apply {

        confirmChangeTl.showProgress {
            buttonText = "Processing..."
            progressColor = Color.WHITE
        }
        confirmChangeTl.isEnabled = false
    }

    private fun teamLeaderChangedForAllGigers() = viewBinding.apply {

        this.formMainInfoLayout.root.gone()
        this.changeTlShimmerContainer.gone()
        this.changeTeamLeaderMainLayout.root.gone()
        this.changeTeamLeaderSuccessLayout.root.visible()
    }

    private fun someTLChangeFailed(
        failedList: List<ResultItem>
    ) = viewBinding.changeTeamLeaderMainLayout.apply {
        confirmChangeTl.hideProgress(getString(R.string.submit_lead))
        confirmChangeTl.isEnabled = true

        var message = ""
        failedList.forEach {
            message = message.plus("- ${it.value?.gigerName}")
        }

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Unable to change TL for these users")
            .setMessage(message)
            .setPositiveButton("Okay") {_,_ ->}
            .show()
    }

    private fun errorWhileTLChangingTeamLeaders(
        error: String
    ) = viewBinding.changeTeamLeaderMainLayout.apply {
        confirmChangeTl.hideProgress(getString(R.string.submit_lead))
        confirmChangeTl.isEnabled = true

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Unable to change TLs")
            .setMessage(error)
            .setPositiveButton("Okay") { _, _ -> }
            .show()
    }

    override fun onTeamLeaderFiltered(
        tlCountVisibleAfterFiltering: Int,
        selectedTLVisible: Boolean
    ) {

        if (tlCountVisibleAfterFiltering != 0) {
            viewBinding.formMainInfoLayout.root.gone()
            viewBinding.changeTeamLeaderMainLayout.confirmChangeTl.isEnabled = selectedTLVisible
        } else {
            viewBinding.changeTeamLeaderMainLayout.confirmChangeTl.isEnabled = false
            viewBinding.formMainInfoLayout.root.visible()
            viewBinding.formMainInfoLayout.infoMessageTv.text =
                "No Team leader to show"
            viewBinding.formMainInfoLayout.infoIv.loadImage(R.drawable.ic_no_selection)
        }
    }

    override fun onTeamLeaderSelected(
        selectedTL: TeamLeader
    ) {
        viewBinding.changeTeamLeaderMainLayout.confirmChangeTl.isEnabled = true
    }

}