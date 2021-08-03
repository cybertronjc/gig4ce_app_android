package com.gigforce.giger_gigs.tl_login_details

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.lifecycle.Observer
import com.gigforce.common_ui.ext.showToast
import com.gigforce.core.navigation.INavigation
import com.gigforce.core.utils.Lce
import com.gigforce.giger_gigs.LoginSummaryConstants
import com.gigforce.giger_gigs.R
import com.gigforce.giger_gigs.databinding.AddNewLoginSummaryFragmentBinding
import com.gigforce.giger_gigs.databinding.TeamLeaderLoginDetailsFragmentBinding
import com.gigforce.giger_gigs.models.*
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint
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

    private var mode = LoginSummaryConstants.MODE_ADD

    var arrayAdapter: ArrayAdapter<String>? = null
    var citiesArray = arrayListOf<String>()
    var selectedCity: LoginSummaryCity = LoginSummaryCity()
    var citiesModelArray = listOf<LoginSummaryCity>()
    var businessListToSubmit = listOf<LoginSummaryBusiness>()

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

        }

        savedInstanceState?.let {

            mode = it.getInt(LoginSummaryConstants.INTENT_EXTRA_MODE)

        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt(LoginSummaryConstants.INTENT_EXTRA_MODE, mode)
    }

    private fun initializeViews() {
        if (mode == LoginSummaryConstants.MODE_ADD){
            viewModel.getCities()
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
        arrayAdapter = context?.let { ArrayAdapter(it,android.R.layout.simple_spinner_dropdown_item, citiesArray) }
        citySpinner.setAdapter(arrayAdapter)
        citySpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                val cityId = citiesModelArray.get(p2).id
                selectedCity = citiesModelArray.get(p2)
                viewModel.getBusinessByCity(cityId = cityId)
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {

            }

        }

        submit.setOnClickListener {
            if (citySpinner.selectedItem.toString().isEmpty()){
                showToast("Select a city to continue")
            } else {
                //submit data
                submitLoginSummary()
            }
        }
    }

    private fun submitLoginSummary() {
        val tlUID = FirebaseAuth.getInstance().currentUser?.uid
        val businessList = arrayListOf<BusinessDataReqModel>()
        var isUpdate = if (mode == LoginSummaryConstants.MODE_EDIT) true else false
        businessListToSubmit = viewModel.getBusinessListForProcessingData()
        businessListToSubmit.forEachIndexed { index, loginSummaryBusiness ->
            val businessDataReqModel = BusinessDataReqModel(
                loginSummaryBusiness.business_id,
                loginSummaryBusiness.legalName,
                loginSummaryBusiness.businessName,
                selectedCity,
                loginSummaryBusiness.loginCount
            )
            businessList.add(businessDataReqModel)
        }
        val addNewSummaryReqModel = AddNewSummaryReqModel(
            tlUID.toString(),
            selectedCity,
            businessList,
            isUpdate

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

                    if (mode == LoginSummaryConstants.MODE_ADD){
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
                    showBusinesses(state.businessList)
                }

                is BusinessAppViewState.ErrorInLoadingDataFromServer -> {
                    showToast("Error loading businesses")
                }
            }
        })

        viewModel.submitDataState.observe(viewLifecycleOwner, Observer {
            val result = it ?: return@Observer

            when(result) {
                "Loading" -> {
                    showToast("Submitting data")
                }

                "Created" -> {
                    showToast("Data submitted successfully")
                    navigation.popBackStack()
                }

                "Already Exists" -> {
                    showToast("Data already exists")
                }

                "Error" -> {
                    showToast("Error submitting data")
                }
            }
        })

    }

    private fun showBusinesses(businessList: List<BusinessListRecyclerItemData>)  = viewBinding.apply {
        Log.d(TAG, "Business list $businessList")
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