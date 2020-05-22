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
        viewModel.preferenceDataModel.observe(this, Observer { preferenceData ->
            viewModel.setPreferenceDataModel(preferenceData)
            initializeViews()
        })

    }

    private fun observeProfileData() {
        viewModel.profileDataModel.observe(this, Observer { profileData ->
            viewModel.setProfileDataModel(profileData)
            initializeViews()
        })

    }

    private fun initializeViews() {
        preferenceDataModel = viewModel.getPreferenceDataModel()
        profileDataModel = viewModel.getProfileDataModel()
        switch1.isChecked = preferenceDataModel.isWorkFromHome
        textView51.text = convertAddressToString(viewModel.getPermanentAddress())
        textView57.text = convertAddressToString(viewModel.getCurrentAddress())
        if (viewModel.getCurrentAddress()!!.isEmpty()) {
            switch2.isEnabled=false
            textView109.text = getString(R.string.add_current_address)
        } else {
            switch2.isEnabled=true
            textView109.text = getString(R.string.around_current_address)
        }
    }

    private fun convertAddressToString(address: AddressModel?): String {
        Log.e("ADDRESS",address!!.firstLine)
        return if (address!!.isEmpty() || address == null)
            getString(R.string.add_address)
        else
            address.firstLine + "," + address.secondLine + "," + address.city + "," + address.state + ". " + address.pincode
    }

    private fun listener() {
        imageView10.setOnClickListener(View.OnClickListener { activity?.onBackPressed() })
        addloc.setOnClickListener(View.OnClickListener {
            if (viewModel.getCurrentAddress()!!.isEmpty())
                showToast(getString(R.string.add_current_address))
            else navigate(R.id.preferredLocationFragment)
        })
        textView49.setOnClickListener(View.OnClickListener {
            if (profileDataModel.address.home.isEmpty()) navigate(R.id.permanentAddressEditFragment)
            else navigate(R.id.permanentAddressViewFragment)
        })
        textView55.setOnClickListener(View.OnClickListener {
            if(profileDataModel.address.current.isEmpty()) navigate(R.id.currentAddressEditFragment)
            else navigate(R.id.currentAddressViewFragment)
        })
        switch1.setOnClickListener { view ->
            var isChecked = (view as Switch).isChecked
            viewModel.setWorkFromHome(isChecked)
        }
    }

}