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
import kotlinx.android.synthetic.main.current_address_edit_fragment.*

class CurrentAddressEditFragment : BaseFragment() {
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
        return inflateView(R.layout.current_address_edit_fragment, inflater, container)
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
        imageView10.setOnClickListener { activity?.onBackPressed() }
        var currentAddress = viewModel.getCurrentAddress()
        var permanentAddress = viewModel.getPermanentAddress()
        populateAddress(currentAddress!!)
        switch1.isEnabled = !permanentAddress!!.isEmpty()
        switch1.isChecked = currentAddress!!.isSame(viewModel.getPermanentAddress()!!)
    }

    private fun populateAddress(address: AddressModel){
        editText1.setText(address.firstLine)
        editText2.setText(address.secondLine)
        editText3.setText(address.area)
        editText4.setText(address.city)
        editText5.setText(address.state)
        editText6.setText(address.pincode)

    }

    private fun convertAddressToString(address: AddressModel?): String {
        Log.e("ADDRESS",address!!.firstLine)
        return if (address!!.isEmpty() || address == null)
            getString(R.string.add_address)
        else
            address.firstLine + "," + address.secondLine + "," + address.city + "," + address.state + ". " + address.pincode
    }

    private fun listener() {
        if(switch1.isEnabled) {
            switch1.setOnClickListener { view ->
                var isChecked = (view as Switch).isChecked
                if (isChecked) viewModel.getPermanentAddress()?.let { populateAddress(it) }
                else viewModel.getCurrentAddress()?.let { populateAddress(it) }
            }
        }
        else {
            switch1.setOnClickListener { view ->
                showToast("Please add Permanent Address first")
            }
        }
    }
}