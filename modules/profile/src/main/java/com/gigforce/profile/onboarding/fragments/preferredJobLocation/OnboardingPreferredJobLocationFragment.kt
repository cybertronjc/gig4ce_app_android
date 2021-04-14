package com.gigforce.profile.onboarding.fragments.preferredJobLocation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestManager
import com.gigforce.profile.R
import com.gigforce.profile.adapters.OnCitySelectedListener
import com.gigforce.profile.adapters.OnboardingCityAdapter
import com.gigforce.profile.adapters.OnboardingMajorCityAdapter
import com.gigforce.profile.models.City
import com.gigforce.profile.models.CityWithImage
import com.gigforce.profile.onboarding.fragments.profilePicture.OnboardingAddProfilePictureFragment
import com.gigforce.profile.viewmodel.OnboardingViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.fragment_preferred_job_location.*

class OnboardingPreferredJobLocationFragment : Fragment(), OnCitySelectedListener {

    private val viewModel: OnboardingViewModel by viewModels()

    private val glide: RequestManager by lazy {
        Glide.with(requireContext())
    }

    private val majorCitiesAdapter: OnboardingMajorCityAdapter by lazy {
        OnboardingMajorCityAdapter(glide).apply {
            setOnCitySelectedListener(this@OnboardingPreferredJobLocationFragment)
        }
    }

    private val cityAdapter: OnboardingCityAdapter by lazy {
        OnboardingCityAdapter(requireContext()).apply {
            setOnCitySelectedListener(this@OnboardingPreferredJobLocationFragment)
        }
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

    override fun onCitySelected(city: City) {

        val delhiId = "HCbEvKJd2aPZaYgenUV7"
        if (city.id == delhiId) {
            val delhiSubLocations = arrayOf(
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

            MaterialAlertDialogBuilder(requireContext())
                    .setTitle("Choose a sub location")
                    .setItems(delhiSubLocations) { _, which ->

                        val selectedSubLocation = delhiSubLocations[which]
                        viewModel.savePreferredJobLocation(
                                cityId = city.id,
                                cityName = city.name,
                                subLocation = selectedSubLocation,
                                stateCode = city.stateCode
                        )
                        Toast.makeText(requireContext(), "Preferred Location Selected", Toast.LENGTH_SHORT).show()
                    }.show()


        } else {

            viewModel.savePreferredJobLocation(
                    cityId = city.id,
                    cityName = city.name,
                    subLocation = null,
                    stateCode = city.stateCode
            )
            Toast.makeText(requireContext(), "Preferred Location Selected", Toast.LENGTH_SHORT).show()
        }
    }

    companion object{

        fun newInstance() : OnboardingPreferredJobLocationFragment {
            return OnboardingPreferredJobLocationFragment()
        }
    }
}