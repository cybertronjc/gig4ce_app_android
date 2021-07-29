package com.gigforce.lead_management.ui.select_gig_location

import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.LinearLayout
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import com.gigforce.common_ui.components.atoms.ChipGroupComponent
import com.gigforce.common_ui.components.atoms.models.ChipGroupModel
import com.gigforce.common_ui.datamodels.ShimmerDataModel
import com.gigforce.common_ui.ext.showToast
import com.gigforce.common_ui.ext.startShimmer
import com.gigforce.common_ui.ext.stopShimmer
import com.gigforce.common_ui.viewdatamodels.GigerProfileCardDVM
import com.gigforce.common_ui.viewdatamodels.leadManagement.*
import com.gigforce.core.base.BaseFragment2
import com.gigforce.core.extensions.gone
import com.gigforce.core.extensions.visible
import com.gigforce.core.navigation.INavigation
import com.gigforce.core.utils.Lce
import com.gigforce.lead_management.LeadManagementConstants
import com.gigforce.lead_management.LeadManagementNavDestinations
import com.gigforce.lead_management.R
import com.gigforce.lead_management.databinding.SelectGigLocationFragmentLayoutBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

import javax.inject.Inject

@AndroidEntryPoint
class SelectGigLocationFragment : BaseFragment2<SelectGigLocationFragmentLayoutBinding>(
    fragmentName = "SelectGigLocationFragment",
    layoutId = R.layout.select_gig_location_fragment_layout,
    statusBarColor = R.color.lipstick_2
) {

    companion object {
        fun newInstance() = SelectGigLocationFragment()
        private const val TAG = "SelectGigLocationFragment"
    }

    @Inject
    lateinit var navigation: INavigation

    private val viewModel: SelectGigLocationViewModel by viewModels()
    private lateinit var userUid: String
    private lateinit var assignGigRequest: AssignGigRequest
    private var currentGigerInfo: GigerProfileCardDVM? = null
    var arrayAdapter1: ArrayAdapter<String>? = null
    var locationsArray = arrayListOf<String>()
    var selectedJobLocation : JobLocation? = null
    var jobLocalities : List<JobLocality> = emptyList()
    var jobStores : List<JobStore> = emptyList()
    val cityChips = arrayListOf<ChipGroupModel>()
    val locationChips = arrayListOf<ChipGroupModel>()
    var cityAndLocations = listOf<JobProfileCityAndLocation>()

    override fun viewCreated(
        viewBinding: SelectGigLocationFragmentLayoutBinding,
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
    }

    private fun initViewModel() {
        viewModel.getJobProfileDetails(assignGigRequest.jobProfileId, userUid)
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

    private fun initListeners() = viewBinding.apply{
        toolbar.apply {
            hideActionMenu()
            showTitle("Gig Location")
            setBackButtonListener(View.OnClickListener {
                navigation.popBackStack()
            })
        }

        submitBtn.setOnClickListener {
            cityChips.forEachIndexed { index, chipGroupModel ->
                if (chipGroupModel.isSelected){
                    assignGigRequest.cityId = cityAndLocations.get(index).id
                    assignGigRequest.cityName = cityAndLocations.get(index).city.toString()
                }
            }
            //get the selected location chip and create JobLocation model
            locationChips.forEachIndexed { index, chipGroupModel ->
                if (chipGroupModel.isSelected){
                    if ("Locality".equals(chipGroupModel.text)){
                        jobLocalities.forEach {
                            if (searchLocation1.text.toString().trim().equals(it.name)){
                                selectedJobLocation = JobLocation(it.id, "Locality", it.name)
                            }
                        }
                    } else {
                        jobStores.forEach {
                            if (searchLocation1.text.toString().trim().equals(it.name)){
                                selectedJobLocation = JobLocation(it.id, it.type, it.name)
                            }
                        }
                    }
                }
            }
            if (assignGigRequest.cityId.isEmpty()) {
                MaterialAlertDialogBuilder(requireContext())
                    .setMessage("Select a City to continue")
                    .setPositiveButton("Okay") { _, _ -> }
                    .show()
            } else {
                logger.d(TAG, "AssignGigRequest $assignGigRequest")
                assignGigRequest.location = selectedJobLocation
                navigation.navigateTo(
                    LeadManagementNavDestinations.FRAGMENT_SELECT_SHIFT_TIMMINGS, bundleOf(
                        LeadManagementConstants.INTENT_EXTRA_USER_ID to userUid,
                        LeadManagementConstants.INTENT_EXTRA_ASSIGN_GIG_REQUEST_MODEL to assignGigRequest,
                        LeadManagementConstants.INTENT_EXTRA_CURRENT_JOINING_USER_INFO to currentGigerInfo
                    )
                )
            }
        }



        searchLocation1.setOnFocusChangeListener { view, b ->
            if (b){
                searchLocation1.showDropDown()
            }
        }

        spinnerDrop.setOnClickListener {
            searchLocation1.showDropDown()
        }




        if (currentGigerInfo != null) {
            viewBinding.gigerProfileCard.setProfileCard(currentGigerInfo!!)
        } else {
            viewLifecycleOwner.lifecycleScope.launch {
                viewBinding.gigerProfileCard.setGigerProfileData(userUid)
            }
            viewBinding.gigerProfileCard.setJobProfileData(assignGigRequest.jobProfileName, assignGigRequest.companyLogo)
        }

    }

    private fun showGigLocations(jobProfile: JobProfileDetails) = viewBinding.apply {

        stopShimmer(
            this.locationShimmerContainer,
            R.id.shimmer_controller
        )
        locationLayout1.visible()
        locationShimmerContainer.gone()
        locationInfoLayout.root.gone()

        //set chips for gig locations

        cityAndLocations = jobProfile.cityAndLocations
        if (cityAndLocations.isEmpty()){
            showNoGigLocationFound()
        } else {
            cityChips.clear()
            cityAndLocations.forEachIndexed { index, jobProfileCityAndLocation ->
                jobProfileCityAndLocation.let {
                    cityChips.add(ChipGroupModel(it.city.toString(), -1, index))
                }
            }
            viewBinding.cityChipGroup.removeAllViews()
            viewBinding.cityChipGroup.addChips(cityChips, isSingleSelection = true, false)
            logger.d(TAG, "Job city and locations ${cityChips.toArray()}")
            viewBinding.cityChipGroup.setOnCheckedChangeListener(object :
                ChipGroupComponent.OnCustomCheckedChangeListener {
                override fun onCheckedChangeListener(model: ChipGroupModel) {
                    try {
                        //make select location layout visible
                        viewBinding.selectLocTV.visible()
                        viewBinding.locationLayout2.visible()

                        //make submit button enabled
                        viewBinding.submitBtn.background = resources.getDrawable(R.drawable.app_gradient_button)

                        //clear locations dropdown array
                        locationsArray.clear()
                        viewBinding.searchLocation1.setAdapter(null)
                        viewBinding.searchLocation1.setText("")
                        arrayAdapter1?.clear()
                        jobLocalities = jobProfile.locality
                        jobStores = jobProfile.stores.filter { it.cityId?.equals(cityAndLocations.get(model.chipId).id) == true }
                        locationChipGroup.removeAllViews()
                        processJobLocations(jobLocalities, jobStores)

                    } catch (e: Exception) {
                        logger.d(TAG, "Exception while checkedChangeListener ${e.toString()}")
                    }

                }
            })
        }
    }


    private fun processJobLocations(jobLocalities: List<JobLocality>, jobStores: List<JobStore>) = viewBinding.apply{

        locationChips.clear()

        val groupedJobLocations = jobStores?.filter {
            it.type != null
        }?.groupBy {
            it.type
        }
        var count = 0;
        if (jobLocalities.isNotEmpty()){
            locationChips.add(ChipGroupModel("Locality", -1, 0))
            logger.d(TAG, "processing job localities $locationChips")
            count++
        }

        groupedJobLocations?.forEach { (storeType, list) ->
            locationChips.add(ChipGroupModel(storeType, -1, count))
            logger.d(TAG, "processing data, Status :  : ${locationChips} JobStores")
            count++
        }

       locationChipGroup.addChips(locationChips, isSingleSelection = true, false)

        //extract dropdown from JobLocations by grouping into one list with type : Locality, Hub, Store
        viewBinding.locationChipGroup.setOnCheckedChangeListener(object : ChipGroupComponent.OnCustomCheckedChangeListener{
            override fun onCheckedChangeListener(model1: ChipGroupModel) {
                //make dropdown visible
                viewBinding.searchLocation1.visible()
                spinnerDrop.visible()
                locationsArray.clear()
                viewBinding.searchLocation1.setAdapter(null)
                viewBinding.searchLocation1.setText("")
                arrayAdapter1?.clear()
                if (model1.text.equals("Locality")){
                    jobLocalities.forEachIndexed { index, jobLocality ->
                        locationsArray.add(jobLocality.name.toString())
                    }
                } else{
                    groupedJobLocations.forEach { s, list ->
                        //locationsArray.add(s)
                        list.forEach {
                            locationsArray.add(it.name.toString())
                        }
                    }
                }
                arrayAdapter1 = context?.let { ArrayAdapter(it,android.R.layout.simple_spinner_dropdown_item, locationsArray) }
                searchLocation1.threshold = 1
                searchLocation1.setAdapter(arrayAdapter1)
                if (locationsArray.size == 1){
                    searchLocation1.setText(arrayAdapter1?.getItem(0).toString())
                }
                arrayAdapter1?.notifyDataSetChanged()
                logger.d(TAG, "locations array $locationsArray")

                searchLocation1.onItemClickListener = object : AdapterView.OnItemClickListener{
                    override fun onItemClick(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                        //todo
                        showToast("Clicked ${locationsArray.get(p2)}")

                    }

                }

            }

        })
    }

    private fun showNoGigLocationFound() = viewBinding.apply {
        locationLayout1.gone()
        locationInfoLayout.root.visible()
        locationShimmerContainer.gone()
        stopShimmer(
            locationShimmerContainer,
            R.id.shimmer_controller
        )

        locationInfoLayout.infoIv.loadImage(R.drawable.ic_no_joining_found)
        locationInfoLayout.infoMessageTv.text = "No Gig Location Found"
    }

    private fun showErrorInLoadingGigLocation(error: String) = viewBinding.apply{
        locationLayout1.gone()
        locationInfoLayout.root.visible()
        locationShimmerContainer.gone()
        stopShimmer(
            locationShimmerContainer,
            R.id.shimmer_controller
        )

        locationInfoLayout.infoIv.loadImage(R.drawable.ic_no_joining_found)
        locationInfoLayout.infoMessageTv.text = error
    }

    private fun showGigLocationAsLoading(){
        viewBinding.locationLayout1.gone()
        viewBinding.locationInfoLayout.root.gone()
        viewBinding.locationShimmerContainer.visible()
        startShimmer(
            viewBinding.locationShimmerContainer,
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