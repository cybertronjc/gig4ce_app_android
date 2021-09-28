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
import com.gigforce.core.navigation.INavigation
import com.gigforce.lead_management.LeadManagementNavDestinations
import com.gigforce.lead_management.R
import com.gigforce.lead_management.databinding.FragmentSelectReportingLocationBinding
import com.gigforce.lead_management.ui.LeadManagementSharedViewModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class SelectReportingLocationFragment : BaseFragment2<FragmentSelectReportingLocationBinding>(
    fragmentName = TAG,
    layoutId = R.layout.fragment_select_reporting_location,
    statusBarColor = R.color.lipstick_2
), ReportingLocationAdapter.OnReportingLocationSelectedListener {

    companion object {
        private const val TAG = "SelectJobProfileFragment"
        const val INTENT_EXTRA_REPORTING_LOCATIONS = "reporting_locations"
        const val INTENT_EXTRA_SELECTED_CITY = "selected_city"
    }

    @Inject
    lateinit var navigation: INavigation
    private val sharedViewModel: LeadManagementSharedViewModel by activityViewModels()
    private var reportingLocations: ArrayList<ReportingLocationsItem> = arrayListOf()
    private lateinit var selectCity: ReportingLocationsItem

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
            reportingLocations =
                it.getParcelableArrayList(INTENT_EXTRA_REPORTING_LOCATIONS) ?: return@let
            selectCity = it.getParcelable(INTENT_EXTRA_SELECTED_CITY) ?: return@let
        }

        savedInstanceState?.let {
            reportingLocations =
                it.getParcelableArrayList(INTENT_EXTRA_REPORTING_LOCATIONS) ?: return@let
            selectCity = it.getParcelable(INTENT_EXTRA_SELECTED_CITY) ?: return@let
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
        outState.putParcelable(
            INTENT_EXTRA_SELECTED_CITY,
            selectCity
        )
    }


    private fun initListeners() = viewBinding.apply {
        toolbar.apply {
            titleText.text = "Select reporting location"
            setBackButtonListener {
                navigation.navigateUp()
            }
            setBackButtonDrawable(R.drawable.ic_chevron)
            searchTextChangeListener = object : SearchTextChangeListener {
                override fun onSearchTextChanged(text: String) {
                    reportingLocationAdapter.filter.filter(text)
                }
            }
        }

        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = reportingLocationAdapter

        okayButton.setOnClickListener {
            val selectedReportingLocation =
                reportingLocationAdapter.getSelectedReportingLocation() ?: return@setOnClickListener
            sharedViewModel.reportingLocationSelected(
                selectCity,
                selectedReportingLocation
            )
            navigation.popBackStack(
                LeadManagementNavDestinations.FRAGMENT_SELECTION_FORM_2,
                false
            )
        }
    }


    private fun setDataOnView() = viewBinding.apply {

        if (reportingLocations.isEmpty()) {
            this.infoLayout.root.visible()
            this.infoLayout.infoMessageTv.text = "No reporting location to show"
            this.infoLayout.infoIv.loadImage(R.drawable.ic_no_selection)
        } else {
            this.infoLayout.root.gone()
            reportingLocationAdapter.setData(reportingLocations)
            viewBinding.okayButton.isEnabled = reportingLocations.find { it.selected } != null
        }
    }

    override fun onReportingLocationSelected(reportingLocation: ReportingLocationsItem) {
        viewBinding.okayButton.isEnabled = true
    }
}