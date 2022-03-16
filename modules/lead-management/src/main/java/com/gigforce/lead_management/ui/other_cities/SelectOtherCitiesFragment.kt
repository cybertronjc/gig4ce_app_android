package com.gigforce.lead_management.ui.other_cities

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestManager
import com.gigforce.common_ui.components.cells.SearchTextChangeListener
import com.gigforce.common_ui.viewdatamodels.leadManagement.JobProfilesItem
import com.gigforce.common_ui.viewdatamodels.leadManagement.JoiningBusinessAndJobProfilesItem
import com.gigforce.common_ui.viewdatamodels.leadManagement.OtherCityClusterItem
import com.gigforce.core.base.BaseFragment2
import com.gigforce.core.extensions.gone
import com.gigforce.core.extensions.visible
import com.gigforce.core.navigation.INavigation
import com.gigforce.lead_management.LeadManagementNavDestinations
import com.gigforce.lead_management.R
import com.gigforce.lead_management.databinding.FragmentSelectOtherCitiesBinding
import com.gigforce.lead_management.ui.LeadManagementSharedViewModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class SelectOtherCitiesFragment : BaseFragment2<FragmentSelectOtherCitiesBinding>(
    fragmentName = TAG,
    layoutId = R.layout.fragment_select_other_cities,
    statusBarColor = R.color.lipstick_2
), SelectOtherCitiesAdapter.OnCitySelectedListener {

    companion object {
        private const val TAG = "SelectOtherCitiesFragment"
        const val INTENT_EXTRA_SELECTED_OTHER_CITIES = "selected_other_cities"
    }

    @Inject
    lateinit var navigation: INavigation
    private val sharedViewModel: LeadManagementSharedViewModel by activityViewModels()
    private var jobProfiles: ArrayList<JobProfilesItem> = arrayListOf()
    private lateinit var selectedBusiness: JoiningBusinessAndJobProfilesItem
    private var otherCityList: ArrayList<OtherCityClusterItem> = arrayListOf()
    private var locationType: String? = null
    private val glide: RequestManager by lazy {
        Glide.with(requireContext())
    }

    private val otherCityAdapter: SelectOtherCitiesAdapter by lazy {
        SelectOtherCitiesAdapter(requireContext(), glide).apply {
            setOnCitySelectedListener(this@SelectOtherCitiesFragment)
        }
    }
    override fun viewCreated(
        viewBinding: FragmentSelectOtherCitiesBinding,
        savedInstanceState: Bundle?
    ) {
        getDataFrom(arguments,savedInstanceState)
        initListeners()
        setDataOnView()
    }

    private fun getDataFrom(
        arguments: Bundle?,
        savedInstanceState: Bundle?
    ) {

        arguments?.let {
            //selectedBusiness = it.getParcelable(INTENT_EXTRA_SELECTED_OTHER_CITIES) ?: return@let
            otherCityList = it.getParcelableArrayList(INTENT_EXTRA_SELECTED_OTHER_CITIES) ?: return@let
        }

        savedInstanceState?.let {
            //selectedBusiness = it.getParcelable(INTENT_EXTRA_SELECTED_OTHER_CITIES) ?: return@let
            otherCityList = it.getParcelableArrayList(INTENT_EXTRA_SELECTED_OTHER_CITIES) ?: return@let
        }
    }

    override fun onSaveInstanceState(
        outState: Bundle
    ) {
        super.onSaveInstanceState(outState)
        outState.putParcelableArrayList(
            INTENT_EXTRA_SELECTED_OTHER_CITIES,
            otherCityList
        )
//        outState.putParcelable(
//            INTENT_EXTRA_SELECTED_OTHER_CITIES,
//            selectedBusiness
//        )
    }

    private fun initListeners() = viewBinding.apply {
        toolbar.apply {
            titleText.text = getString(R.string.select_other_city_lead)
            setBackButtonListener{
                navigation.navigateUp()
            }
            setBackButtonDrawable(R.drawable.ic_chevron)
            searchTextChangeListener = object : SearchTextChangeListener {
                override fun onSearchTextChanged(text: String) {
                    otherCityAdapter.filter.filter(text)
                }
            }
        }

        otherCitiesRecyclerView.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        otherCitiesRecyclerView.adapter = otherCityAdapter

        okayButton.setOnClickListener {
            val selectedCities = otherCityAdapter.getSelectedOtherCities() ?: return@setOnClickListener
            sharedViewModel.otherCitySelected(selectedCities)
            okayButton.postDelayed({
                navigation.popBackStack(
                    LeadManagementNavDestinations.FRAGMENT_SELECTION_FORM_2,
                    false
                )
            },200)

        }
    }

    private fun setDataOnView() = viewBinding.apply {
        if (otherCityList.isEmpty()) {
            this.infoLayout.root.visible()
            this.infoLayout.infoMessageTv.text = getString(R.string.no_city_to_show_lead)
            this.infoLayout.infoIv.loadImage(R.drawable.ic_no_selection)
        } else {
            this.infoLayout.root.gone()
//            otherCityAdapter.setData(otherCityList)
            var updatedOtherCityList: ArrayList<OtherCityClusterItem> = arrayListOf()
            val groupedOtherCities = otherCityList.groupBy { it.name?.get(0) }
            Log.d("groupedOtherCities", " $groupedOtherCities")
            groupedOtherCities.forEach{ (alphabet, otherCities) ->
                updatedOtherCityList.add(
                    OtherCityClusterItem(alphabet.toString(), "", false, 1)
                )
                otherCities.forEach {
                    it.viewType = 2
                    updatedOtherCityList.add(
                        it
                    )
                }
            }
            otherCityAdapter.setData(updatedOtherCityList)
        }

        okayButton.isEnabled = otherCityList.find { it.selected } != null
    }

    override fun onCityFiltered(cityCountVisibleAfterFiltering: Int, selectedCityVisible: Boolean) {
        if(cityCountVisibleAfterFiltering != 0){
            viewBinding.infoLayout.root.gone()
            viewBinding.okayButton.isEnabled = selectedCityVisible
        } else{
            viewBinding.okayButton.isEnabled = false
            viewBinding.infoLayout.root.visible()
            viewBinding.infoLayout.infoMessageTv.text = getString(R.string.no_city_to_show_lead)
            viewBinding.infoLayout.infoIv.loadImage(R.drawable.ic_no_selection)
        }
    }

    override fun onCitySelected(selectedCity: OtherCityClusterItem) {
        viewBinding.okayButton.isEnabled = otherCityAdapter.getSelectedOtherCities()?.size != 0
    }


}