package com.gigforce.lead_management.ui.select_cluster

import android.os.Bundle
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestManager
import com.gigforce.common_ui.components.cells.SearchTextChangeListener
import com.gigforce.common_ui.viewdatamodels.leadManagement.*
import com.gigforce.core.base.BaseFragment2
import com.gigforce.core.extensions.gone
import com.gigforce.core.extensions.visible
import com.gigforce.core.navigation.INavigation
import com.gigforce.lead_management.R
import com.gigforce.lead_management.databinding.FragmentSelectClusterBinding
import com.gigforce.lead_management.ui.LeadManagementSharedViewModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class SelectClusterFragment : BaseFragment2<FragmentSelectClusterBinding>(
    fragmentName = TAG,
    layoutId = R.layout.fragment_select_cluster,
    statusBarColor = R.color.lipstick_2
), ClusterAdapter.OnClusterSelectedListener {

    companion object {
        private const val TAG = "SelectJobProfileFragment"
        const val INTENT_EXTRA_SELECTED_BUSINESS = "selected_business"
        const val INTENT_EXTRA_CLUSTER = "clusters"
    }

    @Inject
    lateinit var navigation: INavigation
    private val sharedViewModel: LeadManagementSharedViewModel by activityViewModels()
    private var clusters: ArrayList<OtherCityClusterItem> = arrayListOf()

    private val glide: RequestManager by lazy {
        Glide.with(requireContext())
    }

    private val clusterAdapter: ClusterAdapter by lazy {
        ClusterAdapter(requireContext(), glide).apply {
            setOnClusterSelectedListener(this@SelectClusterFragment)
        }
    }

    override fun viewCreated(
        viewBinding: FragmentSelectClusterBinding,
        savedInstanceState: Bundle?
    ) {
        getDataFrom(
            arguments,
            savedInstanceState
        )
        initListeners()
        setDataOnView()
    }

    private fun getDataFrom(
        arguments: Bundle?,
        savedInstanceState: Bundle?
    ) {

        arguments?.let {
            clusters = it.getParcelableArrayList(INTENT_EXTRA_CLUSTER) ?: return@let
        }

        savedInstanceState?.let {
            clusters = it.getParcelableArrayList(INTENT_EXTRA_CLUSTER) ?: return@let
        }
    }

    override fun onSaveInstanceState(
        outState: Bundle
    ) {
        super.onSaveInstanceState(outState)
        outState.putParcelableArrayList(
            INTENT_EXTRA_CLUSTER,
            clusters
        )
    }


    private fun initListeners() = viewBinding.apply {
        toolbar.apply {
            titleText.text = getString(R.string.select_cluster_lead)
            setBackButtonListener {
                navigation.popBackStack()
            }
            setBackButtonDrawable(R.drawable.ic_chevron)
            searchTextChangeListener = object : SearchTextChangeListener {

                override fun onSearchTextChanged(text: String) {
                    clusterAdapter.filter.filter(text)
                }
            }
        }

        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = clusterAdapter

//        okayButton.setOnClickListener {
//            val selectedJobProfile = clusterAdapter.getSelectedBusiness() ?: return@setOnClickListener
//            sharedViewModel.jobProfileSelected(selectedBusiness,selectedJobProfile)
//
//            navigation.popBackStack(
//                LeadManagementNavDestinations.FRAGMENT_SELECTION_FORM_1,
//                false
//            )
//        }
    }

    private fun setDataOnView() = viewBinding.apply {
        if (clusters.isEmpty()) {
            this.infoLayout.root.visible()
            this.infoLayout.infoMessageTv.text = getString(R.string.no_job_profile_to_show_lead)
            this.infoLayout.infoIv.loadImage(R.drawable.ic_no_selection)
        } else {
            this.infoLayout.root.gone()
            clusterAdapter.setData(clusters)
        }

        okayButton.isEnabled = clusters.find { it.selected } != null
    }

    override fun onJobProfileFiltered(
        jobProfileCountVisibleAfterFiltering: Int,
        selectedJobProfileVisible: Boolean
    ) {
        if(jobProfileCountVisibleAfterFiltering != 0){
            viewBinding.infoLayout.root.gone()
            viewBinding.okayButton.isEnabled = selectedJobProfileVisible
        } else{
            viewBinding.okayButton.isEnabled = false
            viewBinding.infoLayout.root.visible()
            viewBinding.infoLayout.infoMessageTv.text = getString(R.string.no_job_profile_to_show_lead)
            viewBinding.infoLayout.infoIv.loadImage(R.drawable.ic_no_selection)
        }
    }

    override fun onClusterSelected(clusterSelected: OtherCityClusterItem) {
        viewBinding.okayButton.isEnabled = true
    }
}