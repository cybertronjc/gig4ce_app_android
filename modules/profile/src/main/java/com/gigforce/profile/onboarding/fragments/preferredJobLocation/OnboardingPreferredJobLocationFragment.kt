package com.gigforce.profile.onboarding.fragments.preferredJobLocation

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestManager
import com.gigforce.profile.R
import com.gigforce.profile.adapters.*
import com.gigforce.profile.models.City
import com.gigforce.profile.models.CityWithImage
import com.gigforce.profile.onboarding.OnboardingFragmentNew
import com.gigforce.profile.onboarding.SpaceItemDecoration
import com.gigforce.profile.viewmodel.OnboardingViewModel
import kotlinx.android.synthetic.main.fragment_preferred_job_location.*

class OnboardingPreferredJobLocationFragment(val formCompletionListener: OnboardingFragmentNew.OnFragmentFormCompletionListener) : Fragment(), OnCitySelectedListener,
    OnboardingFragmentNew.FragmentSetLastStateListener, OnSubCitySelectedListener {

    private val viewModel: OnboardingViewModel by viewModels()

    private val glide: RequestManager by lazy {
        Glide.with(requireContext())
    }

    private var selectedCity : City? = null
    private var spaceItemDecoration: SpaceItemDecoration? = null

    private val majorCitiesAdapter: OnboardingMajorCityAdapter by lazy {
        OnboardingMajorCityAdapter(requireContext(),glide).apply {
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

    private var confirmSubCityList: ArrayList<String>? = null

    fun getSelectedCity() : City? {
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

        major_cities_recyclerview.layoutManager = GridLayoutManager(requireContext(), 4)
        major_cities_recyclerview.adapter = majorCitiesAdapter
        major_cities_recyclerview.addItemDecoration(SpaceItemDecoration(0));

        sub_cities_rv.layoutManager = LinearLayoutManager(requireContext())
        sub_cities_rv.adapter = subCityAdapter
    }

    private fun getMajorCitiesAndOtherCities() {
        viewModel.getAllMajorCities()
        viewModel.getAllCities()
    }

    private fun initListeners() {
        search_cities_et.doOnTextChanged { text, start, before, count ->

            cityAdapter.filter.filter(text)
            majorCitiesAdapter.filter.filter(text)
        }
    }

    private fun initViewModel() {
        viewModel.majorCities
                .observe(viewLifecycleOwner, {
                    showMajorCities(it)
                })

        viewModel.allCities
                .observe(viewLifecycleOwner, Observer {
                    showAllCities(it)
                })
    }

    private fun showAllCities(it: List<City>) {
        cityAdapter.setData(it.sortedBy { it.name })
    }

    private fun showMajorCities(it: List<CityWithImage>) {
        majorCitiesAdapter.setData(it)



//        major_cities_recyclerview.adapter = PreferredLocationMajorCitiesAdapter(
//                requireContext(),
//                R.layout.recycler_item_major_city,
//                it,
//                glide
//        )

    }


    override fun onSubCitySelected(add: Boolean, text: String){
        if (add){
            confirmSubCityList?.add(text)
            Log.d("added", "text" + " list: " + confirmSubCityList.toString())
        }
        else{
            if (confirmSubCityList?.contains(text)!!){
                confirmSubCityList?.remove(text)
                Log.d("removed", "text" + " list: " + confirmSubCityList.toString())
            }
        }
    }

    override fun onCitySelected(city: City) {

        cities_layout.visibility = View.GONE
        sub_cities_layout.visibility = View.VISIBLE

        val delhiId = "HCbEvKJd2aPZaYgenUV7"
        if (city.id == delhiId) {
            val delhiSubLocations = arrayListOf<String>(
                    "Faridabad",
                    "Ghaziabad",
                    "Gurugram",
                    "Gautam Buddh Nagar",
                    "New Delhi",
                    "North Delhi",
                    "West Delhi",
                    "East Delhi",
                    "South Delhi"
            )

            subCityAdapter.setData(delhiSubLocations)


//            MaterialAlertDialogBuilder(requireContext())
//                    .setTitle("Choose a sub location")
//                    .setItems(delhiSubLocations) { _, which ->
//
//                        val selectedSubLocation = delhiSubLocations[which]
//                        city.subLocation = selectedSubLocation
//                        selectedCity = city
//
//                    }.show()


        } else {
            selectedCity = city
        }
    }

    companion object{

        fun newInstance(formCompletionListener: OnboardingFragmentNew.OnFragmentFormCompletionListener) : OnboardingPreferredJobLocationFragment {
            return OnboardingPreferredJobLocationFragment(formCompletionListener)
        }
    }

    override fun lastStateFormFound(): Boolean {
        formCompletionListener.enableDisableNextButton(true)
        return false
    }
}