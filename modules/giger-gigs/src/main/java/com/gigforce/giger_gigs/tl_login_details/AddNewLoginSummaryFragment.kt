package com.gigforce.giger_gigs.tl_login_details

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.gigforce.common_ui.ext.showToast
import com.gigforce.common_ui.repository.ProfileFirebaseRepository
import com.gigforce.core.extensions.gone
import com.gigforce.core.extensions.invisible
import com.gigforce.core.extensions.visible
import com.gigforce.core.navigation.INavigation
import com.gigforce.core.utils.DateHelper
import com.gigforce.core.utils.Lce
import com.gigforce.giger_gigs.LoginSummaryConstants
import com.gigforce.giger_gigs.R
import com.gigforce.giger_gigs.databinding.AddNewLoginSummaryFragmentBinding
import com.gigforce.giger_gigs.models.*
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.add_new_login_summary_fragment.*
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

@AndroidEntryPoint
class AddNewLoginSummaryFragment : Fragment() {

    companion object {
        fun newInstance() = AddNewLoginSummaryFragment()
        const val TAG = "AddNewLoginSummaryFragment"
    }

    @Inject
    lateinit var navigation: INavigation

    private lateinit var viewModel: AddNewLoginSummaryViewModel
    private lateinit var viewBinding: AddNewLoginSummaryFragmentBinding

    private val profileFirebaseRepository: ProfileFirebaseRepository = ProfileFirebaseRepository()

    private var mode = -1

    var arrayAdapter: ArrayAdapter<String>? = null
    var citiesArray = arrayListOf<String>()
    var selectedCity: LoginSummaryCity = LoginSummaryCity()
    var citiesModelArray = listOf<LoginSummaryCity>()
    var businessListToSubmit = listOf<LoginSummaryBusiness>()
    private var loginSummaryDetails: ListingTLModel? = null
    var totalLoginsCount = 0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewBinding = AddNewLoginSummaryFragmentBinding.inflate(inflater, container, false)
        return viewBinding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(this).get(AddNewLoginSummaryViewModel::class.java)
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
        viewModel.checkIfTLAttendanceMarked()

        val c: Date = Calendar.getInstance().time
        viewBinding.dateOfAtt.text = DateHelper.getDateInDDMMYYYY(c)

