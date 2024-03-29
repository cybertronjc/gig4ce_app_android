package com.gigforce.lead_management.ui.select_city

import android.os.Bundle
import androidx.core.os.bundleOf
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.GridLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestManager
import com.gigforce.common_ui.components.cells.SearchTextChangeListener
import com.gigforce.common_ui.viewdatamodels.leadManagement.*
import com.gigforce.core.base.BaseFragment2
import com.gigforce.core.extensions.gone
import com.gigforce.core.extensions.visible
import com.gigforce.core.navigation.INavigation
import com.gigforce.common_ui.navigation.LeadManagementNavDestinations
import com.gigforce.lead_management.R
import com.gigforce.lead_management.databinding.FragmentSelectBusinessBinding
import com.gigforce.lead_management.ui.LeadManagementSharedViewModel
import com.gigforce.lead_management.ui.select_reporting_location.SelectReportingLocationFragment
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class SelectCityFragment : BaseFragment2<FragmentSelectBusinessBinding>(
    fragmentName = TAG,
    layoutId = R.layout.fragment_select_business,
    statusBarColor = R.color.lipstick_2
), CityAdapter.OnCitySelectedListener {

    companion object {
        private const val TAG = "SelectCityFragment"
        const val INTENT_EXTRA_CITY_LIST = "city_list"
        const val INTENT_ONSITE_OFFSITE = "onsite_offsite"
    }

    @Inject
    lateinit var navigation: INavigation
    private val sharedViewModel: LeadManagementSharedViewModel by activityViewModels()
    private var cityList: ArrayList<ReportingLocationsItem> = arrayListOf()
    private var locationType: String? = null

    private val glide: RequestManager by lazy {
        Glide.with(requireContext())
    }

    private val cityAdapter: CityAdapter by lazy {
        CityAdapter(requireContext(), glide).apply {
            setOnCitySelectedListener(this@SelectCityFragment)
        }
    }

    override fun shouldPreventViewRecreationOnNavigation(): Boolean {
        return true
    }

    override fun viewCreated(
        viewBinding: FragmentSelectBusinessBinding,
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
            cityList = it.getParcelableArrayList(INTENT_EXTRA_CITY_LIST) ?: return@let
            locationType = it.getString(INTENT_ONSITE_OFFSITE) ?: return@let
        }

        savedInstanceState?.let {
            cityList = it.getParcelableArrayList(INTENT_EXTRA_CITY_LIST) ?: return@let
            locationType = it.getString(INTENT_ONSITE_OFFSITE) ?: return@let
        }
    }

    override fun onSaveInstanceState(
        outState: Bundle
    ) {
        super.onSaveInstanceState(outState)
        outState.putParcelableArrayList(
            INTENT_EXTRA_CITY_LIST,
            cityList
        )
        outState.putString(
            INTENT_ONSITE_OFFSITE,
            locationType
        )
    }


    private fun initListeners() = viewBinding.apply {
        toolbar.apply {
            titleText.text = getString(R.string.select_city_lead)
            setBackButtonListener{
                navigation.navigateUp()
            }
            setBackButtonDrawable(R.drawable.ic_chevron)
            searchTextChangeListener = object : SearchTextChangeListener {
                override fun onSearchTextChanged(text: String) {
                    cityAdapter.filter.filter(text)
                }
            }
        }

        businessRecyclerView.layoutManager = GridLayoutManager(requireContext(), 3)
        businessRecyclerView.adapter = cityAdapter

        okayButton.setOnClickListener {
            val selectedCity = cityAdapter.getSelectedCity() ?: return@setOnClickListener
            sharedViewModel.citySelected(selectedCity)


            okayButton.postDelayed({
                if (locationType == "On Site"){
                    navigation.navigateTo(
                        LeadManagementNavDestinations.FRAGMENT_SELECT_REPORTING_LOCATION,
                        bundleOf(
                            SelectReportingLocationFragment.INTENT_EXTRA_REPORTING_LOCATIONS to selectedCity.reportingLocations,
                            SelectReportingLocationFragment.INTENT_EXTRA_SELECTED_CITY to selectedCity
                        ),
                        getNavOptions()
                    )
                }else{
                    navigation.popBackStack(
                        LeadManagementNavDestinations.FRAGMENT_SELECTION_FORM_2,
                        false
                    )
                }


            },200)

        }
    }

    private fun setDataOnView() = viewBinding.apply {
        if (cityList.isEmpty()) {
            this.businessInfoLayout.root.visible()
            this.businessInfoLayout.infoMessageTv.text = getString(R.string.no_city_to_show_lead)
            this.businessInfoLayout.infoIv.loadImage(R.drawable.ic_no_selection)
        } else {
            this.businessInfoLayout.root.gone()
            cityAdapter.setData(cityList)
        }

        okayButton.isEnabled = cityList.find { it.selected } != null
    }

    override fun onCitySelected(selectedCity: ReportingLocationsItem) {
        viewBinding.okayButton.isEnabled = true
    }

    override fun onCityFiltered(
        cityCountVisibleAfterFiltering: Int,
        selectedCityVisible: Boolean
    ) {

        if(cityCountVisibleAfterFiltering != 0){
            viewBinding.businessInfoLayout.root.gone()
            viewBinding.okayButton.isEnabled = selectedCityVisible
        } else{
            viewBinding.okayButton.isEnabled = false
            viewBinding.businessInfoLayout.root.visible()
            viewBinding.businessInfoLayout.infoMessageTv.text = getString(R.string.no_city_to_show_lead)
            viewBinding.businessInfoLayout.infoIv.loadImage(R.drawable.ic_no_selection)
        }
    }
}