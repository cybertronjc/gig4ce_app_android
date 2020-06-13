package com.gigforce.app.modules.preferences.location

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Switch
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.gigforce.app.R
import com.gigforce.app.core.base.BaseFragment
import com.gigforce.app.modules.preferences.SharedPreferenceViewModel
import com.gigforce.app.modules.preferences.prefdatamodel.PreferencesDataModel
import com.gigforce.app.modules.profile.models.AddressModel
import com.gigforce.app.modules.profile.models.ProfileData
import kotlinx.android.synthetic.main.location_settings_fragment.*

class LocationFragment : BaseFragment() {
    companion object {
        fun newInstance() = LocationFragment()
    }

    private lateinit var viewModel: SharedPreferenceViewModel
    private lateinit var preferenceDataModel: PreferencesDataModel
    private lateinit var profileDataModel: ProfileData

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflateView(R.layout.location_settings_fragment, inflater, container)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(SharedPreferenceViewModel::class.java)
        initializeViews()
        listener()
        observePreferenceData()
        observeProfileData()
    }

    private fun observePreferenceData() {
        viewModel.preferenceDataModel.observe(viewLifecycleOwner, Observer { preferenceData ->
            viewModel.setPreferenceDataModel(preferenceData)
            initializeViews()
        })

    }

    private fun observeProfileData() {
        viewModel.userProfileData.observe(viewLifecycleOwner, Observer { profileData ->
            viewModel.setProfileDataModel(profileData)
            initializeViews()
        })
    }

    private fun initializeViews() {
        preferenceDataModel = viewModel.getPreferenceDataModel()
        profileDataModel = viewModel.getProfileDataModel()
        workFromHomeSwitch.isChecked = preferenceDataModel.isWorkFromHome
        textView79.text = convertAddressToString(viewModel.getPermanentAddress())
        textView81.text = convertAddressToString(viewModel.getCurrentAddress())
        if (viewModel.getCurrentAddress()!!.isEmpty()) {
            arroundCurrentAddSwitch.isEnabled = false
            preferredDistanceTV.text = getString(R.string.add_current_address)
        } else {
            arroundCurrentAddSwitch.isEnabled = true
            preferredDistanceTV.text = viewModel.getCurrentAddress()?.preferred_distance.toString()+" KM Around the Current Address"
        }
        arroundCurrentAddSwitch.isChecked = viewModel.getCurrentAddress()?.preferredDistanceActive!!
    }

    private fun convertAddressToString(address: AddressModel?): String {
        Log.e("ADDRESS", address!!.firstLine)
        return if (address!!.isEmpty() || address == null)
            getString(R.string.add_address)
        else
            address.firstLine + "," + address.secondLine + "," + address.city + "," + address.state + ". " + address.pincode
    }

    private fun listener() {
        back_arrow_iv.setOnClickListener(View.OnClickListener { activity?.onBackPressed() })
        imageview_plus.setOnClickListener(View.OnClickListener {
            if (viewModel.getCurrentAddress()!!.isEmpty()) {
                navigate(R.id.preferredLocationFragment)
                navigate(R.id.currentAddressEditFragment)
            }
            else navigate(R.id.preferredLocationFragment)
        })
        permanentAddLayout.setOnClickListener(View.OnClickListener {
            if (profileDataModel.address.home.isEmpty()) navigate(R.id.permanentAddressEditFragment)
            else navigate(R.id.permanentAddressViewFragment)
        })
        currentAddLayout.setOnClickListener(View.OnClickListener {
            if (profileDataModel.address.current.isEmpty()) navigate(R.id.currentAddressEditFragment)
            else navigate(R.id.currentAddressViewFragment)
        })
        workFromHomeSwitch.setOnClickListener { view ->
            var isChecked = (view as Switch).isChecked
            if(viewModel.getCurrentAddress()!!.isEmpty()){
                navigate(R.id.currentAddressEditFragment)
            }else
            viewModel.setWorkFromHome(isChecked)
        }
        arroundCurrentAddressLayout.setOnClickListener { view ->
            if(!viewModel.getCurrentAddress()!!.isEmpty()){
                navigate(R.id.arrountCurrentAddress)
            }
            else{
                showToast("Please add current address")
            }
        }
        arroundCurrentAddSwitch.setOnClickListener { view ->
            var isChecked = (view as Switch).isChecked
            if(viewModel.getCurrentAddress()?.preferred_distance!=0)
            viewModel.setCurrentAddressPreferredDistanceActive(isChecked)
            else
                navigate(R.id.arrountCurrentAddress)
        }
    }

}