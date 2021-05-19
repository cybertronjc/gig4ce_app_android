package com.gigforce.user_preferences.location

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Switch
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.gigforce.core.datamodels.profile.AddressModel
import com.gigforce.core.datamodels.profile.ProfileData
import com.gigforce.user_preferences.R
import com.gigforce.user_preferences.SharedPreferenceViewModel
import com.gigforce.user_preferences.prefdatamodel.PreferencesDataModel
import kotlinx.android.synthetic.main.current_address_edit_fragment.*
import kotlinx.android.synthetic.main.current_address_edit_fragment.area
import kotlinx.android.synthetic.main.current_address_edit_fragment.button2
import kotlinx.android.synthetic.main.current_address_edit_fragment.cancel_button
import kotlinx.android.synthetic.main.current_address_edit_fragment.back_arrow_iv
import kotlinx.android.synthetic.main.current_address_edit_fragment.line1
import kotlinx.android.synthetic.main.current_address_edit_fragment.line2
import kotlinx.android.synthetic.main.current_address_edit_fragment.location
import kotlinx.android.synthetic.main.current_address_edit_fragment.pincode
import kotlinx.android.synthetic.main.current_address_edit_fragment.state

class CurrentAddressEditFragment : Fragment() {
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
        return inflater.inflate(R.layout.current_address_edit_fragment, container, false)
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
        back_arrow_iv.setOnClickListener { activity?.onBackPressed() }
        var currentAddress = viewModel.getCurrentAddress()
        var permanentAddress = viewModel.getPermanentAddress()
        populateAddress(currentAddress!!)
        workFromHomeSwitch.isEnabled = !permanentAddress!!.isEmpty()
        workFromHomeSwitch.isChecked = currentAddress!!.isSame(viewModel.getPermanentAddress()!!)
    }

    private fun populateAddress(address: AddressModel) {
        line1.setText(address.firstLine)
        line2.setText(address.secondLine)
        area.setText(address.area)
        location.setText(address.city)
        state.setText(address.state)
        pincode.setText(address.pincode)

    }

    private fun convertAddressToString(address: AddressModel?): String {
        Log.e("CURRENT ADDRESS", address!!.firstLine)
        return if (address!!.isEmpty() || address == null)
            getString(R.string.add_address)
        else
            address.firstLine + "," + address.secondLine + "," + address.city + "," + address.state + ". " + address.pincode
    }

    private fun listener() {
//        if (workFromHomeSwitch.isEnabled) {
            workFromHomeSwitch.setOnClickListener { view ->
                var isChecked = (view as Switch).isChecked
                if (isChecked) populateAddress(profileDataModel.address.home)
                else populateAddress(profileDataModel.address.current)
            }

            cancel_button.setOnClickListener {
                activity?.onBackPressed()
            }

            button2.setOnClickListener {
                if(validate()) {
                    var editedAddress = AddressModel(
                        line1.text.toString(),
                        line2.text.toString(),
                        area.text.toString(),
                        location.text.toString(),
                        state.text.toString(),
                        pincode.text.toString()
                    )
                    Log.e("EDIT CURRENT", convertAddressToString(editedAddress))
                    viewModel.setCurrentAddress(editedAddress)
                    activity?.onBackPressed()
                }
            }
//        }
    }

    fun addressIsValid(view: EditText):Boolean{
        if(view.text.toString().trim().length<3){
            view.setError(getString(R.string.more_detail))
            return false
        }
        return true
    }
    private fun validate(): Boolean {
        if(!addressIsValid(line1))
            return false
        if(!addressIsValid(line2))
            return false
        if(!addressIsValid(area))
            return false
        if(!addressIsValid(location))
            return false
        if(!addressIsValid(state))
            return false
        if(pincode.text.toString().length<6){
            pincode.setError(getString(R.string.pincode_not_correct))
            return false
        }
        return true
    }

}