        if (mode == LoginSummaryConstants.MODE_VIEW) {
            viewBinding.submit.invisible()
        } else {
            viewBinding.submit.visible()
        }

    }

    private fun initToolbar() = viewBinding.apply {

        appBarComp.apply {
            if (mode == LoginSummaryConstants.MODE_VIEW) {
                setAppBarTitle("Login Summary".toString())
            } else if (mode == LoginSummaryConstants.MODE_EDIT) {
                setAppBarTitle("Edit Login Summary".toString())
            } else {
                setAppBarTitle("Add New Login Summary".toString())
            }
            setBackButtonListener(View.OnClickListener {
                activity?.onBackPressed()
            })
        }

    }

    private fun listeners() = viewBinding.apply {

        arrayAdapter = context?.let {
            ArrayAdapter(
                it,
                android.R.layout.simple_spinner_dropdown_item,
                citiesArray
            )
        }
        citySpinner.adapter = arrayAdapter

        submit.setOnClickListener {
            if (submit.text.equals("Checkin Now")){
                navigation.popBackStack()
                navigation.navigateTo("gig/mygig")
            } else {
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


        }

        if (mode == LoginSummaryConstants.MODE_ADD) {
            viewModel.checkIfTLAttendanceMarked()
        } else {
            if (loginSummaryDetails != null) {
                citiesArray.add(loginSummaryDetails?.city?.name.toString())
                citiesModelArray.toMutableList().add(loginSummaryDetails?.city!!)
                citySpinner.isEnabled = false
                citySpinner.invisible()
                cityTextView.visible()
                cityTextView.text = loginSummaryDetails?.city?.name.toString()

                var itemMode = 0
                if (mode == LoginSummaryConstants.MODE_VIEW) {
                    itemMode = 1
                }

                //set businesses
                val list = arrayListOf<LoginSummaryBusiness>()
                loginSummaryDetails?.businessData?.forEachIndexed { index, businessDataReqModel ->
                    list.add(
                        LoginSummaryBusiness(
                            businessDataReqModel.businessId,
                            businessDataReqModel.businessName,
                            businessDataReqModel.legalName,
                            businessDataReqModel.jobProfileId,
                            businessDataReqModel.jobProfileName,
                            businessDataReqModel.gigerCount,
                            businessDataReqModel.updatedBy,
                            itemMode
                        )
                    )
                    if (businessDataReqModel.gigerCount != null && businessDataReqModel.gigerCount != 0){
                        totalLoginsCount += businessDataReqModel.gigerCount
                    }
                }
                loginsCount.setText(totalLoginsCount.toString())
                viewModel.processBusinessList(list)
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

    private fun launchSuccessfullDialog(){
        val dialog = context?.let { MaterialAlertDialogBuilder(it) }
        val customView : View = LayoutInflater.from(context)
            .inflate(R.layout.login_data_submitted_dialog_layout, null, false)

        dialog?.setView(customView)?.setPositiveButton("Done"){ dialog, _ ->
            dialog.dismiss()
            viewBinding.progressBar.visibility = View.GONE
            navigation.popBackStack()

        }?.show()


    }

    private fun submitLoginSummary() {
        val tlUID = FirebaseAuth.getInstance().currentUser?.uid
        val businessList = arrayListOf<BusinessDataReqModel>()
        var isUpdate = false
        isUpdate = mode != LoginSummaryConstants.MODE_ADD
        businessListToSubmit = viewModel.getBusinessListForProcessingData()
        businessListToSubmit.forEachIndexed { index, loginSummaryBusiness ->
            val businessDataReqModel = BusinessDataReqModel(
                loginSummaryBusiness.business_id,
                loginSummaryBusiness.legalName,
                loginSummaryBusiness.businessName,
                selectedCity,
                loginSummaryBusiness.jobProfileId,
                loginSummaryBusiness.jobProfileName,
                loginSummaryBusiness.loginCount
            )
            businessList.add(businessDataReqModel)
        }
        val addNewSummaryReqModel = AddNewSummaryReqModel(
            tlUID.toString(),
            selectedCity,
            businessList,
            isUpdate,
            loginSummaryDetails?.id.toString()

        )
        viewModel.submitLoginSummaryData(addNewSummaryReqModel = addNewSummaryReqModel)
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

        viewModel.checkinMarked.observe(viewLifecycleOwner, Observer {
            val checkIn = it ?: return@Observer
            try {
                if (mode == LoginSummaryConstants.MODE_ADD) {
                    if (checkIn.checkedIn) {
                        viewBinding.apply {
                            citySpinner.visibility = View.VISIBLE
                            businessRV.visibility = View.VISIBLE
                            chooseCity.visibility = View.VISIBLE
                            chooseCityImg.visibility = View.VISIBLE
                            defaultGreyLayout.visibility = View.GONE
                            noDataFound.visibility = View.GONE
                            submit.setText("Submit")
                        }
                        viewModel.getCities()

                    } else {
                        viewBinding.apply {
                            citySpinner.visibility = View.GONE
                            businessRV.visibility = View.GONE
                            chooseCity.visibility = View.GONE
                            chooseCityImg.visibility = View.GONE
                            defaultGreyLayout.visibility = View.GONE
                            noDataFound.visibility = View.VISIBLE
                            submit.setText("Checkin Now")
                        }
                    }
                }
            }catch (e: Exception){

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
                    launchSuccessfullDialog()
                }

                "Already Exists" -> {
                    viewBinding.progressBar.visibility = View.GONE
                    showToast("Data already exists")
                }

                "Error" -> {
                    viewBinding.progressBar.visibility = View.GONE
                    showToast("Error submitting data")
                }

                else -> {
                    viewBinding.progressBar.visibility = View.GONE
                    showToast(result)
                }
            }
        })
    }


    private fun showBusinesses(businessList: List<BusinessListRecyclerItemData>) =
        viewBinding.apply {
            Log.d("List", "Business list $businessList")
            businessRV.collection = businessList
        }


    private fun processCities(content: List<LoginSummaryCity>) {
        citiesModelArray = content
        citiesModelArray.forEach {
            citiesArray.add(it.name)
        }

        arrayAdapter?.notifyDataSetChanged()
    }


}