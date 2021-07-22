package com.gigforce.lead_management.ui.select_gig_location

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.gigforce.common_ui.components.atoms.ChipGroupComponent
import com.gigforce.common_ui.components.atoms.models.ChipGroupModel
import com.gigforce.common_ui.viewdatamodels.leadManagement.JobProfileDetails
import com.gigforce.common_ui.viewdatamodels.leadManagement.JobProfileOverview
import com.gigforce.core.base.BaseFragment2
import com.gigforce.core.navigation.INavigation
import com.gigforce.core.utils.Lce
import com.gigforce.lead_management.LeadManagementConstants
import com.gigforce.lead_management.R
import com.gigforce.lead_management.databinding.SelectGigLocationFragmentBinding
import com.gigforce.lead_management.gigeronboarding.SelectGigApplicationToActivateViewModel
import dagger.hilt.android.AndroidEntryPoint

import javax.inject.Inject

@AndroidEntryPoint
class SelectGigLocationFragment : BaseFragment2<SelectGigLocationFragmentBinding>(
    fragmentName = "SelectGigLocationFragment",
    layoutId = R.layout.select_gig_location_fragment,
    statusBarColor = R.color.lipstick_2
) {

    companion object {
        fun newInstance() = SelectGigLocationFragment()
        private const val TAG = "SelectGigLocationFragment"
    }

    @Inject
    lateinit var navigation: INavigation

    private val viewModel: SelectGigLocationViewModel by viewModels()
    private  var userUid: String = ""
    private  var jobProfileId: String = ""

    override fun viewCreated(
        viewBinding: SelectGigLocationFragmentBinding,
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
        viewModel.viewState.observe(viewLifecycleOwner, Observer {
            val jobProfileDetails = it

            when (jobProfileDetails) {
                is Lce.Content -> showGigLocations(jobProfileDetails.content)
                is Lce.Error -> showErrorInLoadingGigLocation(jobProfileDetails.error)
                Lce.Loading -> {
                    showGigLocationAsLoading()
                }
            }
        })
    }

    private fun initListeners() {
        viewBinding.toolbar.apply {
            hideActionMenu()
            showTitle("Gig Location")
            setBackButtonListener(View.OnClickListener {
                navigation.popBackStack()
            })
        }

        viewBinding.submitBtn.setOnClickListener {
            navigation.navigateTo("LeadMgmt/shiftTimings", bundleOf(
                LeadManagementConstants.INTENT_EXTRA_USER_UID to userUid,
                LeadManagementConstants.INTENT_EXTRA_JOB_PROFILE to jobProfileId
            ))
        }
    }

    private fun showGigLocations(jobProfile: JobProfileDetails){
        //set chips for gig locations
        var cityChips = arrayListOf<ChipGroupModel>()
        var locationChips = arrayListOf<ChipGroupModel>()
        val cityAndLocations = jobProfile.cityAndLocations
        cityAndLocations.forEachIndexed { index, jobProfileCityAndLocation ->
            jobProfileCityAndLocation.let {
                cityChips.add(ChipGroupModel(it.city.toString(), -1, index))
            }
        }
        viewBinding.cityChipGroup.addChips(cityChips)
        logger.d(TAG, "Job city and locations ${cityChips.toArray()}")
        viewBinding.cityChipGroup.setOnCheckedChangeListener(object : ChipGroupComponent.OnCustomCheckedChangeListener{
            override fun onCheckedChangeListener(model: ChipGroupModel) {
                val groupedJobLocations = cityAndLocations.get(model.chipId).jobLocations.filter {
                    it.type != null
                }.groupBy {
                    it.type
                }
                var count = 0;
                groupedJobLocations.forEach { (localityType, list) ->
                    logger.d(TAG, "processing data, Status :  : ${list.size} JobLocations")
                    locationChips.add(ChipGroupModel(localityType, -1, count))
                    count++
                }
                viewBinding.locationChipGroup.addChips(locationChips)

                //extract dropdown from JobLocations by grouping into one list with type : Locality, Hub, Store
                viewBinding.locationChipGroup.setOnCheckedChangeListener(object : ChipGroupComponent.OnCustomCheckedChangeListener{
                    override fun onCheckedChangeListener(model1: ChipGroupModel) {
                        cityAndLocations.get(model.chipId).jobLocations.forEachIndexed { index, jobLocation ->

                        }
                    }

                })
            }
        })




    }

    private fun showErrorInLoadingGigLocation(error: String){

    }

    private fun showGigLocationAsLoading(){

    }

}