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
                                }.
                                distinctBy {
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

            if(mode == LoginSummaryConstants.MODE_ADD)
                this.setAppBarTitle(context.getString(R.string.add_login_report_giger_gigs))
            else if(mode == LoginSummaryConstants.MODE_EDIT)
                this.setAppBarTitle(context.getString(R.string.edit_login_report_giger_gigs))
            else
                this.setAppBarTitle(context.getString(R.string.login_report_giger_gigs))
        }
    }

    private fun listeners() = viewBinding.apply {


        submit.setOnClickListener {
            if (mode == LoginSummaryConstants.MODE_ADD) {
                if (citySpinner.selectedItem.toString().isEmpty()) {
                    showToast(getString(R.string.select_city_to_continue_giger_gigs))
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
            addDetailsLabel.text = getString(R.string.add_details_giger_gigs)
            cityTextView.gone()
            bussinessTextView.gone()
            jobProfileTextView.gone()

            viewModel.getCities()
        } else if(mode == LoginSummaryConstants.MODE_EDIT) {
            addDetailsLabel.text = getString(R.string.details_giger_gigs)
            cityJobProfileControls.visible()
            reportCityOverview.gone()

            if (loginSummaryDetails != null) {
                citiesModelArray.toMutableList().add(loginSummaryDetails?.city!!)

                citySpinner.isEnabled = false
                citySpinner.gone()
                cityTextView.visible()
                cityTextView.text = loginSummaryDetails?.city?.name ?: ""

                businessSpinner.isEnabled = false
                businessSpinner.gone()
                bussinessTextView.visible()
                bussinessTextView.text = loginSummaryDetails?.businessData?.businessName ?: ""

                jobProfileSpinner.isEnabled = false
                jobProfileSpinner.gone()
                jobProfileTextView.visible()
                jobProfileTextView.text = loginSummaryDetails?.businessData?.jobProfileName ?: ""

                cityOverviewTextview.gone()

                //set businesses
                if (loginSummaryDetails != null) {
                    viewModel.processBusinessList(
                        listOf(loginSummaryDetails!!.businessData!!)
                    )
                }
            }
        } else {

            addDetailsLabel.text = getString(R.string.details_giger_gigs)
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
        if (mode == LoginSummaryConstants.MODE_ADD &&
            viewBinding.jobProfileSpinner.childCount == 0
        ) {

            MaterialAlertDialogBuilder(requireContext())
                .setTitle(getString(R.string.select_job_profile_giger_gigs))
                .setMessage(getString(R.string.select_job_please_giger_gigs))
                .setPositiveButton(getString(R.string.okay_giger_gigs)) { _, _ -> }
                .show()
            return
        }

        if (mode == LoginSummaryConstants.MODE_ADD &&
            viewBinding.businessSpinner.childCount == 0
        ) {

            MaterialAlertDialogBuilder(requireContext())
                .setTitle(getString(R.string.select_business_giger_gigs))
                .setMessage(getString(R.string.select_business_please_giger_gigs))
                .setPositiveButton(getString(R.string.okay_giger_gigs)) { _, _ -> }
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
                    .setTitle(getString(R.string.select_one_filed_giger_gigs))
                    .setMessage(getString(R.string.please_select_atleast_one_field_giger_gigs))
                    .setPositiveButton(getString(R.string.okay_giger_gigs)) { _, _ -> }
                    .show()
                return
            }

            if(businessDataItem.totalActive != null &&
                businessDataItem.loginToday != null &&
                businessDataItem.totalActive  < businessDataItem.loginToday){

                MaterialAlertDialogBuilder(requireContext())
                    .setTitle(getString(R.string.login_count_greater_giger_gigs))
                    .setMessage(getString(R.string.login_could_be_greater_giger_gigs))
                    .setPositiveButton(getString(R.string.okay_giger_gigs)) { _, _ -> }
                    .show()
                return
            }

            if(businessDataItem.totalActive != null &&
                businessDataItem.absentToday != null &&
                businessDataItem.totalActive < businessDataItem.absentToday){

                MaterialAlertDialogBuilder(requireContext())
                    .setTitle(getString(R.string.absent_greater_giger_gigs))
                    .setMessage(getString(R.string.absent_gigers_could_greater_giger_gigs))
                    .setPositiveButton(getString(R.string.okay_giger_gigs)) { _, _ -> }
                    .show()
                return
            }

            if(businessDataItem.totalActive != null &&
                businessDataItem.absentToday != null &&
                businessDataItem.loginToday != null &&
                businessDataItem.totalActive < (businessDataItem.absentToday + businessDataItem.loginToday)){

                MaterialAlertDialogBuilder(requireContext())
                    .setTitle(getString(R.string.absent_login_greater_giger_gigs))
                    .setMessage(getString(R.string.absent_login_could_greater_giger_gigs))
                    .setPositiveButton(getString(R.string.okay_giger_gigs)) { _, _ -> }
                    .show()
                return
            }

            businessDataItem.city = selectedCity
            businessDataItem.jobProfileId = if(mode == LoginSummaryConstants.MODE_ADD )
                jobProfile.id
            else
                loginSummaryDetails?.businessData?.jobProfileId

            businessDataItem.jobProfileName = if(mode == LoginSummaryConstants.MODE_ADD )
                jobProfile.title
            else
                loginSummaryDetails?.businessData?.jobProfileName


            businessDataItem.businessId = if(mode == LoginSummaryConstants.MODE_ADD )
                businessSelected.business_id
            else
                loginSummaryDetails?.businessData?.businessId

            businessDataItem.businessName = if(mode == LoginSummaryConstants.MODE_ADD )
                businessSelected.businessName
            else
                loginSummaryDetails?.businessData?.businessName


            businessDataItem.legalName = if(mode == LoginSummaryConstants.MODE_ADD )
                businessSelected.legalName
            else
                loginSummaryDetails?.businessData?.legalName

            dailyTLReportList.add(
                DailyTlAttendanceReport(
                    uID = tlUID.toString(),
                    city = selectedCity,
                    businessData = listOf(businessDataItem),
                    update = false
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
                }

                is BusinessAppViewState.BusinessListLoaded -> {
                    Log.d("List1", "Business list ${state.businessList}")
                    showBusinesses(state.businessList)
                }

                is BusinessAppViewState.ErrorInLoadingDataFromServer -> {

                    showToast(getString(R.string.error_loading_businesses_giger_gigs))
                }
            }
        })

        viewModel.submitDataState.observe(viewLifecycleOwner, Observer {
            val result = it ?: return@Observer

            when (result) {
                is Lce.Content -> {

                    viewBinding.progressBar.visibility = View.GONE
                    showToast(result.content)
                    navigation.popBackStack()
                }
                is Lce.Error -> {

                    viewBinding.progressBar.visibility = View.GONE
                    MaterialAlertDialogBuilder(requireContext())
                        .setTitle(getString(R.string.unable_to_submit_giger_gigs))
                        .setMessage(result.error)
                        .setPositiveButton(getString(R.string.okay_giger_gigs)){ _, _ -> }
                        .show()
                }
                Lce.Loading -> {

                    viewBinding.progressBar.visibility = View.VISIBLE
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