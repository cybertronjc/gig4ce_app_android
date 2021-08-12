package com.gigforce.giger_gigs.tl_login_report

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import com.gigforce.common_ui.ext.showToast
import com.gigforce.common_ui.repository.ProfileFirebaseRepository
import com.gigforce.core.base.BaseFragment2
import com.gigforce.core.datamodels.gigpage.JobProfile
import com.gigforce.core.extensions.gone
import com.gigforce.core.extensions.visible
import com.gigforce.core.navigation.INavigation
import com.gigforce.core.utils.Lce
import com.gigforce.giger_gigs.LoginSummaryConstants
import com.gigforce.giger_gigs.R
import com.gigforce.giger_gigs.databinding.FragmentAddNewLoginReportBinding
import com.gigforce.giger_gigs.models.*
import com.gigforce.giger_gigs.tl_login_report.views.DailyLoginReportItemEdit
import com.gigforce.giger_gigs.tl_login_report.views.DailyLoginReportItemView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_add_new_login_report.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

@AndroidEntryPoint
class AddDailyLoginReportFragment : BaseFragment2<FragmentAddNewLoginReportBinding>(
    fragmentName = "AddDailyLoginReportFragment",
    layoutId = R.layout.fragment_add_new_login_report,
    statusBarColor = R.color.white
) {

    companion object {
        fun newInstance() = AddDailyLoginReportFragment()
        const val TAG = "AddNewLoginSummaryFragment"
    }

    @Inject
    lateinit var navigation: INavigation
    private val viewModel: AddDailyLoginReportViewModel by viewModels()

    private val profileFirebaseRepository: ProfileFirebaseRepository = ProfileFirebaseRepository()

    private var mode = -1

    var selectedCity: LoginSummaryCity = LoginSummaryCity()
    var citiesModelArray = listOf<LoginSummaryCity>()

    private var businessAndProfileList: List<BusinessData> ? = null
    private var loginSummaryDetails: DailyLoginReport? = null
    private val dateFormatter = SimpleDateFormat("dd-MMM-yyyy")
    private val standardDateFormatter = SimpleDateFormat("dd-MM-yyyy")


    override fun viewCreated(
        viewBinding: FragmentAddNewLoginReportBinding,
        savedInstanceState: Bundle?
    ) {
        getDataFromIntents(arguments, savedInstanceState)
        initToolbar()
        initializeViews()
        observer()
        listeners()
    }

    private fun getDataFromIntents(arguments: Bundle?, savedInstanceState: Bundle?) {
        arguments?.let {
            mode = it.getInt(LoginSummaryConstants.INTENT_EXTRA_MODE)
            loginSummaryDetails = it.getParcelable(LoginSummaryConstants.INTENT_LOGIN_SUMMARY)
        }
        savedInstanceState?.let {
            mode = it.getInt(LoginSummaryConstants.INTENT_EXTRA_MODE)
            loginSummaryDetails = it.getParcelable(LoginSummaryConstants.INTENT_LOGIN_SUMMARY)

        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt(LoginSummaryConstants.INTENT_EXTRA_MODE, mode)
        outState.putParcelable(LoginSummaryConstants.INTENT_LOGIN_SUMMARY, loginSummaryDetails)
    }

    private fun initializeViews() {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val userUid = FirebaseAuth.getInstance().uid
                val profileData = profileFirebaseRepository.getProfileData(userUid)
                viewBinding.teamLeaderName.text = profileData.name
            } catch (e: Exception) {

            }
        }

        if (mode == LoginSummaryConstants.MODE_VIEW) {
            viewBinding.submit.gone()
            loginSummaryDetails?.let {

                val date = standardDateFormatter.parse(it.date)
                viewBinding.dateTextview.text = dateFormatter.format(date)
            }
        } else {
            viewBinding.dateTextview.text = dateFormatter.format(Date()) //remove it from here
            viewBinding.submit.visible()

            viewBinding.businessSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {

                    if(viewBinding.businessSpinner.childCount != 0){
                        val businessSelected = viewBinding.businessSpinner.selectedItem as LoginSummaryBusiness?

                        if(businessAndProfileList != null){

                            val jobProfiles = businessAndProfileList!!
                                .filter {
                                    if(businessSelected == null )
                                        true
                                    else
                                        it.businessId == businessSelected.business_id
                                }
                                .map {
                                    JobProfile(
                                        id = it.jobProfileId,
                                        title = it.jobProfileName
                                    )
                                }.distinctBy {
                                    it.id
                                }
                            val jobProfileArrayAdapter = ArrayAdapter(
                                requireContext(),
                                R.layout.item_spinner_text_pink,
                                jobProfiles
                            )
                            jobProfileSpinner.setAdapter(jobProfileArrayAdapter)

                        }
                    }
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {}
            }
        }
    }

    private fun initToolbar() = viewBinding.apply {
        appBar.apply {
            setBackButtonListener(View.OnClickListener {
                activity?.onBackPressed()
            })
        }
    }

    private fun listeners() = viewBinding.apply {


        submit.setOnClickListener {
            if (mode == LoginSummaryConstants.MODE_ADD) {
                if (citySpinner.selectedItem.toString().isEmpty()) {
                    showToast("Select a city to continue")
                } else {
                    //submit data
                    submitLoginSummary()
                }
            } else {
                selectedCity = loginSummaryDetails?.city!!
                submitLoginSummary()
            }

        }

        if (mode == LoginSummaryConstants.MODE_ADD) {
            cityJobProfileControls.visible()
            reportCityOverview.gone()
            addDetailsLabel.text = "Add details"
            cityTextView.gone()

            viewModel.getCities()
        } else if(mode == LoginSummaryConstants.MODE_EDIT) {
            addDetailsLabel.text = "Details"
            cityJobProfileControls.visible()
            reportCityOverview.gone()

            if (loginSummaryDetails != null) {
                citiesModelArray.toMutableList().add(loginSummaryDetails?.city!!)
                citySpinner.isEnabled = false
                citySpinner.gone()

                cityTextView.visible()
                cityTextView.text = loginSummaryDetails?.city?.name ?: ""
                cityOverviewTextview.gone()

                //set businesses
                if (loginSummaryDetails != null) {
                    viewModel.processBusinessList(
                        listOf(loginSummaryDetails!!.businessData!!)
                    )
                }
            }
        } else {

            addDetailsLabel.text = "Details"
            cityJobProfileControls.gone()
            reportCityOverview.visible()

            if (loginSummaryDetails != null) {
                citiesModelArray.toMutableList().add(loginSummaryDetails?.city!!)
                citySpinner.isEnabled = false
                citySpinner.gone()

                cityOverviewTextview.text = "${loginSummaryDetails?.businessData?.jobProfileName} " +
                        "at " +
                        "${loginSummaryDetails?.businessData?.businessName} " +
                        "- " +
                        "${loginSummaryDetails?.city?.name}"

                //set businesses
                if (loginSummaryDetails != null) {
                    viewModel.processBusinessList(
                        listOf(loginSummaryDetails!!.businessData!!)
                    )
                }
            }
        }

        viewBinding.citySpinner.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                    val cityId = citiesModelArray.get(p2).id
                    selectedCity = citiesModelArray.get(p2)
                    if (mode == LoginSummaryConstants.MODE_ADD) {
                        viewModel.getBusinessByCity(cityId = cityId)
                    }
                }

                override fun onNothingSelected(p0: AdapterView<*>?) {

                }
            }
    }

    private fun submitLoginSummary() {
        val tlUID = FirebaseAuth.getInstance().currentUser?.uid
        if (viewBinding.jobProfileSpinner.childCount == 0) {

            MaterialAlertDialogBuilder(requireContext())
                .setTitle("Select job profile")
                .setMessage("Select job profile please")
                .setPositiveButton("Okay") { _, _ -> }
                .show()
            return
        }

        if (viewBinding.businessSpinner.childCount == 0) {

            MaterialAlertDialogBuilder(requireContext())
                .setTitle("Select business")
                .setMessage("Select business please")
                .setPositiveButton("Okay") { _, _ -> }
                .show()
            return
        }


        val businessSelected = viewBinding.businessSpinner.selectedItem as LoginSummaryBusiness
        val jobProfile = viewBinding.jobProfileSpinner.selectedItem as JobProfile
        val dailyTLReportList: MutableList<DailyTlAttendanceReport> = mutableListOf()
        for (i in 0 until viewBinding.bussinessReportFormContainerLayout.childCount) {
            val itemView =
                viewBinding.bussinessReportFormContainerLayout.getChildAt(i) as DailyLoginReportItemEdit

            val businessDataItem = itemView.getDailyReportItem()

            if(!businessDataItem.atLeastOneFieldFilled()){
                MaterialAlertDialogBuilder(requireContext())
                    .setTitle("Select at least one field")
                    .setMessage("Please select at least one field in the form below")
                    .setPositiveButton("Okay") { _, _ -> }
                    .show()
                return
            }

            if(businessDataItem.totalActive < businessDataItem.loginToday){

                MaterialAlertDialogBuilder(requireContext())
                    .setTitle("Login count greater than active")
                    .setMessage("Login gigers count cannot be greater than active count")
                    .setPositiveButton("Okay") { _, _ -> }
                    .show()
                return
            }

            if(businessDataItem.totalActive < businessDataItem.absentToday){

                MaterialAlertDialogBuilder(requireContext())
                    .setTitle("Absent count greater than active")
                    .setMessage("Absent gigers count cannot be greater than active count")
                    .setPositiveButton("Okay") { _, _ -> }
                    .show()
                return
            }

            if(businessDataItem.totalActive < (businessDataItem.absentToday + businessDataItem.loginToday)){

                MaterialAlertDialogBuilder(requireContext())
                    .setTitle("Absent + Login count greater than active")
                    .setMessage("Absent and Login gigers count cannot be greater than active count")
                    .setPositiveButton("Okay") { _, _ -> }
                    .show()
                return
            }

            businessDataItem.city = selectedCity
            businessDataItem.jobProfileId = jobProfile.id
            businessDataItem.jobProfileName = jobProfile.title
            businessDataItem.businessId = businessSelected.business_id
            businessDataItem.businessName = businessSelected.businessName
            businessDataItem.legalName = businessSelected.legalName

            dailyTLReportList.add(
                DailyTlAttendanceReport(
                    uID = tlUID.toString(),
                    city = selectedCity,
                    businessData = listOf(businessDataItem),
                    update = false,
                    loginSummaryDetails?.id.toString()
                )
            )
        }

        viewModel.submitLoginReportData(
            addNewSummaryReqModel = dailyTLReportList
        )
    }

    private fun observer() = viewBinding.apply {
        viewModel.cities.observe(viewLifecycleOwner, Observer {
            when (it) {
                Lce.Loading -> {
                    citySpinner.isEnabled = false
                }

                is Lce.Content -> {
                    showToast("getting cities")

                    if (mode == LoginSummaryConstants.MODE_ADD) {
                        citySpinner.isEnabled = true
                        processCities(it.content)

                    } else {
                        citySpinner.isEnabled = false
                    }
                }

                is Lce.Error -> {
                    showToast("${it.error}")
                }
            }
        })

        viewModel.viewState.observe(viewLifecycleOwner, Observer {
            val state = it ?: return@Observer
            when (state) {
                is BusinessAppViewState.LoadingDataFromServer -> {
                    showToast("Loading businesses")
                }

                is BusinessAppViewState.BusinessListLoaded -> {
                    Log.d("List1", "Business list ${state.businessList}")
                    showBusinesses(state.businessList)
                }

                is BusinessAppViewState.ErrorInLoadingDataFromServer -> {

                    showToast("Error loading businesses")
                }
            }
        })

        viewModel.submitDataState.observe(viewLifecycleOwner, Observer {
            val result = it ?: return@Observer

            when (result) {
                "Loading" -> {
                    showToast("Submitting data")
                    viewBinding.progressBar.visibility = View.VISIBLE
                }

                "Created" -> {
                    showToast("Data submitted successfully")
                    viewBinding.progressBar.visibility = View.GONE
                    navigation.popBackStack()
                }

                "Already Exists" -> {
                    viewBinding.progressBar.visibility = View.GONE
                    showToast("Data already exists")
                }

                "Error" -> {
                    viewBinding.progressBar.visibility = View.GONE
                    showToast("Error submitting data")
                }
            }
        })
    }


    private fun showBusinesses(businessList: List<BusinessData>) = viewBinding.apply {
        Log.d("List", "Business list $businessList")
        businessAndProfileList = businessList

        if(mode == LoginSummaryConstants.MODE_VIEW){

            bussinessReportFormContainerLayout.removeAllViews()
            if (businessList.isNotEmpty()) {

                val bussinessData = businessList.first()

                val view = DailyLoginReportItemView(requireContext(), null)
                bussinessReportFormContainerLayout.addView(view)

                view.showData(
                    bussinessData
                )
            }

        } else {

            val business = businessList.map {
                LoginSummaryBusiness(
                    business_id = it.businessId ?: "",
                    businessName = it.businessName ?: "",
                    legalName = it.legalName ?: ""
                )
            }.distinctBy {
                it.business_id
            }

            val businessArrayAdapter = ArrayAdapter(
                requireContext(),
                R.layout.item_spinner_text_pink,
                business
            )
            businessSpinner.setAdapter(businessArrayAdapter)


            val firstBussiness = business.firstOrNull()
            //show job profiles in view
            val jobProfiles = businessList
                .filter {
                    if(firstBussiness == null )
                        true
                    else
                        it.businessId == firstBussiness.business_id
                }
                .map {
                JobProfile(
                    id = it.jobProfileId,
                    title = it.jobProfileName
                )
            }.distinctBy {
                it.id
            }
            val jobProfileArrayAdapter = ArrayAdapter(
                requireContext(),
                R.layout.item_spinner_text_pink,
                jobProfiles
            )
            jobProfileSpinner.setAdapter(jobProfileArrayAdapter)

            bussinessReportFormContainerLayout.removeAllViews()
            if (businessList.isNotEmpty()) {

                val bussinessData = businessList.first()

                val view = DailyLoginReportItemEdit(requireContext(), null)
                bussinessReportFormContainerLayout.addView(view)

                view.showData(
                    bussinessData
                )
            }
        }
    }

    private fun processCities(content: List<LoginSummaryCity>) {
        citiesModelArray = content

        val arrayAdapter = context?.let {
            ArrayAdapter(
                it,
                R.layout.item_spinner_text_pink,
                citiesModelArray
            )
        }
        citySpinner.setAdapter(arrayAdapter)
    }


}