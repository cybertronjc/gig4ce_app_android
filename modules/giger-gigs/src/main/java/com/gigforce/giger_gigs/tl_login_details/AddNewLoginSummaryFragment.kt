package com.gigforce.giger_gigs.tl_login_details

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.core.os.bundleOf
import androidx.core.view.get
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.gigforce.common_ui.ext.showToast
import com.gigforce.common_ui.repository.ProfileFirebaseRepository
import com.gigforce.core.StringConstants
import com.gigforce.core.extensions.gone
import com.gigforce.core.extensions.invisible
import com.gigforce.core.extensions.visible
import com.gigforce.core.navigation.INavigation
import com.gigforce.core.utils.DateHelper
import com.gigforce.core.utils.Lce
import com.gigforce.core.utils.NavFragmentsData
import com.gigforce.giger_gigs.LoginSummaryConstants
import com.gigforce.giger_gigs.R
import com.gigforce.giger_gigs.databinding.AddNewLoginSummaryFragmentBinding
import com.gigforce.giger_gigs.models.*
import com.gigforce.giger_gigs.tl_login_details.views.BusinessRecyclerItemView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.add_new_login_summary_fragment.*
import kotlinx.android.synthetic.main.gig_details_item.view.*
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject
import kotlin.collections.ArrayList

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

    val map = mutableMapOf<String, String>()

    var arrayAdapter: ArrayAdapter<String>? = null
    var citiesArray = arrayListOf<String>()
    var selectedCity: LoginSummaryCity = LoginSummaryCity()
    var citiesModelArray = listOf<LoginSummaryCity>()
    var businessListToSubmit = listOf<LoginSummaryBusiness>()
    var businessListToProcess = listOf<LoginSummaryBusiness>()
    private var loginSummaryDetails: ListingTLModel? = null
    var totalLoginsCount = 0
    var citiesMap = mutableMapOf<String, Int>()

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

        viewBinding.root.foreground.alpha = 0
        viewBinding.submit.transformationMethod = null
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
        viewBinding.dateOfAtt.text = DateHelper.getDateInDDMMMYYYY(c)

        if (mode == LoginSummaryConstants.MODE_VIEW) {
            viewBinding.submit.invisible()
        } else {
            viewBinding.submit.visible()
        }

    }

    private fun initToolbar() = viewBinding.apply {

        appBarComp.apply {
            if (mode == LoginSummaryConstants.MODE_VIEW) {
                setAppBarTitle(context.getString(R.string.login_summary))
            } else if (mode == LoginSummaryConstants.MODE_EDIT) {
                setAppBarTitle(context.getString(R.string.edit_login_summary).toString())
            } else {
                setAppBarTitle(context.getString(R.string.add_new_login_summary).toString())
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
        citySpinner.setAdapter(arrayAdapter)
        citySpinner.setOnFocusChangeListener { view, b ->
            if (b){
                citySpinner.showDropDown()
            }
        }
        chooseCityImg.setOnClickListener {
            citySpinner.showDropDown()
        }

//        citySpinner.adapter = arrayAdapter

        submit.setOnClickListener {
            if (submit.text.equals("Check In")){
                navigation.popBackStack()
                navigation.navigateTo("gig/mygig")
            } else {
                if (mode == LoginSummaryConstants.MODE_ADD) {
                    if (citySpinner.text.toString().isEmpty()) {
                        showToast(getString(R.string.select_a_city_to_continue))
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
                //citiesArray.add("Choose City...")
                citiesArray.add(loginSummaryDetails?.city?.name.toString())
                citiesModelArray.toMutableList().add(loginSummaryDetails?.city!!)
                citySpinner.isEnabled = false
                chooseCityImg.isEnabled = false
                chooseCityImg.invisible()
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

        viewBinding.citySpinner.onItemClickListener = object : AdapterView.OnItemClickListener {
            override fun onItemClick(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
//                if (p2 != 0){
                    Log.d("cityModelArray", "cities $citiesModelArray  string array: $citiesArray")
                    if (p2 <= citiesModelArray.size && citySpinner.text.toString().isNotEmpty()){
                        val actualIndex = citiesMap.get(citySpinner.text.toString().trim())
                        val cityId = actualIndex?.let { citiesModelArray.get(it).id }
                        selectedCity = actualIndex?.let { citiesModelArray.get(it) }!!
                        viewBinding.businessRV.visibility = View.VISIBLE
                        viewBinding.submit.visibility = View.VISIBLE
                        if (mode == LoginSummaryConstants.MODE_ADD) {
                            cityId?.let { viewModel.getBusinessByCity(cityId = it) }
                        }
                    }

//                } else {
//                    viewBinding.businessRV.visibility = View.INVISIBLE
//                    viewBinding.submit.visibility = View.INVISIBLE
//                }
            }

        }

//        viewBinding.citySpinner.onItemSelectedListener =
//            object : AdapterView.OnItemSelectedListener {
//                override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
//                    if (p2 != 0){
//                        Log.d("cityModelArray", "cities $citiesModelArray  string array: $citiesArray")
//                        if (p2 <= citiesModelArray.size + 1){
//                            val cityId = citiesModelArray.get(p2 - 1).id
//                            selectedCity = citiesModelArray.get(p2 - 1)
//                            viewBinding.businessRV.visibility = View.VISIBLE
//                            viewBinding.submit.visibility = View.VISIBLE
//                            if (mode == LoginSummaryConstants.MODE_ADD) {
//                                viewModel.getBusinessByCity(cityId = cityId)
//                            }
//                        }
//
//                    } else {
//                        viewBinding.businessRV.visibility = View.INVISIBLE
//                        viewBinding.submit.visibility = View.INVISIBLE
//                    }
//
//
//                }
//
//                override fun onNothingSelected(p0: AdapterView<*>?) {
//
//                }
//
//            }
    }

    private fun launchSuccessfullDialog(){
        val dialog = context?.let { MaterialAlertDialogBuilder(it) }
        val customView : View = LayoutInflater.from(context)
            .inflate(R.layout.login_data_submitted_dialog_layout, null, false)

        viewBinding.root.foreground.alpha = 200
        dialog?.setView(customView)?.setCancelable(false)?.setPositiveButton(getString(R.string.done)){ dialog, _ ->
            dialog.dismiss()
            viewBinding.progressBar.visibility = View.GONE
            navigation.popBackStack()
//            navigation.navigateTo("gig/tlLoginDetails", bundleOf(
//                LoginSummaryConstants.CAME_BACK_FROM_ADD to true
//            ))
//            var navFragmentsData = activity as NavFragmentsData
//            navFragmentsData.setData(
//                bundleOf(
//                    LoginSummaryConstants.CAME_BACK_FROM_ADD to true
//                )
//            )
        }?.show()


    }

    private fun submitLoginSummary() {
        val tlUID = FirebaseAuth.getInstance().currentUser?.uid
//        val businessList = arrayListOf<BusinessDataReqModel>()
        var isUpdate = false
        isUpdate = mode != LoginSummaryConstants.MODE_ADD
//        businessListToSubmit = viewModel.getBusinessListForProcessingData()
//        businessListToSubmit.forEachIndexed { index, loginSummaryBusiness ->
//            val businessDataReqModel = BusinessDataReqModel(
//                loginSummaryBusiness.business_id,
//                loginSummaryBusiness.legalName,
//                loginSummaryBusiness.businessName,
//                selectedCity,
//                loginSummaryBusiness.jobProfileId,
//                loginSummaryBusiness.jobProfileName,
//                loginSummaryBusiness.loginCount
//            )
//            businessList.add(businessDataReqModel)
//        }
        val businessList = getDataToSubmit()
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
                    showToast(getString(R.string.getting_cities))

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
                            chooseCityImg.visibility = View.VISIBLE
                            noDataFound.visibility = View.GONE
                            submit.setText("Submit")
                        }
                        viewModel.getCities()

                    } else {
                        viewBinding.apply {
                            citySpinner.visibility = View.GONE
                            businessRV.visibility = View.GONE
                            chooseCityImg.visibility = View.GONE
                            noDataFound.visibility = View.VISIBLE
                            submit.setText("Check In")
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
                    showToast(getString(R.string.loading_businesses))
                }

                is BusinessAppViewState.BusinessListLoaded -> {
                    Log.d("List1", "Business list ${state.businessList}")
                    showBusinesses(state.businessList)
                }

                is BusinessAppViewState.ErrorInLoadingDataFromServer -> {

                    showToast(getString(R.string.error_loading_businesses))
                }
            }
        })

        viewModel.submitDataState.observe(viewLifecycleOwner, Observer {
            val result = it ?: return@Observer

            when (result) {
                "Loading" -> {
                    showToast(getString(R.string.submitting_data))
                    viewBinding.progressBar.visibility = View.VISIBLE
                }

                "Created" -> {
                    showToast(getString(R.string.data_submitted_successfully))
                    launchSuccessfullDialog()
                }

                "Already Exists" -> {
                    viewBinding.progressBar.visibility = View.GONE
                    showToast(getString(R.string.data_already_exists))
                }

                "Error" -> {
                    viewBinding.progressBar.visibility = View.GONE
                    showToast(getString(R.string.error_submitting_data))
                }

                else -> {
                    viewBinding.progressBar.visibility = View.GONE
                    showToast(result)
                }
            }
        })

        viewModel.totalCount.observe(viewLifecycleOwner, Observer {
            val count = it ?: return@Observer
            count?.let {
                viewBinding.submit.text = "Submit ($count Logins)"
            }
        })
    }


    private fun showBusinesses(businessList: List<LoginSummaryBusiness>) =
        viewBinding.apply {
            Log.d("List", "Business list $businessList")
//            businessRV.collection = businessList
            businessListToProcess = businessList
            businessLoginLayout.removeAllViews()
            map.clear()
            submit.setText("Submit")
            businessList.forEachIndexed { index, loginSummaryBusiness ->
                val view = BusinessRecyclerItemView(requireContext(), null)
                businessLoginLayout.addView(view)
                //need to set listener
                view.setOnQuantityTextChangeListener(object: BusinessRecyclerItemView.QuantityTextChangeListener{
                    override fun onQuantityTextChanged(text: String, tag: String) {
                        map.put(tag, text)
                        Log.d("map", "getting listener $map")
                        setTotalSumFromMap()
                    }

                }, index.toString())
                view.showData(
                    BusinessListRecyclerItemData.BusinessRecyclerItemData(
                        businessId = loginSummaryBusiness.business_id,
                        businessName = loginSummaryBusiness.businessName,
                        legalName = loginSummaryBusiness.legalName,
                        jobProfileId = loginSummaryBusiness.jobProfileId.toString(),
                        jobProfileName = loginSummaryBusiness.jobProfileName.toString(),
                        loginCount = loginSummaryBusiness.loginCount,
                        updatedBy = loginSummaryBusiness.updatedBy.toString(),
                        addNewLoginSummaryViewModel = viewModel,
                        itemView = loginSummaryBusiness.itemMode
                    )
                )
                if (loginSummaryBusiness.loginCount != null){
                    map.put(index.toString(), loginSummaryBusiness.loginCount.toString())
                }
            }
            setTotalSumFromMap()
        }

    fun setTotalSumFromMap(){
        var count = 0
        map.forEach {
            it.value.toIntOrNull()?.let {
                count += it
            }
        }
        Log.d("count", "count $count , map: $map")

        viewBinding.submit.setText("Submit ($count Logins)")
        viewBinding.loginsCount.setText("$count")

    }

    fun getDataToSubmit() : ArrayList<BusinessDataReqModel> {
        val businessList = arrayListOf<BusinessDataReqModel>()
        businessListToProcess.forEachIndexed { index, loginSummaryBusiness ->
            val view = viewBinding.businessLoginLayout.get(index) as BusinessRecyclerItemView
            val loginSummary = view.getTLLoginSummary()
            val businessDataReqModel = BusinessDataReqModel(
                loginSummary.businessId,
                loginSummary.legalName,
                loginSummary.businessName,
                selectedCity,
                loginSummary.jobProfileId,
                loginSummary.jobProfileName,
                loginSummary.loginCount
            )
            businessList.add(businessDataReqModel)
        }
        return businessList
    }
    private fun processCities(content: List<LoginSummaryCity>) {
        citiesArray.clear()
        citiesModelArray.toMutableList().clear()
        //citiesArray.add("Choose City...")
        citiesModelArray = content
        citiesModelArray.forEachIndexed { index, loginSummaryCity ->

            citiesArray.add(loginSummaryCity.name)
            citiesMap.put(loginSummaryCity.name, index)
        }

        arrayAdapter?.notifyDataSetChanged()
    }


}