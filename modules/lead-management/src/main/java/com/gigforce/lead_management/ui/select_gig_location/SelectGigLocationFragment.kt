package com.gigforce.lead_management.ui.select_gig_location

import android.os.Bundle
import android.view.View
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
import com.gigforce.lead_management.databinding.SelectGigLocationFragmentBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
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
    private lateinit var userUid: String
    private lateinit var assignGigRequest: AssignGigRequest
    private var currentGigerInfo: GigerProfileCardDVM? = null

    val cityChips = arrayListOf<ChipGroupModel>()
    var cityAndLocations = listOf<JobProfileCityAndLocation>()

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
            userUid = it.getString(LeadManagementConstants.INTENT_EXTRA_USER_ID) ?: return@let
            assignGigRequest =
                it.getParcelable(LeadManagementConstants.INTENT_EXTRA_ASSIGN_GIG_REQUEST_MODEL)
                    ?: return@let
        }

        savedInstanceState?.let {
            userUid = it.getString(LeadManagementConstants.INTENT_EXTRA_USER_ID) ?: return@let
            assignGigRequest =
                it.getParcelable(LeadManagementConstants.INTENT_EXTRA_ASSIGN_GIG_REQUEST_MODEL)
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

    private fun initListeners() {
        viewBinding.toolbar.apply {
            hideActionMenu()
            showTitle("Gig Location")
            setBackButtonListener(View.OnClickListener {
                navigation.popBackStack()
            })
        }

        viewBinding.submitBtn.setOnClickListener {
            cityChips.forEachIndexed { index, chipGroupModel ->
                if (chipGroupModel.isSelected) {
                    assignGigRequest.cityId = cityAndLocations.get(index).id
                    assignGigRequest.cityName = cityAndLocations.get(index).city.toString()
                }
            }
            if (!assignGigRequest.cityId.isNotEmpty()) {
                showToast("Select a city to continue")
            } else {
                logger.d(TAG, "AssignGigRequest $assignGigRequest")
                navigation.navigateTo(
                    LeadManagementNavDestinations.FRAGMENT_SELECT_SHIFT_TIMMINGS, bundleOf(
                        LeadManagementConstants.INTENT_EXTRA_USER_ID to userUid,
                        LeadManagementConstants.INTENT_EXTRA_ASSIGN_GIG_REQUEST_MODEL to assignGigRequest
                    )
                )
            }
        }


        if (currentGigerInfo != null) {
            viewBinding.gigerProfileCard.setProfileCard(currentGigerInfo!!)
        } else {
            viewLifecycleOwner.lifecycleScope.launch {
                viewBinding.gigerProfileCard.setGigerProfileData(userUid)
            }
        }

        viewBinding.searchLocation.setOnItemClickListener { adapterView, view, i, l ->

        }
    }

    private fun showGigLocations(jobProfile: JobProfileDetails) = viewBinding.apply {

        stopShimmer(
            this.locationShimmerContainer,
            R.id.shimmer_controller
        )
        locationLayout.visible()
        locationShimmerContainer.gone()
        locationInfoLayout.root.gone()

        //set chips for gig locations

        cityAndLocations = jobProfile.cityAndLocations
        if (cityAndLocations.isEmpty()) {
            showNoGigLocationFound()
        } else {
            cityChips.clear()
            cityAndLocations.forEachIndexed { index, jobProfileCityAndLocation ->
                jobProfileCityAndLocation.let {
                    cityChips.add(ChipGroupModel(it.city.toString(), -1, index))
                }
            }
            viewBinding.cityChipGroup.removeAllViews()
            viewBinding.cityChipGroup.addChips(cityChips, isSingleSelection = true, true)
            logger.d(TAG, "Job city and locations ${cityChips.toArray()}")
            viewBinding.cityChipGroup.setOnCheckedChangeListener(object :
                ChipGroupComponent.OnCustomCheckedChangeListener {
                override fun onCheckedChangeListener(model: ChipGroupModel) {
                    try {
                        val jobLocations = cityAndLocations.get(model.chipId).jobLocations
                        jobLocations?.let { processJobLocations(it) }

                    } catch (e: Exception) {
                        logger.d(TAG, "Exception while checkedChangeListener ${e.toString()}")
                    }

                }
            })
        }
    }

    private fun processJobLocations(jobLocations: List<JobLocation>) {
        val locationChips = arrayListOf<ChipGroupModel>()
        val groupedJobLocations = jobLocations?.filter {
            it.type != null
        }?.groupBy {
            it.type
        }

        var count = 0;
        groupedJobLocations?.forEach { (localityType, list) ->
            logger.d(TAG, "processing data, Status :  : ${list.size} JobLocations")
            locationChips.add(ChipGroupModel(localityType, -1, count))
            count++
        }

        viewBinding.locationChipGroup.addChips(locationChips, isSingleSelection = true, true)

        //extract dropdown from JobLocations by grouping into one list with type : Locality, Hub, Store
        viewBinding.locationChipGroup.setOnCheckedChangeListener(object :
            ChipGroupComponent.OnCustomCheckedChangeListener {
            override fun onCheckedChangeListener(model1: ChipGroupModel) {
                val locationsArray = arrayListOf<String>()
                jobLocations.forEachIndexed { index, jobLocation ->
                    locationsArray.add(jobLocation.name.toString())
                }
                val arrayAdapter: ArrayAdapter<String> = ArrayAdapter(
                    requireContext(),
                    android.R.layout.simple_spinner_dropdown_item,
                    locationsArray
                )
                viewBinding.searchLocation.threshold = 1
                viewBinding.searchLocation.setAdapter(arrayAdapter)

                viewBinding.searchLocation.setOnItemClickListener { adapterView, view, i, l ->
                    assignGigRequest.location = jobLocations.get(i)
                }
            }

        })
    }

    private fun showNoGigLocationFound() = viewBinding.apply {
        locationLayout.gone()
        locationInfoLayout.root.visible()
        locationShimmerContainer.gone()
        stopShimmer(
            locationShimmerContainer,
            R.id.shimmer_controller
        )

        locationInfoLayout.infoIv.loadImage(R.drawable.ic_no_joining_found)
        locationInfoLayout.infoMessageTv.text = "No Gig Location Found"
    }

    private fun showErrorInLoadingGigLocation(error: String) = viewBinding.apply {
        locationLayout.gone()
        locationInfoLayout.root.visible()
        locationShimmerContainer.gone()
        stopShimmer(
            locationShimmerContainer,
            R.id.shimmer_controller
        )

        locationInfoLayout.infoIv.loadImage(R.drawable.ic_no_joining_found)
        locationInfoLayout.infoMessageTv.text = error
    }

    private fun showGigLocationAsLoading() {
        viewBinding.locationLayout.gone()
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