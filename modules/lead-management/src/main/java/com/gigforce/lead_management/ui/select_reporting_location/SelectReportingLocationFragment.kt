package com.gigforce.lead_management.ui.select_reporting_location

import android.os.Bundle
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestManager
import com.gigforce.common_ui.components.cells.SearchTextChangeListener
import com.gigforce.common_ui.viewdatamodels.leadManagement.*
import com.gigforce.core.base.BaseFragment2
import com.gigforce.core.extensions.gone
import com.gigforce.core.extensions.visible
import com.gigforce.lead_management.R
import com.gigforce.lead_management.databinding.FragmentSelectJobProfileBinding
import com.gigforce.lead_management.databinding.FragmentSelectReportingLocationBinding
import com.gigforce.lead_management.ui.LeadManagementSharedViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SelectReportingLocationFragment : BaseFragment2<FragmentSelectReportingLocationBinding>(
    fragmentName = TAG,
    layoutId = R.layout.fragment_select_reporting_location,
    statusBarColor = R.color.lipstick_2
), ReportingLocationAdapter.OnReportingLocationSelectedListener {

    companion object {
        private const val TAG = "SelectJobProfileFragment"
        const val INTENT_EXTRA_REPORTING_LOCATIONS = "reporting_locations"
    }

    private val sharedViewModel: LeadManagementSharedViewModel by activityViewModels()
    private var reportingLocations: ArrayList<ReportingLocationsItem> = arrayListOf()

    private val glide: RequestManager by lazy {
        Glide.with(requireContext())
    }

    private val reportingLocationAdapter: ReportingLocationAdapter by lazy {
        ReportingLocationAdapter(requireContext(), glide).apply {
            setOnReportingLocationSelectedListener(this@SelectReportingLocationFragment)
        }
    }

    override fun shouldPreventViewRecreationOnNavigation(): Boolean {
        return true
    }

    override fun viewCreated(
        viewBinding: FragmentSelectReportingLocationBinding,
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
            reportingLocations = it.getParcelableArrayList(INTENT_EXTRA_REPORTING_LOCATIONS) ?: return@let
        }

        savedInstanceState?.let {
            reportingLocations = it.getParcelableArrayList(INTENT_EXTRA_REPORTING_LOCATIONS) ?: return@let
        }
    }

    override fun onSaveInstanceState(
        outState: Bundle
    ) {
        super.onSaveInstanceState(outState)
        outState.putParcelableArrayList(
            INTENT_EXTRA_REPORTING_LOCATIONS,
            reportingLocations
        )
    }


    private fun initListeners() = viewBinding.apply {
        toolbar.apply {
            setBackButtonListener{
                navigation.navigateUp()
            }
            searchTextChangeListener = object : SearchTextChangeListener {
                override fun onSearchTextChanged(text: String) {
                    reportingLocationAdapter.filter.filter(text)
                }
            }
        }

        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = reportingLocationAdapter

        okayButton.setOnClickListener {
            val selectedReportingLocation = reportingLocationAdapter.getSelectedReportingLocation() ?: return@setOnClickListener
            sharedViewModel.reportingLocationSelected(selectedReportingLocation)
            findNavController().navigateUp()
        }
    }

    private fun setDataOnView() = viewBinding.apply {
        if (reportingLocations.isEmpty()) {
            this.infoLayout.root.visible()
            this.infoLayout.infoMessageTv.text = "No reporting location to show"
        } else {
            this.infoLayout.root.gone()
            reportingLocationAdapter.setData(reportingLocations)
        }
    }

    override fun onReportingLocationSelected(reportingLocation: ReportingLocationsItem) {
        viewBinding.okayButton.isEnabled = true
    }
}