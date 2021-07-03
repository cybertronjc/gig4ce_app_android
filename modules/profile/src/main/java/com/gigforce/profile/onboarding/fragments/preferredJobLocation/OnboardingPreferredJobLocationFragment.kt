package com.gigforce.profile.onboarding.fragments.preferredJobLocation

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestManager
import com.gigforce.common_ui.ext.showToast
import com.gigforce.core.IEventTracker
import com.gigforce.core.ProfilePropArgs
import com.gigforce.core.TrackingEventArgs
import com.gigforce.core.extensions.getTextChangeAsStateFlow
import com.gigforce.core.extensions.gone
import com.gigforce.core.extensions.visible
import com.gigforce.profile.R
import com.gigforce.profile.adapters.*
import com.gigforce.profile.analytics.OnboardingEvents
import com.gigforce.profile.models.City
import com.gigforce.profile.models.CityWithImage
import com.gigforce.profile.onboarding.OnFragmentFormCompletionListener
import com.gigforce.profile.onboarding.OnboardingFragmentNew
import com.gigforce.profile.onboarding.SpaceItemDecoration
import com.gigforce.profile.viewmodel.OnboardingViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_preferred_job_location.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class OnboardingPreferredJobLocationFragment() : Fragment(),
        OnCitySelectedListener,
        OnboardingFragmentNew.FragmentSetLastStateListener,
        OnSubCitySelectedListener,
        OnboardingFragmentNew.FragmentInteractionListener, OnboardingFragmentNew.SetInterfaceListener {

    private val viewModel: OnboardingViewModel by viewModels()

    @Inject
    lateinit var eventTracker: IEventTracker

    private val glide: RequestManager by lazy {
        Glide.with(requireContext())
    }

    private var selectedCity: City? = null
    private var spaceItemDecoration: SpaceItemDecoration? = null

    private val majorCitiesAdapter: OnboardingMajorCityAdapter by lazy {
        OnboardingMajorCityAdapter(requireContext(), glide).apply {
            setOnCitySelectedListener(this@OnboardingPreferredJobLocationFragment)
        }
    }

    private val cityAdapter: OnboardingCityAdapter by lazy {
        OnboardingCityAdapter(requireContext()).apply {
            setOnCitySelectedListener(this@OnboardingPreferredJobLocationFragment)
        }
    }

    private val subCityAdapter: OnboardingSubCityAdapter by lazy {
        OnboardingSubCityAdapter(requireContext()).apply {
            setOnSubCitySelectedListener(this@OnboardingPreferredJobLocationFragment)
        }
    }

    private var confirmSubCityList: ArrayList<String> = ArrayList()

    fun getSelectedCity(): City? {
        selectedCity?.subLocation = confirmSubCityList.toSet().toList()
        return selectedCity
    }


    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ) = inflater.inflate(R.layout.fragment_preferred_job_location, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initViews()
        initListeners()
        initViewModel()
        getMajorCitiesAndOtherCities()
    }

    private fun initViews() {

        other_cities_recyclerview.layoutManager = LinearLayoutManager(requireContext())
        other_cities_recyclerview.adapter = cityAdapter
        other_cities_recyclerview.itemAnimator = null
        cityAdapter.setData(emptyList())

        major_cities_recyclerview.layoutManager = GridLayoutManager(requireContext(), 4)
        major_cities_recyclerview.adapter = majorCitiesAdapter
//        major_cities_recyclerview.addItemDecoration(SpaceItemDecoration(0));

        sub_cities_rv.layoutManager = LinearLayoutManager(requireContext())
        sub_cities_rv.adapter = subCityAdapter
    }

    private fun getMajorCitiesAndOtherCities() {
        viewModel.getAllMajorCities()
        viewModel.getAllCities()
    }

    private fun initListeners() {
        lifecycleScope.launch {

        search_cities_et.getTextChangeAsStateFlow()
                .debounce(300)
                .distinctUntilChanged()
                .flowOn(Dispatchers.Default)
                .collect { searchString ->
                    Log.d("Search ","Searhcingg...$searchString")

                    cityAdapter.filter.filter(searchString)
                    majorCitiesAdapter.filter.filter(searchString)
                }
        }
    }


    private fun initViewModel() {
        viewModel.majorCities
                .observe(viewLifecycleOwner, {
                    Log.d("cities", it.toString())
                    showMajorCities(it)
                })

        viewModel.allCities
                .observe(viewLifecycleOwner, {
                    showAllCities(it)
                })
    }

    private fun showAllCities(it: List<City>) {
        cityAdapter.setData(it.sortedBy { it.name })
    }

    private fun showMajorCities(it: ArrayList<CityWithImage>) {
        majorCitiesAdapter.setData(it)


//        major_cities_recyclerview.adapter = PreferredLocationMajorCitiesAdapter(
//                requireContext(),
//                R.layout.recycler_item_major_city,
//                it,
//                glide
//        )

    }


    override fun onSubCitySelected(add: Boolean, text: String) {

        val uniqueList = confirmSubCityList.toSet().toList()
        confirmSubCityList.clear()
        uniqueList.forEach { obj -> confirmSubCityList.add(obj) }

        if (add) {
            confirmSubCityList.add(text)
            Log.d("added", "text" + " list: " + confirmSubCityList.toString())
        } else {
            if (confirmSubCityList.contains(text)) {
                confirmSubCityList.remove(text)
                Log.d("removed", "text" + " list: " + confirmSubCityList.toString())
            }
        }
        formCompletionListener?.enableDisableNextButton(confirmSubCityList.size > 0)


    }

    override fun onCitySelected(city: City,isMajorCity : Boolean) {
        formCompletionListener?.enableDisableNextButton(true)
        selectedCity = city
        if(isMajorCity){
            cityAdapter.uncheckedSelection()
        }
        else{
            majorCitiesAdapter.uncheckedSelection()
        }

    }

    companion object {

        fun newInstance() =  OnboardingPreferredJobLocationFragment()

    }

    override fun lastStateFormFound(): Boolean {
        formCompletionListener?.enableDisableNextButton(true)
        if (sub_cities_layout.isVisible) {
            cities_layout.visible()
            sub_cities_layout.gone()
            currentStep = 0
            return true
        }
        return false
    }

    var currentStep = 0
    override fun nextButtonActionFound(): Boolean {
        if (currentStep == 0) {
            if (selectedCity?.subLocationFound == true) {
                cities_layout.visibility = View.GONE
                sub_cities_layout.visibility = View.VISIBLE
                sub_cities_label.setText(selectedCity!!.name)
                confirmSubCityList.clear()
                //get sub cities here and set data to adapter
                viewModel.getSubCities(selectedCity!!.stateCode, selectedCity!!.cityCode)
                viewModel.subCities.observe(viewLifecycleOwner, Observer {
                    if (it != null){
                        subCityAdapter.setData(it, confirmSubCityList)
                    }
                })
                currentStep = 1
                return true
            } else {
                confirmSubCityList.clear()
                selectedCity?.subLocation = confirmSubCityList
                setSelectedCityTracker()
                return false
            }
        }
       // setSelectedCityTracker()
        setSelectedCitySubCityTracker()
        return false
    }

    private fun setSelectedCitySubCityTracker() {
        if (confirmSubCityList.size > 0) {
            selectedCity?.name?.let {
                var map = mapOf("Location" to it, "SubLocation" to confirmSubCityList)
                eventTracker.pushEvent(TrackingEventArgs(OnboardingEvents.EVENT_USER_UPDATED_PREF_LOCATION, map))
                eventTracker.setUserProperty(map)

                if(it.isNotBlank())
                eventTracker.setProfileProperty(ProfilePropArgs("Location", it))

                eventTracker.setProfileProperty(ProfilePropArgs("SubLocation", confirmSubCityList))
            }
        }
    }

    fun setSelectedCityTracker() {
        selectedCity?.name?.let {
            var map = mapOf<String, String>("Location" to it)
            eventTracker.pushEvent(TrackingEventArgs(OnboardingEvents.EVENT_USER_UPDATED_PREF_LOCATION, map))
            eventTracker.setUserProperty(map)

            if(it.isNotBlank())
            eventTracker.setProfileProperty(ProfilePropArgs("Location", it))
            eventTracker.removeUserProperty("SubLocation")
        }

    }

    override fun activeNextButton() {
        if (currentStep == 0) {
            if (selectedCity != null) {
                formCompletionListener?.enableDisableNextButton(true)
            } else {
                formCompletionListener?.enableDisableNextButton(false)
            }
        } else {
            if (confirmSubCityList.size > 0) {
                formCompletionListener?.enableDisableNextButton(true)
            } else {
                formCompletionListener?.enableDisableNextButton(false)
            }
        }
    }
    var formCompletionListener: OnFragmentFormCompletionListener? = null
    override fun setInterface(onFragmentFormCompletionListener: OnFragmentFormCompletionListener) {
        formCompletionListener = formCompletionListener?:onFragmentFormCompletionListener
    }


}